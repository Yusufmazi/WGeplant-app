package com.wgeplant.ui.calendar.entry

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.wgeplant.ui.theme.EventColors
import java.time.LocalDateTime

/**
 * Represents the UI state of the appointment creation and editing screen.
 *
 * @property title The current title input for the appointment.
 * @property startDate The selected start date and time of the appointment.
 * @property endDate The selected end date and time of the appointment.
 * @property color The currently selected color representing the appointment.
 * @property description The current description input for the appointment.
 * @property wgMembers A list of WG members with selection states for participation.
 * @property titleError Optional error message related to the title input.
 * @property startDateError Optional error message related to the start date input.
 * @property endDateError Optional error message related to the end date input.
 * @property affectedUsersError Optional error message when no participants are selected.
 * @property isValid Indicates whether the current form state is valid for submission.
 * @property id The unique ID of the appointment, if editing an existing one.
 * @property isEditing Indicates whether the screen is in editing mode.
 * @property isExisting Indicates whether the appointment has been deleted during displaying it.
 */
@Immutable
data class AppointmentUiState(
    val title: String = "",
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val color: Color = EventColors.defaultEventColor,
    val description: String = "",
    val wgMembers: List<WGMemberSelection> = emptyList(),
    val titleError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null,
    val affectedUsersError: String? = null,
    val isValid: Boolean = false,
    val id: String? = null,
    val isEditing: Boolean = false,
    val isExisting: Boolean = true
)
