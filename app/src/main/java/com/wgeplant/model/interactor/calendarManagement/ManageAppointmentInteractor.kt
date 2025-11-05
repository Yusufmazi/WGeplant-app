package com.wgeplant.model.interactor.calendarManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for starting the appointment related use cases.
 */
interface ManageAppointmentInteractor {
    /**
     * This method creates a new appointment.
     * @param input: The input for the new appointment.
     */
    suspend fun executeCreation(input: CreateAppointmentInput): Result<Unit, DomainError>

    /**
     * This method edits an existing appointment.
     * @param appointmentId: The ID of the appointment that got edited.
     * @param edit: The input for the edited appointment.
     */
    suspend fun executeEditing(appointmentId: String, edit: CreateAppointmentInput): Result<Unit, DomainError>

    /**
     * This method deletes an existing appointment.
     * @param appointmentId: The ID of the appointment that got deleted.
     */
    suspend fun executeDeletion(appointmentId: String): Result<Unit, DomainError>
}
