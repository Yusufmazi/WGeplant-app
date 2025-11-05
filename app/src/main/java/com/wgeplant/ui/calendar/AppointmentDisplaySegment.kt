package com.wgeplant.ui.calendar

import androidx.compose.runtime.Immutable
import com.wgeplant.model.domain.Appointment
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a visual segment of an appointment that is displayed on a specific day.
 *
 * This model is used for rendering multi-day appointments in a calendar view, where a single
 * [Appointment] may span across multiple days and must be divided into displayable segments.
 *
 * Each segment contains metadata that determines whether it starts or ends on the represented
 * day, and whether it is part of a multi-day sequence (start, middle, end).
 *
 * @property originalAppointment The original full appointment that this segment is derived from.
 * @property segmentStartDate The start datetime of this segment (may differ from original start).
 * @property segmentEndDate The end datetime of this segment (may differ from original end).
 * @property date The calendar day this segment is rendered on.
 * @property startsOnThisDay `true` if the segment starts on this day.
 * @property endsOnThisDay `true` if the segment ends on this day.
 * @property isMultiDayStart `true` if this segment represents the start of a multi-day appointment.
 * @property isMultiDayEnd `true` if this segment represents the end of a multi-day appointment.
 * @property isMultiDayMiddle `true` if this segment represents a middle part of a multi-day appointment.
 * @property laneIndex Optional visual lane index used for layout in overlapping views.
 */
@Immutable
data class AppointmentDisplaySegment(
    val originalAppointment: Appointment,
    val segmentStartDate: LocalDateTime,
    val segmentEndDate: LocalDateTime,
    val date: LocalDate,
    val startsOnThisDay: Boolean,
    val endsOnThisDay: Boolean,
    val isMultiDayStart: Boolean = false,
    val isMultiDayEnd: Boolean = false,
    val isMultiDayMiddle: Boolean = false,
    val laneIndex: Int? = null
) {
    /**
     * Returns `true` if this segment is part of a multi-day appointment.
     */
    fun isPartOfMultiDayAppointment(): Boolean {
        return isMultiDayStart || isMultiDayEnd || isMultiDayMiddle
    }
}
