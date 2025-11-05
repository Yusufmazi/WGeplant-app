package com.wgeplant.model.repository

import com.wgeplant.model.datasource.NetworkDataSource
import com.wgeplant.model.datasource.remote.RemoteDeviceDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This class manages the data of the device token with the server.
 * @param remoteDevice to communicate with the server.
 */
class DeviceRepoImpl @Inject constructor(
    private val remoteDevice: RemoteDeviceDataSource,
    private val networkDataSource: NetworkDataSource
) : DeviceRepo {
    /**
     * This method uses the remote data source, to give the server the device token of the user.
     * The device token is from firebase cloud messaging.
     * It is used to tell the server that the user is authenticated.
     * @param deviceToken the device Token
     */
    override suspend fun addDeviceToken(deviceToken: String): Result<Unit, DomainError> {
        return remoteDevice.addDeviceToken(deviceToken)
    }

    /**
     * This method uses the remote data source, to delete the current device token from the server.
     * It is used to tell the server that the user is no longer logged in.
     */
    override suspend fun deleteDeviceToken(): Result<Unit, DomainError> {
        return remoteDevice.deleteDeviceToken()
    }

    /**
     * This method uses the networkDataSource to see if the current device has network connection or not.
     */
    override fun getNetworkConnection(): Flow<Boolean> {
        return networkDataSource.observeNetworkConnection()
    }
}
