package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * This interface is responsible for managing appointments of the user in persistence.
 */
interface LocalAppointmentDataSource {

    /**
     * This method saves a new appointment or updates an existing one in persistence.
     * @param appointment the appointment
     */
    suspend fun saveAppointment(appointment: Appointment): Result<Unit, DomainError>

    /**
     * This method returns a specific appointment from persistence.
     * @param appointmentId of the appointment
     */
    fun getAppointmentById(appointmentId: String): Flow<Result<Appointment, DomainError>>

    /**
     * This method returns the appointments of the local user in the given month.
     * @param month the month
     */
    fun getMonthlyAppointments(month: YearMonth): Flow<Result<List<Appointment>, DomainError>>

    /**
     * This method deletes an appointment in persistence.
     * @param appointmentId of the appointment
     */
    suspend fun deleteAppointment(appointmentId: String): Result<Unit, DomainError>
}
