package com.wgeplant.ui.wg

import androidx.compose.runtime.Immutable

/**
 * Represents the UI state for the Choose WG screen.
 *
 * This state holds the user's display name and optional profile image URL,
 * which are displayed in the UI to personalize the screen.
 *
 * @property userDisplayName The name of the user to be shown in the UI.
 * @property userProfileImageUrl Optional URL to the user's profile image.
 */
@Immutable
data class ChooseWGUiState(
    val userDisplayName: String = "",
    val userProfileImageUrl: String? = null
)
