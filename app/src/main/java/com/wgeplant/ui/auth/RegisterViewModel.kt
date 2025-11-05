package com.wgeplant.ui.auth

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.userManagement.AuthInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Implementation of [IRegisterViewModel] using [AuthInteractor] to handle
 * the registration logic and manage UI state for the registration screen.
 *
 * This ViewModel holds and updates the [RegisterUiState], handles user input changes,
 * and performs registration actions.
 *
 * @param model The [AuthInteractor] used for user registration.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    model: AuthInteractor
) : BaseViewModel<RegisterUiState, AuthInteractor>(RegisterUiState(), model), IRegisterViewModel {

    companion object {
        const val FIELD_EMPTY = "Fülle dieses Feld aus."
        const val EMAIL_INVALID = "Ungültiges E-Mail-Format."
        const val PASSWORD_TOO_SHORT = "Passwort muss mindestens 8 Zeichen lang sein."
        const val PASSWORD_WEAK = "Passwort muss drei Zeichenarten beinhalten."
        const val DISPLAY_NAME_TOO_LONG = "Zu langer Anzeigename."
        const val DISPLAY_NAME_INVALID_CHARS = "Erlaubte Sonderzeichen: .-"
        const val DISPLAY_NAME_NO_LETTER = "Muss einen Buchstaben enthalten."
        const val FIX_INPUTS = "Ändere bitte deine Eingaben."
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_DISPLAY_NAME_LENGTH = 15
        const val MIN_PASSWORD_CHAR_TYPES = 3
    }

    /**
     * Internal object containing regex patterns for input validation.
     */
    private object ValidationRegex {
        val EMAIL = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        val HAS_UPPERCASE = "[A-ZÄÖÜ]".toRegex()
        val HAS_LOWERCASE = "[a-zäöü]".toRegex()
        val HAS_DIGIT = "[0-9]".toRegex()
        val HAS_SPECIAL = "[^a-zA-Z0-9]".toRegex()
        val DISPLAY_NAME_ALLOWED = "^[a-zA-Z0-9 äöüÄÖÜß.-]+$".toRegex()
        val DISPLAY_NAME_HAS_LETTER = "[a-zA-ZäöüÄÖÜ]".toRegex()
    }

    // ----- Input Change Handlers -----

    override fun onEmailChanged(newEmail: String) {
        updateUiState { it.copy(email = newEmail, emailError = null) }
    }

    override fun onPasswordChanged(newPassword: String) {
        updateUiState { it.copy(password = newPassword, passwordError = null) }
    }

    override fun onPasswordVisibilityChanged() {
        updateUiState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    override fun onDisplayNameChanged(newDisplayName: String) {
        updateUiState { it.copy(displayName = newDisplayName, displayNameError = null) }
    }

    // ----- Validation Methods -----

    private fun validateEmail(email: String) {
        val error = when {
            email.isBlank() -> FIELD_EMPTY
            !email.matches(ValidationRegex.EMAIL) -> EMAIL_INVALID
            else -> null
        }
        updateUiState { it.copy(emailError = error) }
    }

    private fun validatePassword(password: String) {
        val error = when {
            password.isBlank() -> FIELD_EMPTY
            password.length < MIN_PASSWORD_LENGTH -> PASSWORD_TOO_SHORT
            !hasThreeCharacterTypes(password) -> PASSWORD_WEAK
            else -> null
        }
        updateUiState { it.copy(passwordError = error) }
    }

    private fun hasThreeCharacterTypes(password: String): Boolean {
        var characterTypeCount = 0

        if (password.contains(ValidationRegex.HAS_UPPERCASE)) {
            characterTypeCount++
        }

        if (password.contains(ValidationRegex.HAS_LOWERCASE)) {
            characterTypeCount++
        }

        if (password.contains(ValidationRegex.HAS_DIGIT)) {
            characterTypeCount++
        }

        if (password.contains(ValidationRegex.HAS_SPECIAL)) {
            characterTypeCount++
        }

        return characterTypeCount >= MIN_PASSWORD_CHAR_TYPES
    }

    private fun validateDisplayName(displayName: String) {
        val error = when {
            displayName.isBlank() -> FIELD_EMPTY
            displayName.length > MAX_DISPLAY_NAME_LENGTH -> DISPLAY_NAME_TOO_LONG
            !displayName.matches(ValidationRegex.DISPLAY_NAME_ALLOWED) -> DISPLAY_NAME_INVALID_CHARS
            !displayName.contains(ValidationRegex.DISPLAY_NAME_HAS_LETTER) -> DISPLAY_NAME_NO_LETTER
            else -> null
        }
        updateUiState { it.copy(displayNameError = error) }
    }

    private fun validate() {
        val currentUiState = uiState.value
        val isValid = currentUiState.emailError == null &&
            currentUiState.passwordError == null &&
            currentUiState.displayNameError == null
        updateUiState { it.copy(isValid = isValid) }
    }

    // ----- Registration Process

    /**
     * Validates all inputs, then attempts user registration via AuthInteractor.
     * On success, navigates to the ChooseWG screen; on failure, displays errors.
     * @param navController The NavController to use for navigation.
     */

    override fun register(navController: NavController) {
        validateEmail(uiState.value.email)
        validatePassword(uiState.value.password)
        validateDisplayName(uiState.value.displayName)
        validate()

        viewModelScope.launch {
            setLoading(true)
            clearError()

            val currentState = uiState.value

            if (!currentState.isValid) {
                showError(FIX_INPUTS)
                setLoading(false)
                return@launch
            }

            try {
                val result = model.executeRegistration(
                    currentState.email,
                    currentState.password,
                    currentState.displayName
                )
                when (result) {
                    is Result.Success -> {
                        navController.navigate(Routes.CHOOSE_WG) {
                            popUpTo(Routes.AUTH_START) { inclusive = true }
                        }
                    }
                    is Result.Error -> {
                        handleDomainError(result.error)
                    }
                }
            } catch (e: Exception) {
                showError(UNEXPECTED_ERROR)
            } finally {
                setLoading(false)
            }
        }
    }
}
