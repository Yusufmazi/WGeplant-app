package com.wgeplant.ui.wg

import androidx.lifecycle.viewModelScope
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the WG profile screen, including WG data,
 * members, profile picture, editing WG name, and member management.
 */
@HiltViewModel
class WGProfileViewModel @Inject constructor(
    interactor: ManageWGProfileInteractor,
    private val wgInteractor: ManageWGInteractor,
    private val userRepo: UserRepo
) : BaseViewModel<WGProfileUiState, ManageWGProfileInteractor>(
    WGProfileUiState(),
    interactor
),
    IWGProfileViewModel {

    companion object {
        private const val MAX_NAME_LENGTH = 15
        private val VALID_NAME_REGEX = Regex("^[a-zA-Z0-9 äöüÄÖÜß.-]+$")
        private const val ERROR_EMPTY_NAME = "Name darf nicht leer sein."
        private const val ERROR_MAX_LENGTH = "Maximal 15 Zeichen erlaubt."
        private const val ERROR_INVALID_CHARS = "Nur Buchstaben, Ziffern, '.', '-' und Leerzeichen erlaubt."
        private const val ERROR_LETTER_REQUIRED = "Mindestens ein Buchstabe erforderlich."
        private const val ERROR_REMOVING_USER = "Fehler beim Entfernen des Benutzers aus der WG."
        private const val ERROR_GENERIC = "Ein unerwarteter Fehler ist aufgetreten."
        private const val ERROR_REMOVING_PROFILE_PICTURE = "Fehler beim Entfernen des Profilbildes."
        private const val ERROR_UPLOADING_PROFILE_PICTURE = "Fehler beim Hochladen des Profilbildes."
    }

    init {
        loadWGData()
        loadWGMembers()
        loadCurrentUserId()
    }

    /**
     * Loads the current user ID from repository and updates UI state.
     */
    private fun loadCurrentUserId() {
        viewModelScope.launch {
            clearError()
            when (val result = userRepo.getLocalUserId()) {
                is Result.Success -> updateUiState { it.copy(currentUserId = result.data) }
                is Result.Error -> handleDomainError(result.error)
            }
        }
    }

    /**
     * Loads WG data and associated member absences, updating UI state.
     */
    override fun loadWGData() {
        viewModelScope.launch {
            clearError()
            model.getWGData().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val wg = result.data
                        model.getWGMembers().collectLatest { membersResult ->
                            when (membersResult) {
                                is Result.Success -> {
                                    val members = membersResult.data
                                    val absenceMap = mutableMapOf<String, List<Absence>>()
                                    for (user in members) {
                                        val absResult = model.getAbsence(user.userId).first()
                                        if (absResult is Result.Success) {
                                            val today = java.time.LocalDate.now()
                                            val futureAbsences = absResult.data
                                                .filter {
                                                    it.endDate.isAfter(today) ||
                                                        it.endDate.isEqual(today)
                                                }
                                                .sortedBy { it.startDate }

                                            absenceMap[user.userId] = futureAbsences
                                        } else if (absResult is Result.Error) {
                                            handleDomainError(absResult.error)
                                        }
                                    }

                                    if (wg != null) {
                                        updateUiState {
                                            it.copy(
                                                wg = wg,
                                                wgName = wg.displayName,
                                                invitationCode = wg.invitationCode,
                                                users = members,
                                                userAbsences = absenceMap
                                            )
                                        }
                                    }
                                }
                                is Result.Error -> handleDomainError(membersResult.error)
                            }
                        }
                    }
                    is Result.Error -> handleDomainError(result.error)
                }
            }
        }
    }

    /**
     * Loads WG members and updates the UI state.
     */
    override fun loadWGMembers() {
        viewModelScope.launch {
            clearError()
            model.getWGMembers().collectLatest { result ->
                when (result) {
                    is Result.Success -> updateUiState { it.copy(users = result.data) }
                    is Result.Error -> handleDomainError(result.error)
                }
            }
        }
    }

    /**
     * Toggles the WG name editing mode.
     */
    override fun toggleEditMode() {
        updateUiState { it.copy(isEditing = !it.isEditing, wgNameError = null) }
    }

    /**
     * Updates the WG name in UI state.
     */
    override fun onWGNameChanged(newName: String) {
        updateUiState { it.copy(wgName = newName) }
    }

    /**
     * Validates the WG name and updates error message in UI state if invalid.
     */
    private fun validateWGName(newName: String) {
        val trimmed = newName.trim()
        val error = when {
            trimmed.isEmpty() -> ERROR_EMPTY_NAME
            trimmed.length > MAX_NAME_LENGTH -> ERROR_MAX_LENGTH
            !VALID_NAME_REGEX.matches(trimmed) -> ERROR_INVALID_CHARS
            !trimmed.any { it.isLetter() } -> ERROR_LETTER_REQUIRED
            else -> null
        }
        updateUiState { it.copy(wgNameError = error) }
    }

    /**
     * Saves WG name editing changes if valid.
     */
    override fun saveEditing() {
        validateWGName(uiState.value.wgName)
        if (uiState.value.wgNameError != null) return

        val wgName = uiState.value.wgName

        viewModelScope.launch {
            setLoading(true)
            clearError()
            try {
                when (val result = model.executeDisplayNameChange(wgName)) {
                    is Result.Success -> {} // success - do nothing extra
                    is Result.Error -> handleDomainError(result.error)
                }
            } catch (e: Exception) {
                showError(ERROR_GENERIC)
            } finally {
                setLoading(false)
                updateUiState { it.copy(isEditing = false) }
            }
        }
    }

    /**
     * Toggles WG name editing flag and clears error message.
     */
    override fun toggleEditing() {
        updateUiState { it.copy(isEditing = !it.isEditing, wgNameError = null) }
    }

    /**
     * Shows the invitation dialog.
     */
    override fun showInvitationDialog() {
        updateUiState { it.copy(showInvitationDialog = true) }
    }

    /**
     * Hides the invitation dialog.
     */
    override fun hideInvitationDialog() {
        updateUiState { it.copy(showInvitationDialog = false) }
    }
