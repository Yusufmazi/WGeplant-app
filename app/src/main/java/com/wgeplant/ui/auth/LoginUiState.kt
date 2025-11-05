package com.wgeplant.ui.auth

import androidx.compose.runtime.Immutable

/**
 * Immutable UI state for the login screen.
 *
 * This data class represents the current state of the login form,
 * including user input, validation errors, and derived state flags.
 *
 */

@Immutable
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isValid: Boolean = false,
    val isInWG: Boolean = false
)
