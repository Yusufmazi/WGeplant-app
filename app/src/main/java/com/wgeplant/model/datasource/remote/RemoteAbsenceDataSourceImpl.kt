package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.AbsenceRequestDTO
import com.wgeplant.common.dto.response.AbsenceResponseDTO
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import okio.IOException
import javax.inject.Inject

/**
 * This class manages the communication with the server about the absence data.
 * @param apiService to send a request to the server
 * @param responseHandler to process the server response correctly
 */
class RemoteAbsenceDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler
) : RemoteAbsenceDataSource {
    /**
     * This method creates a new absence on the server.
     * @param absence the new absence
     */
    override suspend fun createAbsence(
        absence: AbsenceRequestDTO
    ): Result<AbsenceResponseDTO, DomainError> {
        return try {
            val response = apiService.createAbsence(absence)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method updates an existing absence on the server.
     * @param absence updated absence
     */
    override suspend fun updateAbsence(
        absence: AbsenceRequestDTO
    ): Result<AbsenceResponseDTO, DomainError> {
        return try {
            val response = apiService.updateAbsence(absence)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method fetches a specific absence from the server.
     * @param absenceId of the absence
     */
    override suspend fun getAbsenceById(
        absenceId: String
    ): Result<AbsenceResponseDTO, DomainError> {
        return try {
            val response = apiService.getAbsenceById(absenceId)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method deletes an absence from the server.
     * @param absenceId of the absence
     */
    override suspend fun deleteAbsence(absenceId: String): Result<Unit, DomainError> {
        return try {
            val response = apiService.deleteAbsence(absenceId)
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