//    /**
//     * Removes the WG profile picture by setting it to null.
//     */
//    override fun onRemoveProfilePicture() {
//        val currentPicture = uiState.value.wg?.profilePicture
//        if (currentPicture.isNullOrEmpty()) return
//
//        viewModelScope.launch {
//            val result = model.executeProfilePictureChange(Uri.EMPTY)
//            when (result) {
//                is Result.Success -> updateUiState { it.copy(wg = it.wg?.copy(profilePicture
//                = null)) }
//                is Result.Error -> showError(ERROR_REMOVING_PROFILE_PICTURE)
//            }
//        }
//    }
//
//    /**
//     * Sets a new WG profile picture and reloads WG data.
//     */
//    override fun onNewProfilePictureSelected(uri: Uri) {
//
//        updateUiState { it.copy(localProfileImage = uri.toString()) }
//
//        viewModelScope.launch {
//            val result = model.executeProfilePictureChange(uri)
//            when (result) {
//                is Result.Success -> {
//                    val uploadedUrl = result.data
//                    updateUiState {
//                        it.copy(localProfileImage = "$uploadedUrl?t=${System.currentTimeMillis()}")
//                    }
//                    loadWGData()
//                }
//                is Result.Error -> {
//                    showError(ERROR_UPLOADING_PROFILE_PICTURE)
//                }
//            }
//        }
//    }
    /**
     * Selects a user by userId, updating the UI state.
     */
    override fun onUserSelected(userId: String) {
        val selected = uiState.value.users.find { it.userId == userId }
        updateUiState { it.copy(selectedUser = selected) }
    }

    /**
     * Clears the currently selected user.
     */
    override fun clearSelectedUser() {
        updateUiState { it.copy(selectedUser = null) }
    }

    /**
     * Removes a user from the WG.
     */
    override fun removeUserFromWG(userId: String) {
        viewModelScope.launch {
            val result = wgInteractor.executeMemberKickOut(userId)
            when (result) {
                is Result.Success -> loadWGMembers()
                is Result.Error -> showError(ERROR_REMOVING_USER)
            }
        }
    }

    /**
     * Undoes editing by reloading WG data and toggling editing mode.
     */
    override fun undoEdits() {
        loadWGData()
        toggleEditing()
    }
}
