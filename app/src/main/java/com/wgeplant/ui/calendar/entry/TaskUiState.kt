package com.wgeplant.ui.calendar.entry

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.wgeplant.ui.theme.EventColors
import java.time.LocalDate

/**
 * Represents the UI state for a task creation or editing screen.
 *
 * @property title The title of the task.
 * @property date The due date of the task. Nullable if not set.
 * @property color The color associated with the task for display purposes.
 * @property description Optional detailed description of the task.
 * @property wgMembers List of WG members available for assignment selection.
 * @property titleError Error message related to the title input, null if no error.
 * @property dateError Error message related to the date input, null if no error.
 * @property affectedUsersError Error message when no users are assigned, null if no error.
 * @property isValid Indicates if the current UI state passes validation rules.
 * @property id Optional unique identifier for the task (e.g. for editing existing tasks).
 * @property isEditing Indicates whether the screen is in editing mode.
 * @property isExisting Indicates whether the task has been deleted during displaying it.
 */
@Immutable
data class TaskUiState(
    val title: String = "",
    val date: LocalDate? = null,
    val color: Color = EventColors.defaultEventColor,
    val description: String = "",
    val wgMembers: List<WGMemberSelection> = emptyList(),
    val titleError: String? = null,
    val dateError: String? = null,
    val affectedUsersError: String? = null,
    val isValid: Boolean = false,
    val id: String? = null,
    val isEditing: Boolean = false,
    val isExisting: Boolean = true
)
