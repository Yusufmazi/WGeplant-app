package com.wgeplant.model.interactor.userManagement

import com.wgeplant.model.datasource.remote.api.HeaderConfiguration
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractorImpl
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractorImpl
import com.wgeplant.model.interactor.remoteUpdateManagement.RemoteUpdateInteractor
import com.wgeplant.model.repository.AuthRepo
import com.wgeplant.model.repository.DeleteLocalDataRepo
import com.wgeplant.model.repository.DeviceRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.UserRepo
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * This class is responsible for implementing the authentication related use cases.
 * @param authRepo: The repository for authentication.
 * @param userRepo: The repository for users.
 * @param deleteLocalDataRepo: The repository for deleting local data.
 * @param initialDataRepo: The repository for initial data.
 * @param deviceRepo: The repository for devices.
 * @param headerConfiguration: The configuration for headers.
 * @param remoteUpdateInteractor: The interactor for remote updates.
 * @param manageDeviceInteractor: The interactor for device management.
 * @param getInitialDataInteractorImpl: The interactor for getting initial data.
 */
class AuthInteractorImpl @Inject constructor(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val deleteLocalDataRepo: DeleteLocalDataRepo,
    private val initialDataRepo: InitialDataRepo,
    private val deviceRepo: DeviceRepo,
    private val headerConfiguration: HeaderConfiguration,
    private val remoteUpdateInteractor: RemoteUpdateInteractor,
    private val manageDeviceInteractor: ManageDeviceInteractorImpl,
    private val getInitialDataInteractorImpl: GetInitialDataInteractorImpl
) : AuthInteractor {

    companion object {
        /** The constant is used to increment the counter.*/
        private const val NEXT_PROCESS = 1

        /** The constant is used to check the success of the first deletion process*/
        private const val FIRST_DELETION_PROCESS = 1

        /** The constant is used to check the success of the second deletion process*/
        private const val SECOND_DELETION_PROCESS = 2

        /** The constant is used to check the success of the third deletion process*/
        private const val THIRD_DELETION_PROCESS = 3

        /** The constant is used to check the success of the fourth deletion process*/
        private const val FOURTH_DELETION_PROCESS = 4

        /** The constant is used to check the success of the fifth deletion process*/
        private const val FIFTH_DELETION_PROCESS = 5
    }

    /**
     * This variable is used to keep track of the successful deletion processes.
     */
    private var deleteProcessCounter = FIRST_DELETION_PROCESS

    /**
     * This method registers a new user.
     * @param email: The email of the new user.
     * @param password: The password of the new user.
     * @param displayName: The display name of the new user.
     */
    override suspend fun executeRegistration(
        email: String,
        password: String,
        displayName: String
    ): Result<Unit, DomainError> {
        return when (val registerResult = authRepo.register(email, password)) {
            is Result.Success -> {
                val firebaseIdToken = registerResult.data

                return when (val getLocalUserIdResult = authRepo.getLocalUserId()) {
                    is Result.Success -> {
                        val userId = getLocalUserIdResult.data
                        userRepo.setLocalUserId(userId)

                        val deviceId = manageDeviceInteractor.getGeneratedDeviceId()
                        manageDeviceInteractor.saveDeviceIdInLocalDevice(deviceId)
                        headerConfiguration.setAuthData(firebaseIdToken, deviceId)
                        manageDeviceInteractor.saveAuthDataEMailInLocalDevice(email)
                        manageDeviceInteractor.saveAuthDataPasswordInLocalDevice(password)

                        val createUserResult = userRepo.createUser(userId, displayName)
                        return when (createUserResult) {
                            is Result.Success -> {
                                val getTokenResult = remoteUpdateInteractor.getDeviceTokenFromFCM()
                                return when (getTokenResult) {
                                    is Result.Success -> {
                                        val deviceToken = getTokenResult.data
                                        return deviceRepo.addDeviceToken(deviceToken)
                                    }
                                    is Result.Error -> Result.Error(getTokenResult.error)
                                }
                            }
                            is Result.Error -> createUserResult
                        }
                    }
                    is Result.Error -> getLocalUserIdResult
                }
            }
            is Result.Error -> registerResult
        }
    }

    /**
     * This method logs in an existing user.
     * @param email: The email of the existing user.
     * @param password: The password of the existing user.
     */
    override suspend fun executeLogin(email: String, password: String): Result<Unit, DomainError> {
        return when (val loginResult = authRepo.login(email, password)) {
            is Result.Success -> {
                val firebaseIdToken = loginResult.data

                val deviceId = manageDeviceInteractor.getGeneratedDeviceId()
                manageDeviceInteractor.saveDeviceIdInLocalDevice(deviceId)
                headerConfiguration.setAuthData(firebaseIdToken, deviceId)
                manageDeviceInteractor.saveAuthDataEMailInLocalDevice(email)
                manageDeviceInteractor.saveAuthDataPasswordInLocalDevice(password)

                when (val getLocalUserIdResult = authRepo.getLocalUserId()) {
                    is Result.Success -> userRepo.setLocalUserId(getLocalUserIdResult.data)
                    is Result.Error -> return Result.Error(getLocalUserIdResult.error)
                }

                when (val getTokenResult = remoteUpdateInteractor.getDeviceTokenFromFCM()) {
                    is Result.Success -> {
                        val deviceToken = getTokenResult.data
                        val addDeviceTokenResult = deviceRepo.addDeviceToken(deviceToken)
                        if (addDeviceTokenResult is Result.Error) {
                            return Result.Error(addDeviceTokenResult.error)
                        }
                        return initialDataRepo.getInitialData()
                    }
                    is Result.Error -> return Result.Error(getTokenResult.error)
                }
            }
            is Result.Error -> Result.Error(loginResult.error)
        }
    }

    /**
     * This method deletes all necessary data for the logout process.
     * Since there are multiple deletion processes and if one fails,
     * the whole method has to start again and the deletion of already deleted data could
     * create a loop of errors. That's why the processes which succeeded are saved, so that the next try
     * can start from there.
     */
    override suspend fun executeLogout(): Result<Unit, DomainError> {
        if (this.deleteProcessCounter == FIRST_DELETION_PROCESS) {
            val deleteDeviceTokenResult = deviceRepo.deleteDeviceToken() // delete from server
            if (deleteDeviceTokenResult is Result.Error) {
                return Result.Error(deleteDeviceTokenResult.error)
            }
            this.deleteProcessCounter += NEXT_PROCESS
        }
        if (this.deleteProcessCounter == SECOND_DELETION_PROCESS) {
            manageDeviceInteractor.clearDeviceIdFromLocalDevice()
            headerConfiguration.clearAuthData()
            this.deleteProcessCounter += NEXT_PROCESS
        }
        if (this.deleteProcessCounter == THIRD_DELETION_PROCESS) {
            // firebase needs a reauthentication of the user after more than 10 minutes of activity on the app.
            val authDataEMail = manageDeviceInteractor.getAuthDataEMailInLocalDevice()
            val authDataPassword = manageDeviceInteractor.getAuthDataPasswordInLocalDevice()
            if (authDataEMail == null || authDataPassword == null) return Result.Error(DomainError.PersistenceError)
            val loginResult = authRepo.login(authDataEMail, authDataPassword)
            if (loginResult is Result.Error) {
                return Result.Error(loginResult.error)
            }
            val firebaseLogoutResult = authRepo.logout()
            if (firebaseLogoutResult is Result.Error) {
                return Result.Error(firebaseLogoutResult.error)
            }
            manageDeviceInteractor.clearAuthDataEMailFromLocalDevice()
            manageDeviceInteractor.clearAuthDataPasswordFromLocalDevice()
            this.deleteProcessCounter += NEXT_PROCESS
        }
        if (this.deleteProcessCounter == FOURTH_DELETION_PROCESS) {
            val deleteLocalUserDataResult = deleteLocalDataRepo.deleteAllLocalData()
            if (deleteLocalUserDataResult is Result.Error) {
                return Result.Error(deleteLocalUserDataResult.error)
            }
            this.deleteProcessCounter = FIRST_DELETION_PROCESS
        }
        return Result.Success(Unit)
    }

    /**
     * This method deletes all necessary data for the account deletion process.
     * Since there are multiple deletion processes and if one fails,
     * the whole method has to start again and the deletion of already deleted data could
     * create a loop of errors. That's why the processes which succeeded are saved, so that the next try
     * can start from there.
     */
    override suspend fun executeAccountDeletion(): Result<Unit, DomainError> {
        if (this.deleteProcessCounter == FIRST_DELETION_PROCESS) {
            val isUserInWG: Result<Boolean, DomainError> = try {
                getInitialDataInteractorImpl.isUserInWG().first()
            } catch (e: Exception) {
                Result.Error(DomainError.Unknown(e))
            }
            when (isUserInWG) {
                is Result.Success -> {
                    if (isUserInWG.data) {
                        val leaveWGResult = userRepo.leaveWG()
                        if (leaveWGResult is Result.Error) {
                            return Result.Error(leaveWGResult.error)
                        }
                    }
                    this.deleteProcessCounter += NEXT_PROCESS
                }
                is Result.Error -> return Result.Error(isUserInWG.error)
            }
        }
        if (this.deleteProcessCounter == SECOND_DELETION_PROCESS) {
            val deleteRemoteUserDataResult = userRepo.deleteUserData()
            if (deleteRemoteUserDataResult is Result.Error) {
                return Result.Error(deleteRemoteUserDataResult.error)
            }
            this.deleteProcessCounter += NEXT_PROCESS
        }
        if (this.deleteProcessCounter == THIRD_DELETION_PROCESS) {
            // firebase needs a reauthentication of the user after more than 10 minutes of activity on the app
            val authDataEMail = manageDeviceInteractor.getAuthDataEMailInLocalDevice()
            val authDataPassword = manageDeviceInteractor.getAuthDataPasswordInLocalDevice()
            if (authDataEMail == null || authDataPassword == null) return Result.Error(DomainError.PersistenceError)
            val loginResult = authRepo.login(authDataEMail, authDataPassword)
            if (loginResult is Result.Error) {
                return Result.Error(loginResult.error)
            }
            val deleteFirebaseUserDataResult = authRepo.deleteAccount()
            if (deleteFirebaseUserDataResult is Result.Error) {
                return Result.Error(deleteFirebaseUserDataResult.error)
            }
            manageDeviceInteractor.clearAuthDataEMailFromLocalDevice()
            manageDeviceInteractor.clearAuthDataPasswordFromLocalDevice()
            this.deleteProcessCounter += NEXT_PROCESS
        }
        if (this.deleteProcessCounter == FOURTH_DELETION_PROCESS) {
            manageDeviceInteractor.clearDeviceIdFromLocalDevice()
            headerConfiguration.clearAuthData()
            this.deleteProcessCounter += NEXT_PROCESS
        }
        if (this.deleteProcessCounter == FIFTH_DELETION_PROCESS) {
            val deleteLocalDataResult = deleteLocalDataRepo.deleteAllLocalData()
            if (deleteLocalDataResult is Result.Error) {
                return Result.Error(deleteLocalDataResult.error)
            }
            this.deleteProcessCounter = FIRST_DELETION_PROCESS
        }
        return Result.Success(Unit)
    }
}
