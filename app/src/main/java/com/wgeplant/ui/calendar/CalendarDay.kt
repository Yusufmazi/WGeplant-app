package com.wgeplant.ui.calendar

import androidx.compose.runtime.Immutable
import com.wgeplant.model.domain.Task
import java.time.LocalDate

/**
 * Represents a single day in the calendar grid, including its metadata and scheduled content.
 *
 * This class is used to construct the visual calendar month view and supports rendering of
 * both appointments and tasks. It also includes contextual information about the day’s
 * relevance in the UI (e.g., whether it belongs to the current month or is today).
 *
 * @property date The calendar date this object represents.
 * @property isCurrentMonth `true` if this day is part of the currently displayed calendar month.
 * @property hasEntries `true` if there are any appointments or tasks scheduled for this day.
 * @property appointmentSegments A list of [AppointmentDisplaySegment]s that fall on this day.
 * @property tasks A list of [Task]s that are scheduled on this day.
 * @property isToday `true` if this day is the current system date.
 */
@Immutable
data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val hasEntries: Boolean,
    val appointmentSegments: List<AppointmentDisplaySegment>,
    val tasks: List<Task>,
    val isToday: Boolean
)
