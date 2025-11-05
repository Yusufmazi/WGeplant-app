package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.AppointmentRequestDTO
import com.wgeplant.common.dto.response.AppointmentResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface manages the communication with the server about appointment data.
 */
interface RemoteAppointmentDataSource {

    /**
     * This method creates a new appointment on the server.
     * @param appointment the new appointment
     */
    suspend fun createAppointment(appointment: AppointmentRequestDTO):
        Result<AppointmentResponseDTO, DomainError>

    /**
     * This method updates the data of an existing appointment on the server.
     * @param appointment the updated appointment
     */
    suspend fun updateAppointment(appointment: AppointmentRequestDTO):
        Result<AppointmentResponseDTO, DomainError>

    /**
     * This method gets the appointment of the id from the server.
     * @param appointmentId of the appointment
     */
    suspend fun getAppointmentById(appointmentId: String):
        Result<AppointmentResponseDTO, DomainError>

    /**
     * This method deletes an appointment from the server.
     * @param appointmentId of the appointment
     */
    suspend fun deleteAppointment(appointmentId: String): Result<Unit, DomainError>
}
