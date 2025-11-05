package com.wgeplant.model.repository

import com.wgeplant.common.dto.response.toDomain
import com.wgeplant.model.datasource.local.LocalAppointmentDataSource
import com.wgeplant.model.datasource.local.LocalUserDataSource
import com.wgeplant.model.datasource.remote.RemoteAppointmentDataSource
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.toRequestDto
import com.wgeplant.model.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

/**
 * This class manages the data of appointments remote and locally.
 * @param remoteAppointmentData to manage the remote appointment data
 * @param localAppointmentData to manage the local appointment data
 * @param localUserData to manage the local users of the appointments
 */
class AppointmentRepoImpl @Inject constructor(
    private val remoteAppointmentData: RemoteAppointmentDataSource,
    private val localAppointmentData: LocalAppointmentDataSource,
    private val localUserData: LocalUserDataSource
) : AppointmentRepo {
    /**
     * This method creates a new appointment in the server.
     * It is saved in persistence, if the current user is part of the appointment
     * @param appointment the new appointment
     */
    override suspend fun createAppointment(appointment: Appointment): Result<Unit, DomainError> {
        val remoteCreation = remoteAppointmentData.createAppointment(appointment.toRequestDto())
        return when (remoteCreation) {
            is Result.Success -> {
                val remoteAppointment = remoteCreation.data
                if (remoteAppointment.affectedUsers.contains(Persistence.getLocalUserId())) {
                    localAppointmentData.saveAppointment(remoteAppointment.toDomain())
                } else {
                    Result.Success(Unit)
                }
            }
            is Result.Error -> {
                remoteCreation
            }
        }
    }

    /**
     * This method gets the appointment by the id from persistence.
     * @param appointmentId of the appointment
     */
    override fun getAppointmentById(appointmentId: String): Flow<Result<Appointment, DomainError>> {
        return localAppointmentData.getAppointmentById(appointmentId)
    }

    /**
     * This method returns the appointments of the current user that are for the given month.
     * @param month the given month
     */
    override fun getMonthlyAppointments(month: YearMonth): Flow<Result<List<Appointment>, DomainError>> {
        return localAppointmentData.getMonthlyAppointments(month)
    }

    /**
     * This method deletes an appointment from the server and after from persistence.
     * @param appointmentId of the appointment
     */
    override suspend fun deleteAppointment(appointmentId: String): Result<Unit, DomainError> {
        return when (val remoteDeletion = remoteAppointmentData.deleteAppointment(appointmentId)) {
            is Result.Success -> {
                localAppointmentData.deleteAppointment(appointmentId)
            }
            is Result.Error -> {
                remoteDeletion
            }
        }
    }

    /**
     * This method updates the data of an appointment on the server.
     * If the user is still part of the appointment, save it in persistence.
     * Otherwise it is deleted.
     * @param appointment the updated appointment
     */
    override suspend fun updateAppointment(appointment: Appointment): Result<Unit, DomainError> {
        val remoteUpdate = remoteAppointmentData.updateAppointment(appointment.toRequestDto())
        return when (remoteUpdate) {
            is Result.Success -> {
                val updatedAppointment = remoteUpdate.data
                if (updatedAppointment.affectedUsers.contains(Persistence.getLocalUserId())) {
                    localAppointmentData.saveAppointment(updatedAppointment.toDomain())
                } else {
                    localAppointmentData
                        .deleteAppointment(appointment.appointmentId.toString())
                }
            }
            is Result.Error -> {
                remoteUpdate
            }
        }
    }

    /**
     * This method fetches an appointment from the server and saves it in persistence.
     * @param appointmentId of an updated appointment
     */
    override suspend fun fetchAndSafe(appointmentId: String): Result<Unit, DomainError> {
        when (val remoteAppointment = remoteAppointmentData.getAppointmentById(appointmentId)) {
            is Result.Success -> {
                val foundAppointment = remoteAppointment.data.toDomain()
                val updatedUserIds = foundAppointment.affectedUsers
                return when (val userId = localUserData.getLocalUserId()) {
                    is Result.Success -> {
                        if (updatedUserIds.contains(userId.data)) {
                            localAppointmentData.saveAppointment(foundAppointment)
                        } else {
                            localAppointmentData.deleteAppointment(appointmentId)
                        }
                    }
                    is Result.Error -> {
                        userId
                    }
                }
            }
            is Result.Error -> {
                return remoteAppointment
            }
        }
    }

    /**
     * This method deletes an appointment from persistence.
     * It is used when fcm informs the client about the deletion of the appointment.
     * @param appointmentId of the deleted appointment
     */
    override suspend fun deleteLocalAppointment(appointmentId: String): Result<Unit, DomainError> {
        return localAppointmentData.deleteAppointment(appointmentId)
    }
}
