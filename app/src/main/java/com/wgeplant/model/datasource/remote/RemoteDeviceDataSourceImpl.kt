package com.wgeplant.model.datasource.remote

import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import okio.IOException
import javax.inject.Inject

/**
 * This class is responsible for the communication with the server about the device token
 * of the current device.
 * @param apiService to send a request to the server
 * @param responseHandler to process the server response
 */
class RemoteDeviceDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler
) : RemoteDeviceDataSource {

    /**
     * This method sends a request to the server, to add the given device token of the current user.
     * It is used after the authorization of the current user.
     * @param deviceToken of the current user
     */
    override suspend fun addDeviceToken(deviceToken: String): Result<Unit, DomainError> {
        return try {
            val response = apiService.addDeviceToken(deviceToken)
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the request to the server, to delete the device token of the current user.
     * It is used after a logout.
     */
    override suspend fun deleteDeviceToken(): Result<Unit, DomainError> {
        return try {
            val response = apiService.deleteDeviceToken()
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
