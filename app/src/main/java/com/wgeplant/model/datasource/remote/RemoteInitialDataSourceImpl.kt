package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.response.InitialResponseDTO
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import okio.IOException
import javax.inject.Inject

/**
 * This class is communicating with the server to request initial data in two scenarios.
 * @param apiService to send a request to the server
 * @param responseHandler to process the response of the server
 */
class RemoteInitialDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler
) : RemoteInitialDataSource {

    /**
     * This method sends the request to the server to get all the initial data of the current user.
     */
    override suspend fun getInitialData(): Result<InitialResponseDTO, DomainError> {
        return try {
            val response = apiService.getInitialData()
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the request to the server to remove the given user from the wg.
     * It should return the new initial data for the current user.
     * @param userId of the removed user
     */
    override suspend fun removeUserFromWG(userId: String): Result<InitialResponseDTO, DomainError> {
        return try {
            val response = apiService.removeUserFromWG(userId)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
