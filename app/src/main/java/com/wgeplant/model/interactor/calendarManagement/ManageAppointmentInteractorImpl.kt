package com.wgeplant.model.interactor.calendarManagement

import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.repository.AppointmentRepo
import javax.inject.Inject

/**
 * This class is responsible for implementing the appointment related use cases.
 * @param appointmentRepo: The repository for appointments.
 */
class ManageAppointmentInteractorImpl @Inject constructor(
    private val appointmentRepo: AppointmentRepo
) : ManageAppointmentInteractor {

    /**
     * This method starts the creation of a new appointment.
     * @param input: The input data for the new appointment.
     */
    override suspend fun executeCreation(input: CreateAppointmentInput): Result<Unit, DomainError> {
        val newAppointment = createAppointmentObject(input, null)
        return appointmentRepo.createAppointment(newAppointment)
    }

    /**
     * This method starts the editing of an existing appointment.
     * @param appointmentId: The ID of the appointment to be edited.
     * @param edit: The edited input data for the appointment.
     */
    override suspend fun executeEditing(
        appointmentId: String,
        edit: CreateAppointmentInput
    ): Result<Unit, DomainError> {
        val newAppointment = createAppointmentObject(edit, appointmentId)
        return appointmentRepo.updateAppointment(newAppointment)
    }

    /**
     * This method starts the deletion of an existing appointment.
     * @param appointmentId: The ID of the appointment to be deleted.
     */
    override suspend fun executeDeletion(appointmentId: String): Result<Unit, DomainError> {
        return appointmentRepo.deleteAppointment(appointmentId)
    }

    /**
     * This method creates a new appointment object.
     * @param input: The input data for the new appointment.
     * @param appointmentId: The ID of the new appointment. If it's a new Appointment the ID is null.
     */
    internal fun createAppointmentObject(input: CreateAppointmentInput, appointmentId: String?): Appointment {
        val newAppointment = Appointment(
            appointmentId = appointmentId,
            title = input.title,
            startDate = input.startDate,
            endDate = input.endDate,
            affectedUsers = input.affectedUsers,
            color = input.color,
            description = input.description
        )
        return newAppointment
    }
}
