package com.wgeplant.model.interactor.deviceManagement

import kotlinx.coroutines.flow.Flow

/**
 * This interface is responsible for managing the device ID.
 */
interface ManageDeviceInteractor {
    /**
     * This method generates a new device ID.
     */
    fun getGeneratedDeviceId(): String

    /**
     * This method saves the device ID in the local device.
     * @param deviceId: The device ID that got generated.
     */
    suspend fun saveDeviceIdInLocalDevice(deviceId: String)

    /**
     * This method gets the device ID from the local device.
     */
    suspend fun getSavedDeviceId(): String?

    /**
     * This method clears the device ID from the local device.
     */
    suspend fun clearDeviceIdFromLocalDevice()

    /**
     * This method gets the network connection status as a flow.
     */
    fun getNetworkConnectionStatus(): Flow<Boolean>

    /**
     * This method saves the email in the local device for the reauthentication of the local user in firebase.
     */
    suspend fun saveAuthDataEMailInLocalDevice(email: String)

    /**
     * This method gets the email from the local device for the reauthentication of the local user in firebase.
     */
    suspend fun getAuthDataEMailInLocalDevice(): String?

    /**
     * This method clears the email from the local device.
     */
    suspend fun clearAuthDataEMailFromLocalDevice()

    /**
     * This method saves the password in the local device for the reauthentication of the local user in firebase.
     */
    suspend fun saveAuthDataPasswordInLocalDevice(password: String)

    /**
     * This method gets the password from the local device for the reauthentication of the local user in firebase.
     */
    suspend fun getAuthDataPasswordInLocalDevice(): String?

    /**
     * This method clears the password from the local device.
     */
    suspend fun clearAuthDataPasswordFromLocalDevice()
}
