package com.wgeplant.ui.wg

import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for the ViewModel handling the Join WG screen logic.
 */
interface IJoinWGViewModel {
    /**
     * A [StateFlow] holding the current UI state for the "Create WG" screen.
     * Includes input values, validation flags, and error indicators.
     */
    val uiState: StateFlow<JoinWGUiState>

    /**
     * A [StateFlow] holding an optional error message to be displayed in the UI.
     */
    val errorMessage: StateFlow<String?>

    /**
     * A [StateFlow] indicating whether a background operation (e.g., creating the WG) is in progress.
     */
    val isLoading: StateFlow<Boolean>

    /**
     * Called when the invitation code input changes.
     *
     * @param newInvitationCode The new invitation code string.
     */
    fun onInvitationCodeChanged(newInvitationCode: String)

    /**
     * Called to initiate joining a WG.
     *
     * @param navController Navigation controller for handling navigation.
     */
    fun joinWG(navController: NavController)

    /**
     * Called to navigate back to the previous screen.
     *
     * @param navController Navigation controller for handling navigation.
     */
    fun navigateBack(navController: NavController)
}
