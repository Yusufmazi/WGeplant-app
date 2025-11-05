package com.wgeplant.ui.auth

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.model.interactor.userManagement.AuthInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.auth.RegisterViewModel.Companion.FIELD_EMPTY
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FIELD_REQUIRED_ERROR = "Fülle dieses Feld aus."
private const val INVALID_EMAIL_ERROR = "Ungültiges E-Mail-Format."
private const val GENERIC_VALIDATION_ERROR = "Ändere bitte deine Eingaben."

/**
 * ViewModel for the Login screen.
 *
 * Handles user input, validation, login logic, navigation, and
 * initial state setup after authentication.
 *
 * @param model AuthInteractor used for login requests
 * @param initialDataInteractor Interactor used to check WG membership
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    model: AuthInteractor,
    private val initialDataInteractor: GetInitialDataInteractor
) : BaseViewModel<LoginUiState, AuthInteractor>(LoginUiState(), model), ILoginViewModel {

    /**
     * Internal object containing regex patterns for input validation.
     */
    private object ValidationRegex {
        val EMAIL = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
    }

    /** Updates the email field and clears previous error */
    override fun onEmailChanged(newEmail: String) {
        updateUiState { it.copy(email = newEmail, emailError = null) }
    }

    /** Updates the password field and clears previous error */
    override fun onPasswordChanged(newPassword: String) {
        updateUiState { it.copy(password = newPassword, passwordError = null) }
    }

    /** Toggles password visibility */
    override fun onPasswordVisibilityChanged() {
        updateUiState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /** Validates the current email and sets an error message if needed */
    private fun validateEmail(email: String) {
        val error = when {
            email.isBlank() -> FIELD_EMPTY
            !email.matches(ValidationRegex.EMAIL) -> INVALID_EMAIL_ERROR
            else -> null
        }
        updateUiState { it.copy(emailError = error) }
    }

    /** Validates the current password and sets an error message if needed */
    private fun validatePassword(password: String) {
        val error = if (password.isBlank()) FIELD_REQUIRED_ERROR else null
        updateUiState { it.copy(passwordError = error) }
    }

    /** Validates the overall form by checking for null error messages */
    private fun validate() {
        val currentState = uiState.value
        val isValid = currentState.emailError == null && currentState.passwordError == null
        updateUiState { it.copy(isValid = isValid) }
    }

    /**
     * Attempts to log in the user and navigates based on WG membership.
     *
     * - Validates the email and password
     * - Performs login
     * - Fetches whether user is already in a WG
     * - Navigates to either the calendar or WG selection screen
     */
    override fun login(navController: NavController) {
        validateEmail(uiState.value.email)
        validatePassword(uiState.value.password)
        validate()

        viewModelScope.launch {
            setLoading(true)
            clearError()

            val currentState = uiState.value

            if (!currentState.isValid) {
                showError(GENERIC_VALIDATION_ERROR)
                setLoading(false)
                return@launch
            }

            when (val loginResult = model.executeLogin(currentState.email, currentState.password)) {
                is Result.Success -> {}
                is Result.Error -> {
                    handleDomainError(loginResult.error)
                    setLoading(false)
                    return@launch
                }
            }
            // Check if user is in a WG
            val isInWG = when (val isInWGResult = initialDataInteractor.isUserInWG().first()) {
                is Result.Success -> isInWGResult.data
                is Result.Error -> false
            }

            updateUiState { it.copy(isInWG = isInWG) }

            // Navigate to the appropriate screen
            val destination = if (isInWG) Routes.CALENDAR_GRAPH else Routes.CHOOSE_WG

            navController.navigate(destination) {
                popUpTo(Routes.AUTH_START) { inclusive = true }
            }
            setLoading(false)
        }
    }
}
