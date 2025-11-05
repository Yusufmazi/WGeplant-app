package com.wgeplant.ui.calendar

import androidx.navigation.NavController
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.Task
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/**
 * Interface defining the contract for the ViewModel of the calendar screen.
 *
 * Provides UI state flows and functions to handle navigation, calendar interactions,
 * task state changes, and calendar view updates.
 */
interface ICalendarViewModel {
    /** StateFlow holding the current [CalendarUiState] for the calendar screen. */
    val uiState: StateFlow<CalendarUiState>

    /** StateFlow containing an optional error message to display in the UI. */
    val errorMessage: StateFlow<String?>

    /** StateFlow indicating whether data is currently loading. */
    val isLoading: StateFlow<Boolean>

    /**
     * Updates the calendar state when the user selects a specific day.
     *
     * @param selectedDay The [LocalDate] selected by the user.
     */
    fun viewSelectedDay(selectedDay: LocalDate)

    /**
     * Toggles the completion state of the given task.
     *
     * @param task The [Task] whose state is to be changed.
     */
    fun changeTaskState(task: Task)

    /**
     * Navigates to the appointment creation screen.
     *
     * @param navController The [NavController] used for navigation.
     */
    fun navigateToAppointmentCreation(navController: NavController)

    /**
     * Navigates to the details screen for a specific appointment.
     *
     * @param appointment The [Appointment] to be displayed.
     * @param navController The [NavController] used for navigation.
     */
    fun navigateToAppointment(appointment: Appointment, navController: NavController)

    /**
     * Navigates to the details screen for a specific task.
     *
     * @param task The [Task] to be displayed.
     * @param navController The [NavController] used for navigation.
     */
    fun navigateToTask(task: Task, navController: NavController)

    /**
     * Navigates to the shared to-do list screen.
     *
     * @param navController The [NavController] used for navigation.
     */
    fun navigateToToDo(navController: NavController)

    /**
     * Navigates to the WG profile screen.
     *
     * @param navController The [NavController] used for navigation.
     */
    fun navigateToWGProfile(navController: NavController)

    /**
     * Navigates to the daily calendar view for the given date.
     *
     * @param selectedDay The [LocalDate] to display.
     * @param navController The [NavController] used for navigation.
     */
    fun navigateToDailyView(selectedDay: LocalDate, navController: NavController)

    /**
     * Navigates to the monthly calendar view.
     *
     * @param navController The [NavController] used for navigation.
     */
    fun navigateToMonthlyView(navController: NavController)

    /** Displays the previous month in the calendar view. */
    fun showPreviousMonth()

    /** Displays the next month in the calendar view. */
    fun showNextMonth()

    /** Displays the previous week in the calendar view. */
    fun showPreviousWeek()

    /** Displays the next week in the calendar view. */
    fun showNextWeek()

    /**
     * Returns the [CalendarDay] representing the currently selected day.
     *
     * @return The selected [CalendarDay] including its tasks and appointments.
     */
    fun getSelectedDayDetails(): CalendarDay
}
