package com.wgeplant.ui.calendar

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.YearMonth

/**
 * Represents the complete UI state for the calendar screen.
 *
 * Contains all data required to render both the month and week views of the calendar,
 * as well as contextual information about the currently selected day and WG profile.
 *
 * @property currentlyDisplayedMonth The [YearMonth] currently shown in the month view.
 * @property daysForCalendarGrid List of [CalendarDay]s representing the full calendar grid (including overflow days).
 * @property daysForDisplayedWeek List of [CalendarDay]s representing the currently visible week.
 * @property currentlyDisplayedDay The [LocalDate] currently selected or focused by the user.
 * @property wgProfileImageUrl Optional URL to the WG profile image shown in the top bar.
 */
@Immutable
data class CalendarUiState(
    val currentlyDisplayedMonth: YearMonth = YearMonth.now(),
    val daysForCalendarGrid: List<CalendarDay> = emptyList(),
    val daysForDisplayedWeek: List<CalendarDay> = emptyList(),
    val currentlyDisplayedDay: LocalDate = LocalDate.now(),
    val wgProfileImageUrl: String? = null
)
