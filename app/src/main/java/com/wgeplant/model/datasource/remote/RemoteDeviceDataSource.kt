package com.wgeplant.model.datasource.remote

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible of the communication with the server about the device token.
 */
interface RemoteDeviceDataSource {

    /**
     * This method sends a request to the server, to add the given device token of the current user.
     * It is used after the authorization of the current user.
     * @param deviceToken of the current user
     */
    suspend fun addDeviceToken(deviceToken: String): Result<Unit, DomainError>

    /**
     * This method sends the request to the server, to delete the device token of the current user.
     * It is used after a logout.
     */
    suspend fun deleteDeviceToken(): Result<Unit, DomainError>
}
