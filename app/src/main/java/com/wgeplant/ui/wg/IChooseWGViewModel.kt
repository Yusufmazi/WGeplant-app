package com.wgeplant.ui.wg

import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the contract for the ChooseWGViewModel.
 *
 * @property uiState Represents the current UI state of the Choose WG screen.
 * @property errorMessage Holds an optional error message to be displayed.
 */
interface IChooseWGViewModel {
    /**
     * The current UI state of the Choose WG screen.
     */
    val uiState: StateFlow<ChooseWGUiState>

    /**
     * The current error message, if any.
     */
    val errorMessage: StateFlow<String?>

    /**
     * Navigates to the screen for creating a new WG.
     *
     * @param navController The navigation controller used to perform navigation actions.
     */
    fun navigateToCreateWG(navController: NavController)

    /**
     * Navigates to the screen for joining an existing WG.
     *
     * @param navController The navigation controller used to perform navigation actions.
     */
    fun navigateToJoinWG(navController: NavController)

    /**
     * Navigates to the user's profile screen.
     *
     * @param navController The navigation controller used to perform navigation actions.
     */
    fun navigateToUserProfile(navController: NavController)
}
