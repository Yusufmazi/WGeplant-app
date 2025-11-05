package com.wgeplant.ui.user

import android.net.Uri
import com.wgeplant.model.domain.Absence

/**
 * Represents the UI state of the user profile screen.
 */
data class UserProfileUiState(
    val userName: String = "",
    val nameError: String? = null,
    val profilePictureUri: Uri? = null,
    val isInWG: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val errorMessage: String? = null,
    val showAbsenceDialog: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val confirmDialogTitle: String = "",
    val confirmDialogMessage: String = "",
    val isEditing: Boolean = false,
    val userAbsences: List<Absence> = emptyList(),
    val localProfileImage: String? = null
)
