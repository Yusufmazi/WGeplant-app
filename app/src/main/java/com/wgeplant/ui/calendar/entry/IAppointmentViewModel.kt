package com.wgeplant.ui.calendar.entry

import java.time.LocalDateTime

/**
 * ViewModel interface for appointment creation/editing screens.
 * Extends [IEntryViewModel] with appointment-specific logic.
 */
interface IAppointmentViewModel : IEntryViewModel<AppointmentUiState> {

    /**
     * Called when the start date of the appointment changes.
     * @param newStartDate The newly selected start date and time.
     */
    fun onStartDateChanged(newStartDate: LocalDateTime)

    /**
     * Called when the end date of the appointment changes.
     * @param newEndDate The newly selected end date and time.
     */
    fun onEndDateChanged(newEndDate: LocalDateTime)
}
