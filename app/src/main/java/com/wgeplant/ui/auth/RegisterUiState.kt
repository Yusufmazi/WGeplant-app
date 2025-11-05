package com.wgeplant.ui.auth

import androidx.compose.runtime.Immutable

/**
 * Represents the UI state of the registration screen.
 *
 * @property email The current email input.
 * @property password The current password input.
 * @property displayName The current display name input.
 * @property isPasswordVisible Flag indicating whether the password is visible.
 * @property emailError Optional error message related to the email input.
 * @property passwordError Optional error message related to the password input.
 * @property displayNameError Optional error message related to the display name input.
 * @property isValid Indicates whether the current input state is valid for submission.
 */
@Immutable
data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val displayNameError: String? = null,
    val isValid: Boolean = false
)
