package com.wgeplant.ui.wg

import androidx.compose.runtime.Immutable

/**
 * Represents the UI state for the Create WG screen.
 *
 * @property wgName The current name input for the WG.
 * @property wgNameError Optional error message related to the WG name input, if any.
 * @property isValid Indicates whether the current input state is valid.
 */
@Immutable
data class CreateWGUiState(
    val wgName: String = "",
    val wgNameError: String? = null,
    val isValid: Boolean = false
)
