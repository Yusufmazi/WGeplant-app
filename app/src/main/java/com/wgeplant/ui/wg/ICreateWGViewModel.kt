package com.wgeplant.ui.wg

import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface that defines the contract for the ViewModel used in the Create WG screen.
 *
 * Exposes UI state and events for managing the WG creation process.
 */
interface ICreateWGViewModel {
    /**
     * A [StateFlow] holding the current UI state for the "Create WG" screen.
     * Includes input values, validation flags, and error indicators.
     */
    val uiState: StateFlow<CreateWGUiState>

    /**
     * A [StateFlow] holding an optional error message to be displayed in the UI.
     */
    val errorMessage: StateFlow<String?>

    /**
     * A [StateFlow] indicating whether a background operation (e.g., creating the WG) is in progress.
     */
    val isLoading: StateFlow<Boolean>

    /**
     * Triggers the WG creation process and navigates to the appropriate screen on success.
     *
     * @param navController The [NavController] used to handle navigation.
     */
    fun createWG(navController: NavController)

    /**
     * Called whenever the WG name input field changes.
     *
     * @param newWGName The updated WG name entered by the user.
     */
    fun onWGNameChanged(newWGName: String)

    /**
     * Navigates back to the previous screen.
     *
     * @param navController The [NavController] used to handle navigation.
     */
    fun navigateBack(navController: NavController)
}
