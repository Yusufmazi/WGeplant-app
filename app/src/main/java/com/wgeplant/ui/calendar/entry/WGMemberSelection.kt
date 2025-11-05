package com.wgeplant.ui.calendar.entry

import androidx.compose.runtime.Immutable

/**
 * Represents a WG member and their selection state within a form.
 *
 * Used to track which members are currently selected for a task or appointment.
 *
 * @property id Unique identifier of the WG member.
 * @property name Display name of the WG member.
 * @property isSelected Flag indicating whether the member is selected in the current form context.
 */
@Immutable
data class WGMemberSelection(
    val id: String,
    val name: String,
    val isSelected: Boolean
)
