package com.wgeplant.ui.auth

import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow

/**
 * This interface exposes the UI state, error handling, and actions
 * related to the user login process. It's designed to be used with
 * a corresponding Login screen in an MVVM architecture.
 */
interface ILoginViewModel {

    /** Represents the current UI state of the login screen (email, password, etc.). */
    val uiState: StateFlow<LoginUiState>

    /** Emits any error messages that should be shown to the user (e.g. login failure). */
    val errorMessage: StateFlow<String?>

    /** Indicates whether a loading indicator should be shown (e.g. while logging in). */
    val isLoading: StateFlow<Boolean>

    /**
     * Called when the user updates the email field.
     * @param newEmail The new email value entered by the user.
     */
    fun onEmailChanged(newEmail: String)

    /**
     * Called when the user updates the password field.
     * @param newPassword The new password value entered by the user.
     */
    fun onPasswordChanged(newPassword: String)

    /**
     * Toggles the password visibility state (e.g. show/hide password).
     */
    fun onPasswordVisibilityChanged()

    /**
     * Attempts to log the user in using the current credentials.
     * @param navController Used to navigate after successful login.
     */
    fun login(navController: NavController)

    /**
     * Navigates back to the previous screen.
     * @param navController The navigation controller managing backstack.
     */
    fun navigateBack(navController: NavController)
}
