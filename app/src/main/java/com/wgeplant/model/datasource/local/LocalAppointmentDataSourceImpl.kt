package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import java.time.YearMonth
import javax.inject.Inject

/**
 * This class manages appointments of the user in persistence.
 */
class LocalAppointmentDataSourceImpl @Inject constructor() : LocalAppointmentDataSource {
    /**
     * This method saves a new appointment or updates an existing one in persistence.
     * @param appointment the appointment
     */
    override suspend fun saveAppointment(appointment: Appointment): Result<Unit, DomainError> {
        return try {
            Persistence.saveAppointment(appointment)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    /**
     * This method returns a specific appointment from persistence.
     * @param appointmentId of the appointment
     */
    override fun getAppointmentById(appointmentId: String): Flow<Result<Appointment, DomainError>> {
        return Persistence.getAppointment(appointmentId).map { appointment ->
            if (appointment != null) {
                Result.Success(appointment)
            } else {
                Result.Error(DomainError.NotFoundError)
            }
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method returns the appointments of the local user in the given month.
     * @param month the month
     */
    override fun getMonthlyAppointments(month: YearMonth): Flow<Result<List<Appointment>, DomainError>> {
        return Persistence.getMonthlyAppointments(month).map { appointmentList ->
            Result.Success(appointmentList)
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method deletes an appointment in persistence.
     * @param appointmentId of the appointment
     */
    override suspend fun deleteAppointment(appointmentId: String): Result<Unit, DomainError> {
        return Persistence.deleteAppointment(appointmentId)
    }
}
