package com.wgeplant.model.repository

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * This interface administrates the remote data exchange with the server about the device token.
 */
interface DeviceRepo {

    /**
     * This method uses the remote data source, to give the server the device token of the user.
     * The device token is from firebase cloud messaging.
     * It is used to tell the server that the user is authenticated.
     * @param deviceToken the device Token
     */
    suspend fun addDeviceToken(deviceToken: String): Result<Unit, DomainError>

    /**
     * This method uses the remote data source, to delete the current device token from the server.
     * It is used to tell the server that the user is no longer logged in.
     */
    suspend fun deleteDeviceToken(): Result<Unit, DomainError>

    /**
     * This method uses the networkDataSource to see if the current device has network connection or not.
     */
    fun getNetworkConnection(): Flow<Boolean>
}
