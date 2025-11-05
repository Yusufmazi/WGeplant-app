package com.wgeplant.ui.user

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.model.interactor.userManagement.AuthInteractor
import com.wgeplant.model.interactor.userManagement.ManageUserProfileInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val MAX_NAME_LENGTH = 15
private const val NAME_REGEX = "^[a-zA-Z0-9 äöüÄÖÜß.-]+$"

private const val GENERIC_ERROR = "An unexpected error occurred."
private const val DEFAULT_CONFIRM_TITLE = ""
private const val DEFAULT_CONFIRM_MESSAGE = ""

// private const val PROFILE_PIC_TIMESTAMP_KEY = "?t="
// private const val REMOVE_PIC_ERROR = "Failed to remove profile picture."
// private const val UPLOAD_PIC_ERROR = "Failed to upload profile picture."

private const val DISPLAY_NAME_EMPTY = "Name darf nicht leer sein."
private const val DISPLAY_NAME_TOO_LONG = "Maximal $MAX_NAME_LENGTH Zeichen erlaubt."
private const val DISPLAY_NAME_INVALID_CHARS =
    "Nur Buchstaben, Ziffern, '.', '-' und Leerzeichen erlaubt."
private const val DISPLAY_NAME_NO_LETTER = "Mindestens ein Buchstabe erforderlich."

/**
 * ViewModel responsible for managing the user profile screen state and business logic.
 *
 * Handles user data loading, editing display name, profile picture changes,
 * absence management, logout, account deletion, and WG-related actions.
 *
 * Communicates with interactors for user, WG, and absence management,
 * and exposes UI state via StateFlow.
 */

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    interactor: ManageUserProfileInteractor,
    private val wgInteractor: ManageWGInteractor,
    private val wgProfileInteractor: ManageWGProfileInteractor,
    private val initialDataInteractor: GetInitialDataInteractor
) : BaseViewModel<UserProfileUiState, ManageUserProfileInteractor>(
    UserProfileUiState(),
    interactor
),
    IUserProfileViewModel {

    override var showConfirmDialog by mutableStateOf(false)
    override var confirmDialogTitle by mutableStateOf(DEFAULT_CONFIRM_TITLE)
    override var confirmDialogMessage by mutableStateOf(DEFAULT_CONFIRM_MESSAGE)
    private var confirmDialogAction: () -> Unit = {}

    override var showAbsenceDialog by mutableStateOf(false)

    init {
        loadUserProfile()
    }

    /**
     * Loads the current user's profile and absence data.
     */
    override fun loadUserProfile() {
        viewModelScope.launch {
            setLoading(true)
            clearError()

            model.getUserData()
                .onEach { result ->
                    when (result) {
                        is Result.Success -> {
                            val user = result.data
                            val isInWG = when (
                                val isInWGResult = initialDataInteractor
                                    .isUserInWG().first()
                            ) {
                                is Result.Success -> isInWGResult.data
                                is Result.Error -> {
                                    handleDomainError(isInWGResult.error)
                                    false
                                }
                            }

                            updateUiState {
                                it.copy(
                                    userName = user?.displayName.orEmpty(),
                                    profilePictureUri = user?.profilePicture?.let { uri ->
                                        Uri.parse(uri)
                                    },
                                    localProfileImage = user?.profilePicture,
                                    isInWG = isInWG
                                )
                            }

                            user?.userId?.let { id ->
                                val absencesResult = wgProfileInteractor.getAbsence(id).first()
                                if (absencesResult is Result.Success) {
                                    val today = LocalDate.now()
                                    val futureAbsences = absencesResult.data
                                        .filter {
                                            it.endDate.isAfter(today) ||
                                                it.endDate.isEqual(today)
                                        }
                                        .sortedBy { it.startDate }

                                    updateUiState { current ->
                                        current.copy(userAbsences = futureAbsences)
                                    }
                                }
                            }
                        }

                        is Result.Error -> handleDomainError(result.error)
                    }
                    setLoading(false)
                }
                .catch {
                    showError(GENERIC_ERROR)
                    setLoading(false)
                }
                .collect {}
        }
    }

    /**
     * Updates the UI state when the display name changes.
     */
    override fun onDisplayNameChanged(newName: String) {
        updateUiState { it.copy(userName = newName) }
    }

    /**
     * Validates the display name and sets error message if invalid.
     */
    private fun validateDisplayName(newName: String) {
        val trimmed = newName.trim()
        val error = when {
            trimmed.isEmpty() -> DISPLAY_NAME_EMPTY
            trimmed.length > MAX_NAME_LENGTH -> DISPLAY_NAME_TOO_LONG
            !trimmed.matches(Regex(NAME_REGEX)) -> DISPLAY_NAME_INVALID_CHARS
            !trimmed.any { it.isLetter() } -> DISPLAY_NAME_NO_LETTER
            else -> null
        }

        updateUiState { it.copy(nameError = error) }
    }

    /**
     * Saves the updated display name if validation passes.
     */
    override fun saveEditing() {
        validateDisplayName(uiState.value.userName)
        if (uiState.value.nameError != null) return

        val displayName = uiState.value.userName
        viewModelScope.launch {
            setLoading(true)
            clearError()

            try {
                when (val result = model.executeDisplayNameChange(displayName)) {
                    is Result.Success -> {}
                    is Result.Error -> handleDomainError(result.error)
                }
            } catch (e: Exception) {
                showError(GENERIC_ERROR)
            } finally {
                setLoading(false)
                updateUiState { it.copy(isEditing = false) }
            }
        }
    }

    override fun toggleEdit() {
        updateUiState {
            it.copy(
                isEditing = !it.isEditing,
                nameError = null
            )
        }
    }

    override fun logout() {
        viewModelScope.launch {
            when (val result = authInteractor.executeLogout()) {
                is Result.Success -> updateUiState { it.copy(isLoggedOut = true) }
                is Result.Error -> handleDomainError(result.error)
            }
        }
    }

    override fun deleteAccount(navController: NavController) {
        viewModelScope.launch {
            when (val result = authInteractor.executeAccountDeletion()) {
                is Result.Success -> {
                    updateUiState { it.copy(isAccountDeleted = true) }
                    navController.navigate(Routes.AUTH_START) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is Result.Error -> handleDomainError(result.error)
            }
        }
    }

    override fun leaveWG() {
        viewModelScope.launch {
            when (val result = wgInteractor.executeLeaving()) {
                is Result.Success -> { }
                is Result.Error -> showError(result.error.toString())
            }
        }
    }

    override fun addAbsence(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                val start = LocalDate.parse(startDate)
                val end = LocalDate.parse(endDate)
                when (val result = model.executeAbsenceEntry(start, end)) {
                    is Result.Success -> {
                        closeAbsenceDialog()
                        loadUserProfile()
                    }
                    is Result.Error -> showError(result.error.toString())
                }
            } catch (e: Exception) {
                showError(e.localizedMessage ?: GENERIC_ERROR)
            }
        }
    }

    override fun openAbsenceDialog() {
        showAbsenceDialog = true
        updateUiState { it.copy(showAbsenceDialog = true) }
    }

    override fun closeAbsenceDialog() {
        showAbsenceDialog = false
        updateUiState { it.copy(showAbsenceDialog = false) }
    }

    override fun openConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        confirmDialogTitle = title
        confirmDialogMessage = message
        confirmDialogAction = onConfirm
        showConfirmDialog = true
    }

    override fun closeConfirmDialog() {
        showConfirmDialog = false
    }

    override fun onConfirmAction() {
        confirmDialogAction()
        closeConfirmDialog()
    }

    override fun undoEdits() {
        loadUserProfile()
        toggleEdit()
    }

