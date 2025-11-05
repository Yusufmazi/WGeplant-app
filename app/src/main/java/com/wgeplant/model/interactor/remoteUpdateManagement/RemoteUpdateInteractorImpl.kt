package com.wgeplant.model.interactor.remoteUpdateManagement

import com.google.firebase.messaging.FirebaseMessaging
import com.wgeplant.model.datasource.remote.api.HeaderConfiguration
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.repository.AbsenceRepo
import com.wgeplant.model.repository.AppointmentRepo
import com.wgeplant.model.repository.AuthRepo
import com.wgeplant.model.repository.DeleteLocalDataRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.TaskRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * This class is responsible for implementing the remote update related use cases.
 * @param userRepo: The repository for users.
 * @param wgRepo: The repository for WGs.
 * @param taskRepo: The repository for tasks.
 * @param appointmentRepo: The repository for appointments.
 * @param absenceRepo: The repository for absences.
 * @param initialDataRepo: The repository for initial data.
 * @param deleteLocalDataRepo: The repository for deleting local data.
 * @param manageDeviceInteractor: The interactor for managing devices.
 * @param headerConfiguration: The configuration for headers.
 * @param authRepo: The repository for authentication.
 */
class RemoteUpdateInteractorImpl @Inject constructor(
    private val userRepo: UserRepo,
    private val wgRepo: WGRepo,
    private val taskRepo: TaskRepo,
    private val appointmentRepo: AppointmentRepo,
    private val absenceRepo: AbsenceRepo,
    private val initialDataRepo: InitialDataRepo,
    private val deleteLocalDataRepo: DeleteLocalDataRepo,
    private val manageDeviceInteractor: ManageDeviceInteractor,
    private val headerConfiguration: HeaderConfiguration,
    private val authRepo: AuthRepo
) : RemoteUpdateInteractor {

    companion object {
        /** The constant is to set the initial retry value.*/
        private const val START_RETRY_VALUE = 0

        /** The constant is to set the maximum retries.*/
        private const val MAX_RETRIES = 2

        /** The constant is to set the initial delay.*/
        private const val INITIAL_DELAY_MS = 1000L

        /** The constant is to set the delay for firebase.*/
        private const val FIREBASE_DELAY_MS = 2000L

        /** The constant is to set the delay for the deletion of wg data.*/
        private const val WG_DELAY = 3000L

        private const val ADD_USER = "addUser"
        private const val UPDATE_USER = "updateUser"
        private const val DELETE_USER = "deleteUser"
        private const val UPDATE_WG = "updateWG"
        private const val ADD_APPOINTMENT = "addAppointment"
        private const val UPDATE_APPOINTMENT = "updateAppointment"
        private const val DELETE_APPOINTMENT = "deleteAppointment"
        private const val ADD_TASK = "addTask"
        private const val UPDATE_TASK = "updateTask"
        private const val DELETE_TASK = "deleteTask"
        private const val ADD_ABSENCE = "addAbsence"
        private const val UPDATE_ABSENCE = "updateAbsence"
        private const val DELETE_ABSENCE = "deleteAbsence"
        private const val INITIAL_DATA = "initialData"
    }

    private val interactorScope = CoroutineScope(Dispatchers.IO + Job())

    /**
     * This method fetches the device token from the Firebase Cloud Messaging service.
     */
    override suspend fun getDeviceTokenFromFCM(): Result<String, DomainError> {
        return try {
            val token = FirebaseMessaging.getInstance().token.await() // get the token from fcm
            Result.Success(token)
        } catch (e: Exception) {
            Result.Error(DomainError.FirebaseError.FcmTokenFetchFailed)
        }
    }

    /**
     * This method starts the remote update process.
     * @param operationType: The type of the operation that got executed.
     * @param objectId: The ID of the object that should be updated.
     */
    override suspend fun updateModel(operationType: String, objectId: String) {
        execute(operationType, objectId, START_RETRY_VALUE)
    }

    /**
     * This method executes the remote update process.
     * @param operationType: The type of the operation that got executed.
     * @param objectId: The ID of the object that should be updated.
     * @param retryCount: The number of retries that got executed.
     */
    private suspend fun execute(operationType: String?, objectId: String, retryCount: Int) {
        when (operationType) {
            ADD_USER, UPDATE_USER -> {
                val updateResult = userRepo.fetchAndSafe(objectId)
                when (updateResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            // this operation type gets send if there are more than one active device with the same account
            // and one device executes an account deletion.
            // Every other device will get this fcm message to delete their local data
            // since the executioner device already deletes every external data
            DELETE_USER -> {
                delay(FIREBASE_DELAY_MS) // wait for firebase to delete the user
                authRepo.reloadCurrentUser()
                manageDeviceInteractor.clearDeviceIdFromLocalDevice()
                headerConfiguration.clearAuthData()
                if (deleteLocalDataRepo.deleteAllLocalData() is Result.Error) {
                    retryExecution(operationType, objectId, retryCount)
                }
            }
            UPDATE_WG -> {
                val updateResult = wgRepo.fetchAndSafe(objectId)
                when (updateResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            ADD_APPOINTMENT, UPDATE_APPOINTMENT -> {
                val updateResult = appointmentRepo.fetchAndSafe(objectId)
                when (updateResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            DELETE_APPOINTMENT -> {
                val deleteAppointmentResult = appointmentRepo.deleteLocalAppointment(objectId)
                when (deleteAppointmentResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            ADD_TASK, UPDATE_TASK -> {
                val updateResult = taskRepo.fetchAndSafe(objectId)
                when (updateResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            DELETE_TASK -> {
                val deleteTaskResult = taskRepo.deleteLocalTask(objectId)
                when (deleteTaskResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            ADD_ABSENCE, UPDATE_ABSENCE -> {
                val updateResult = absenceRepo.fetchAndSafe(objectId)
                when (updateResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            DELETE_ABSENCE -> {
                val deleteAbsenceResult = absenceRepo.deleteLocalAbsence(objectId)
                when (deleteAbsenceResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
            // this operation type gets send if there is a removal of an user from the wg,
            // because most of the local data has to get actualized in that case
            INITIAL_DATA -> {
                // server will send the user id of the user if there is a removal of
                when (val getLocalUserId = userRepo.getLocalUserId()) {
                    is Result.Success -> {
                        if (objectId == getLocalUserId.data) {
                            delay(WG_DELAY)
                            // the deleteUser operation gets received after this command and need to get executed first
                            deleteLocalDataRepo.deleteAllWGRelatedData()
                            return
                        }
                    }
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
                val updateResult = initialDataRepo.getInitialData()
                when (updateResult) {
                    is Result.Success -> return
                    is Result.Error -> retryExecution(operationType, objectId, retryCount)
                }
            }
        }
    }

    /**
     * This method retries the execution of the remote update process.
     * @param operationType: The type of the operation that got executed.
     * @param objectId: The ID of the object that should be updated.
     * @param retryCount: The number of retries that got executed.
     */
    private fun retryExecution(operationType: String, objectId: String, retryCount: Int) {
        if (retryCount < MAX_RETRIES) {
            val delayTime = INITIAL_DELAY_MS * (1 shl retryCount)
            interactorScope.launch {
                delay(delayTime)
                execute(operationType, objectId, retryCount + 1)
            }
        } else {
            return // This should be revisited and get an error handling
        }
    }

    /**
     * This method is for Unit Tests to prevent memory leaks, not needed in the real application,
     * since the OS should shut it down after leaving the app
     */
    fun cancelAllPendingOperations() {
        interactorScope.cancel()
    }
}
