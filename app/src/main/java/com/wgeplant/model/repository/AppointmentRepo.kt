package com.wgeplant.model.repository

import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * This interface administrates the appointment data in the server and persistence.
 */
interface AppointmentRepo {

    /**
     * This method creates a new appointment in the server.
     * It is saved in persistence, if the current user is part of the appointment
     * @param appointment the new appointment
     */
    suspend fun createAppointment(appointment: Appointment): Result<Unit, DomainError>

    /**
     * This method gets the appointment by the id from persistence.
     * @param appointmentId of the appointment
     */
    fun getAppointmentById(appointmentId: String): Flow<Result<Appointment, DomainError>>

    /**
     * This method returns the appointments of the current user that are for the given month.
     * @param month the given month
     */
    fun getMonthlyAppointments(month: YearMonth): Flow<Result<List<Appointment>, DomainError>>

    /**
     * This method deletes an appointment from the server and after from persistence.
     * @param appointmentId of the appointment
     */
    suspend fun deleteAppointment(appointmentId: String): Result<Unit, DomainError>

    /**
     * This method updates the data of an appointment on the server.
     * If the user is still part of the appointment, save it in persistence.
     * Otherwise it is deleted.
     * @param appointment the updated appointment
     */
    suspend fun updateAppointment(appointment: Appointment): Result<Unit, DomainError>

    /**
     * This method fetches an appointment from the server and saves it in persistence.
     * @param appointmentId of an updated appointment
     */
    suspend fun fetchAndSafe(appointmentId: String): Result<Unit, DomainError>

    /**
     * This method deletes an appointment from persistence.
     * It is used when fcm informs the client about the deletion of the appointment.
     * @param appointmentId of the deleted appointment
     */
    suspend fun deleteLocalAppointment(appointmentId: String): Result<Unit, DomainError>
}