//    /**
//     * Removes the current profile picture.
//     */
//    override fun onRemoveProfilePicture() {
//        val currentUri = uiState.value.profilePictureUri ?: return
//
//        viewModelScope.launch {
//            when (val result = model.executeProfilePictureChange(Uri.EMPTY)) {
//                is Result.Success -> updateUiState {
//                    it.copy(profilePictureUri = null, localProfileImage = null)
//                }
//                is Result.Error -> showError(REMOVE_PIC_ERROR)
//            }
//        }
//    }
//
//    /**
//     * Uploads and sets the selected profile picture URI.
//     */
//    override fun changeProfilePicture(uri: Uri) {
//        updateUiState { it.copy(localProfileImage = uri.toString()) }
//
//        viewModelScope.launch {
//            when (val result = model.executeProfilePictureChange(uri)) {
//                is Result.Success -> updateUiState {
//                    it.copy(localProfileImage =
//                        "${result.data}$PROFILE_PIC_TIMESTAMP_KEY${System.currentTimeMillis()}")
//                }
//                is Result.Error -> showError(UPLOAD_PIC_ERROR)
//            }
//        }
//    }

    override fun deleteAbsence(absenceId: String) {
        viewModelScope.launch {
            val result = wgProfileInteractor.executeAbsenceDeletion(absenceId)

            when (result) {
                is Result.Success -> {
                    loadUserProfile()
                }
                is Result.Error -> {
                    handleDomainError(result.error)
                }
            }
        }
    }

    override fun editAbsence(absenceId: String, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            wgProfileInteractor.executeAbsenceEditing(absenceId, startDate, endDate)
            loadUserProfile()
        }
    }
}
