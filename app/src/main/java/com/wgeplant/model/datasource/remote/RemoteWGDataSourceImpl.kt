package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.WGRequestDTO
import com.wgeplant.common.dto.response.WGResponseDTO
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import okio.IOException
import javax.inject.Inject

/**
 * This class is communicating with the server about wg data.
 * @param apiService to send a request to the server
 * @param responseHandler to process the response of the server
 */
class RemoteWGDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler
) : RemoteWGDataSource {

    /**
     * This method sends the server the request to create a new wg.
     * @param wg the new wg
     */
    override suspend fun createWGRemote(wg: WGRequestDTO): Result<WGResponseDTO, DomainError> {
        return try {
            val response = apiService.createWGRemote(wg)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the server the request to get the wg of the id.
     * @param wgId of the wanted wg
     */
    override suspend fun getWGById(wgId: String): Result<WGResponseDTO, DomainError> {
        return try {
            val response = apiService.getWGById(wgId)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the server the request to update the data of the given wg.
     * @param wg the updated data of the wg
     */
    override suspend fun updateWG(wg: WGRequestDTO): Result<WGResponseDTO, DomainError> {
        return try {
            val response = apiService.updateWG(wg)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
