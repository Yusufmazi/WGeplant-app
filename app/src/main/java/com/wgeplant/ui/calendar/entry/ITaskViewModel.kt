package com.wgeplant.ui.calendar.entry

import java.time.LocalDate

/**
 * ViewModel interface for task creation/editing screens.
 * Extends [IEntryViewModel] with task-specific logic.
 */
interface ITaskViewModel : IEntryViewModel<TaskUiState> {

    /**
     * Called when the due date of the task changes.
     * @param newDate The newly selected due date (can be null).
     */
    fun onDateChanged(newDate: LocalDate?)
}
