package com.wgeplant.ui.wg

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel implementation for the Create WG screen.
 *
 * Handles user input validation, triggers the WG creation process via [ManageWGInteractor],
 * and manages navigation after successful creation. Also exposes UI state, error messages, and loading state.
 *
 * @param model The interactor used for managing WG creation.
 */
@HiltViewModel
class CreateWGViewModel @Inject constructor(
    model: ManageWGInteractor
) : BaseViewModel<CreateWGUiState, ManageWGInteractor>(CreateWGUiState(), model), ICreateWGViewModel {

    companion object {
        const val FIELD_EMPTY = "Fülle dieses Feld aus."
        const val DISPLAY_NAME_TOO_LONG = "Zu langer WG-Name."
        const val DISPLAY_NAME_INVALID_CHARS = "Erlaubte Sonderzeichen: .-"
        const val DISPLAY_NAME_NO_LETTER = "Muss einen Buchstaben enthalten."
        const val FIX_INPUTS = "Ändere bitte deine Eingabe."
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
        const val MAX_DISPLAY_NAME_LENGTH = 15
    }

    /**
     * Internal object containing regex patterns for input validation.
     */
    private object ValidationRegex {
        val DISPLAY_NAME_ALLOWED = "^[a-zA-Z0-9 äöüÄÖÜß.-]+$".toRegex()
        val DISPLAY_NAME_HAS_LETTER = "[a-zA-ZäöüÄÖÜ]".toRegex()
    }

    override fun onWGNameChanged(newWGName: String) {
        updateUiState { it.copy(wgName = newWGName, wgNameError = null) }
    }

    private fun validateWGName(wgName: String) {
        val error = when {
            wgName.isBlank() -> FIELD_EMPTY
            wgName.length > MAX_DISPLAY_NAME_LENGTH -> DISPLAY_NAME_TOO_LONG
            !wgName.matches(ValidationRegex.DISPLAY_NAME_ALLOWED) -> DISPLAY_NAME_INVALID_CHARS
            !wgName.contains(ValidationRegex.DISPLAY_NAME_HAS_LETTER) -> DISPLAY_NAME_NO_LETTER
            else -> null
        }
        updateUiState { it.copy(wgNameError = error, isValid = (error == null)) }
    }

    override fun createWG(navController: NavController) {
        validateWGName(uiState.value.wgName)

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
                val result = model.executeCreation(
                    currentState.wgName
                )
                when (result) {
                    is Result.Success -> {
                        navController.navigate(Routes.CALENDAR_GRAPH) {
                            popUpTo(Routes.CHOOSE_WG) { inclusive = true }
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
