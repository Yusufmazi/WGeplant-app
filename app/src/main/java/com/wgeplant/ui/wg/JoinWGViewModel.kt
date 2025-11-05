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
 * ViewModel responsible for handling the UI logic of the Join WG screen.
 *
 * Manages the invitation code input, validates it, controls loading and error states,
 * and performs the joining operation by interacting with the domain layer.
 *
 * Implements [IJoinWGViewModel] to provide state flows and user actions.
 *
 * @param model The interactor used for managing WG joining.
 */
@HiltViewModel
class JoinWGViewModel @Inject constructor(
    model: ManageWGInteractor
) : BaseViewModel<JoinWGUiState, ManageWGInteractor>(JoinWGUiState(), model), IJoinWGViewModel {

    companion object {
        const val FIELD_EMPTY = "Fülle dieses Feld aus."
        const val INVALID_CODE_FORMAT = "Ungültiges Format für den Code."
        const val CODE_LENGTH = 6
        const val FIX_INPUTS = "Ändere bitte deine Eingabe."
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
    }

    /**
     * Internal object containing regex patterns for input validation.
     */
    private object ValidationRegex {
        val INVITATION_CODE_FORMAT = "^[A-Z0-9]+$".toRegex()
    }

    override fun onInvitationCodeChanged(newInvitationCode: String) {
        updateUiState { it.copy(invitationCode = newInvitationCode, invitationCodeError = null) }
    }

    private fun validateInvitationCode(invitationCode: String) {
        val error = when {
            invitationCode.isBlank() -> FIELD_EMPTY
            invitationCode.length != CODE_LENGTH -> INVALID_CODE_FORMAT
            !invitationCode.matches(ValidationRegex.INVITATION_CODE_FORMAT) -> INVALID_CODE_FORMAT
            else -> null
        }
        updateUiState { it.copy(invitationCodeError = error, isValid = (error == null)) }
    }

    override fun joinWG(navController: NavController) {
        validateInvitationCode(uiState.value.invitationCode)

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
                val result = model.executeJoining(
                    currentState.invitationCode
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
