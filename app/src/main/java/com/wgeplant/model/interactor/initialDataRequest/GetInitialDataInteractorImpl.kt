package com.wgeplant.model.interactor.initialDataRequest

import com.wgeplant.model.datasource.remote.api.HeaderConfiguration
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.repository.AuthRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * This class is responsible for implementing the process of requesting the data for starting the app
 * @param initialDataRepo: The repository for the initial data.
 * @param authRepo: The repository for authentication.
 * @param wgRepo: The repository for the WG.
 * @param manageDeviceInteractor: The interactor for the device management.
 * @param headerConfiguration: The configuration for the headers.
 * @param userRepo: The repository for the user.
 */
class GetInitialDataInteractorImpl @Inject constructor(
    private val initialDataRepo: InitialDataRepo,
    private val authRepo: AuthRepo,
    private val wgRepo: WGRepo,
    private val manageDeviceInteractor: ManageDeviceInteractor,
    private val headerConfiguration: HeaderConfiguration,
    private val userRepo: UserRepo
) : GetInitialDataInteractor {

    companion object {
        /** The constant is used to confirm that the user is logged in.*/
        private const val USER_LOGGED_IN = true

        /** The constant is used to confirm that the user is not logged in.*/
        private const val USER_NOT_LOGGED_IN = false

        /** The constant is used to confirm that the user is in the WG.*/
        private const val USER_IN_WG = true

        /** The constant is used to confirm that the user is not in the WG.*/
        private const val USER_NOT_IN_WG = false
    }

    /**
     * This method starts the process of requesting and saving the initial data.
     * It also sets the user ID of the local user.
     */
    override suspend fun execute(): Result<Unit, DomainError> {
        return when (val getInitialDataResult = initialDataRepo.getInitialData()) {
            is Result.Success -> {
                when (val getLocalUserIdResult = authRepo.getLocalUserId()) {
                    is Result.Success -> {
                        userRepo.setLocalUserId(getLocalUserIdResult.data)
                    }
                    is Result.Error -> Result.Error(getLocalUserIdResult.error)
                }
            }
            is Result.Error -> Result.Error(getInitialDataResult.error)
        }
    }

    /**
     * This method checks if the user is logged in and sets the header for the communication
     * between client and server if the user is logged in.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun isUserLoggedIn(): Flow<Result<Boolean, DomainError>> {
        return authRepo.getAuthStateFlow()
            .flatMapLatest { userStateResult ->
                when (userStateResult) {
                    is Result.Success -> {
                        if (userStateResult.data) {
                            val isUserLoggedIn = authRepo.isLoggedIn()
                            when (isUserLoggedIn) {
                                is Result.Success -> {
                                    try {
                                        flow {
                                            val deviceId = manageDeviceInteractor.getSavedDeviceId()
                                            headerConfiguration.setAuthData(
                                                isUserLoggedIn.data,
                                                deviceId
                                            )
                                            emit(Result.Success(USER_LOGGED_IN))
                                        }
                                    } catch (e: Exception) {
                                        flowOf(Result.Error(DomainError.Unknown(e)))
                                    }
                                }
                                is Result.Error -> flowOf(isUserLoggedIn)
                            }
                        } else {
                            flowOf(Result.Success(USER_NOT_LOGGED_IN))
                        }
                    }

                    is Result.Error -> flowOf(userStateResult)
                }
            }
            .catch { e -> emit(Result.Error(DomainError.Unknown(e))) }
    }

    /**
     * This method checks if the user is in the WG.
     */
    override fun isUserInWG(): Flow<Result<Boolean, DomainError>> {
        return wgRepo.getWG()
            .map { result ->
                when (result) {
                    is Result.Success -> Result.Success(USER_IN_WG)
                    is Result.Error -> {
                        if (result.error is DomainError.PersistenceError) {
                            Result.Success(USER_NOT_IN_WG)
                        } else {
                            Result.Error(result.error)
                        }
                    }
                }
            }
            .catch { e -> emit(Result.Error(DomainError.Unknown(e))) }
    }
}
