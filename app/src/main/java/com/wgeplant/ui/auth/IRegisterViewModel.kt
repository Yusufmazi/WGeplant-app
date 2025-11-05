package com.wgeplant.ui.auth

import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for the RegisterViewModel which manages the state and user interactions
 * for the registration screen.
 */

interface IRegisterViewModel {

    /** Observable UI state of the registration form. */
    val uiState: StateFlow<RegisterUiState>

    /** Error message to be shown. */
    val errorMessage: StateFlow<String?>

    /** Indicates whether a loading process is currently running. */
    val isLoading: StateFlow<Boolean>

    /** Called when the email input changes.
     * @param newEmail The new email.
     * */
    fun onEmailChanged(newEmail: String)

    /** Called when the password input changes.
     * @param newPassword The new password.
     */
    fun onPasswordChanged(newPassword: String)

    /** Toggles password visibility. */
    fun onPasswordVisibilityChanged()

    /** Called when the display name input changes.
     * @param newDisplayName The new display name.
     */
    fun onDisplayNameChanged(newDisplayName: String)

    /** Navigates back to the previous screen using the provided NavController.
     * @param navController The NavController to use for navigation.
     */
    fun navigateBack(navController: NavController)

    /** Executes the registration process.
     * @param navController The NavController to use for navigation.
     */
    fun register(navController: NavController)
}
