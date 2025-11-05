package com.wgeplant.model.interactor.deviceManagement

import android.content.Context
import com.wgeplant.model.repository.DeviceRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * This class is responsible for implementing the management of the device ID.
 * @param context: The context of the application.
 */
class ManageDeviceInteractorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceRepo: DeviceRepo
) : ManageDeviceInteractor {

    companion object {
        /** The constant is used to set the name of the shared preferences.*/
        internal const val PREFS_NAME = "device_prefs"

        /** The constant is used to set the key of the device id.*/
        internal const val KEY_DEVICE_ID = "device_id"

        internal const val KEY_AUTH_DATA_EMAIL = "email"
        internal const val KEY_AUTH_DATA_PASSWORD = "password"
    }
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * This method generates a unique device id.
     */
    override fun getGeneratedDeviceId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * This method saves the device id in the local device.
     * @param deviceId: The device id.
     */
    override suspend fun saveDeviceIdInLocalDevice(deviceId: String) {
        sharedPreferences.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    /**
     * This method gets the device id from the local device.
     */
    override suspend fun getSavedDeviceId(): String? {
        return sharedPreferences.getString(KEY_DEVICE_ID, null)
    }

    /**
     * This method clears the device id from the local device.
     */
    override suspend fun clearDeviceIdFromLocalDevice() {
        sharedPreferences.edit().remove(KEY_DEVICE_ID).apply()
    }

    /**
     * This method gets the network connection status as a flow.
     */
    override fun getNetworkConnectionStatus(): Flow<Boolean> {
        return deviceRepo.getNetworkConnection()
    }

    /**
     * This method saves the email in the local device for the reauthentication of the local user in firebase.
     */
    override suspend fun saveAuthDataEMailInLocalDevice(email: String) {
        sharedPreferences.edit().putString(KEY_AUTH_DATA_EMAIL, email).apply()
    }

    /**
     * This method gets the email from the local device for the reauthentication of the local user in firebase.
     */
    override suspend fun getAuthDataEMailInLocalDevice(): String? {
        return sharedPreferences.getString(KEY_AUTH_DATA_EMAIL, null)
    }

    /**
     * This method clears the email from the local device.
     */
    override suspend fun clearAuthDataEMailFromLocalDevice() {
        sharedPreferences.edit().remove(KEY_AUTH_DATA_EMAIL).apply()
    }

    /**
     * This method saves the password in the local device for the reauthentication of the local user in firebase.
     */
    override suspend fun saveAuthDataPasswordInLocalDevice(password: String) {
        sharedPreferences.edit().putString(KEY_AUTH_DATA_PASSWORD, password).apply()
    }

    /**
     * This method gets the password from the local device for the reauthentication of the local user in firebase.
     */
    override suspend fun getAuthDataPasswordInLocalDevice(): String? {
        return sharedPreferences.getString(KEY_AUTH_DATA_PASSWORD, null)
    }

    /**
     * This method clears the password from the local device.
     */
    override suspend fun clearAuthDataPasswordFromLocalDevice() {
        sharedPreferences.edit().remove(KEY_AUTH_DATA_PASSWORD).apply()
    }
}
