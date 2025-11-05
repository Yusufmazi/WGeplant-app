package com.wgeplant.remoteFCMUpdates

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.remoteUpdateManagement.RemoteUpdateInteractor
import com.wgeplant.model.repository.DeviceRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This class is responsible for receiving push notifications from Firebase.
 */
@AndroidEntryPoint
class RemoteDataSyncService : FirebaseMessagingService() {

    @Inject
    lateinit var remoteUpdateInteractor: RemoteUpdateInteractor
    private lateinit var deviceRepo: DeviceRepo

    // CoroutineScope for asynchronous tasks
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /**
     * This method is called when a new push notification is received.
     * @param remoteMessage: The received push notification.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }
    }

    /**
     * This method is called when a new push notification token is received to handel the new data.
     * The only way for an fcm message to get lost, when firebase is functioning as it should,
     * is when the local user lost the internet connection and in that case the
     * handling of the network will solve that problem though actualizing all local data though an initial data request.
     * @param data: The received push notification data.
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val operationType = data["operationType"]
        val objectId = data["objectId"]
        if (operationType == null || objectId == null) return
        // errors getting handled though the lost internet connection handling
        serviceScope.launch {
            remoteUpdateInteractor.updateModel(operationType, objectId)
        }
    }

    /**
     * This methods is called when a new device token is received.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            updateDeviceToken(token)
        }
    }

    /**
     * This method updates the device token in the server, if firebase changes the token
     * which normally shouldn't happen.
     * @param token: The new device token.
     */
    private suspend fun updateDeviceToken(token: String) {
        try {
            val deleteOldTokenResult = deviceRepo.deleteDeviceToken()
            if (deleteOldTokenResult is Result.Error) {
                return
            }
            val addNewTokenResult = deviceRepo.addDeviceToken(token)
            if (addNewTokenResult is Result.Error) {
                return
            }
        } catch (e: Exception) {
            return
        }
    }
}
