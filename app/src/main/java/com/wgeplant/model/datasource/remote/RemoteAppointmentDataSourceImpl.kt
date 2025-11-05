package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.AppointmentRequestDTO
import com.wgeplant.common.dto.response.AppointmentResponseDTO
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import okio.IOException
import javax.inject.Inject

/**
 * This class manages the communication with the server about appointment data.
 * @param apiService to send a request to the server
 * @param responseHandler to process the server result
 */

class RemoteAppointmentDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler
) : RemoteAppointmentDataSource {

    /**
     * This method creates a new appointment on the server.
     * @param appointment the new appointment
     */
    override suspend fun createAppointment(
        appointment: AppointmentRequestDTO
    ): Result<AppointmentResponseDTO, DomainError> {
        return try {
            val response = apiService.createAppointment(appointment)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method updates the data of an existing appointment on the server.
     * @param appointment the updated appointment
     */
    override suspend fun updateAppointment(
        appointment: AppointmentRequestDTO
    ): Result<AppointmentResponseDTO, DomainError> {
        return try {
            val response = apiService.updateAppointment(appointment)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method gets the appointment of the id from the server.
     * @param appointmentId of the appointment
     */
    override suspend fun getAppointmentById(
        appointmentId: String
    ): Result<AppointmentResponseDTO, DomainError> {
        return try {
            val response = apiService.getAppointmentById(appointmentId)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method deletes an appointment from the server.
     * @param appointmentId of the appointment
     */
    override suspend fun deleteAppointment(appointmentId: String): Result<Unit, DomainError> {
        return try {
            val response = apiService.deleteAppointment(appointmentId)
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
