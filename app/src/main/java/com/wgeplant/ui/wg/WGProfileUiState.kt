package com.wgeplant.ui.wg

import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.WG

/**
 * Represents the UI state for the WG profile screen.
 * Holds all relevant data needed to render the WG profile UI.
 */

data class WGProfileUiState(
    val isLoading: Boolean = false,
    val wg: WG? = null,
    val users: List<User> = emptyList(),
    val invitationCode: String? = null,
    val selectedUser: User? = null,
    val isEditing: Boolean = false,
    val wgName: String = "",
    val wgNameError: String? = null,
    val showInvitationDialog: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String = "",
    val userAbsences: Map<String, List<Absence>> = emptyMap(),
    val profilePictureUrl: String? = null,
    val localProfileImage: String? = null,
    val isValid: Boolean = true,
    val absencesByUser: Map<String, List<Absence>> = emptyMap()

)
