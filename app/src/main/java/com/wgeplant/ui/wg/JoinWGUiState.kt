package com.wgeplant.ui.wg

import androidx.compose.runtime.Immutable

/**
 * Represents the UI state for the Join WG screen.
 *
 * @property invitationCode The current input value of the invitation code.
 * @property invitationCodeError Optional validation error message for the invitation code.
 * @property isValid Indicates whether the current invitation code input is valid.
 */
@Immutable
data class JoinWGUiState(
    val invitationCode: String = "",
    val invitationCodeError: String? = null,
    val isValid: Boolean = false
)
