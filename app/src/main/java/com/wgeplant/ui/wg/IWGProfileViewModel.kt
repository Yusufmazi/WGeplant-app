package com.wgeplant.ui.wg

import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the contract for the WG (shared flat) profile ViewModel.
 * Exposes UI state flows and actions related to WG profile management.
 */
interface IWGProfileViewModel {

    /** StateFlow emitting the current UI state of the WG profile screen */
    val uiState: StateFlow<WGProfileUiState>

    /** StateFlow emitting error messages to be displayed in the UI */
    val errorMessage: StateFlow<String?>

    /** StateFlow indicating if a loading operation is in progress */
    val isLoading: StateFlow<Boolean>

    /** Loads the WG's core data including name, picture, etc. */
    fun loadWGData()

    /** Loads the list of members belonging to the WG */
    fun loadWGMembers()

    /** Shows the invitation dialog to invite new members */
    fun showInvitationDialog()

    /** Hides the invitation dialog */
    fun hideInvitationDialog()

    /** Called when a user is selected from the members list */
    fun onUserSelected(userId: String)

    /** Clears the currently selected user */
    fun clearSelectedUser()

    /** Toggles the edit mode for WG name or profile */
    fun toggleEditMode()

    /** Updates the WG name during editing */
    fun onWGNameChanged(newName: String)

    /** Saves the changes made to the WG name */
    fun saveEditing()

//    /** Removes the WG's profile picture */
//    fun onRemoveProfilePicture()
//
//    /** Updates the WG profile picture with a new image URI */
//    fun onNewProfilePictureSelected(uri: Uri)

    /** Toggles the editing state (alternative to toggleEditMode) */
    fun toggleEditing()

    /** Undoes any unsaved edits and reloads data */
    fun undoEdits()

    /** Removes a user from the WG by their userId */
    fun removeUserFromWG(userId: String)

    /** Handles navigation back action */
    fun navigateBack(navController: NavController)
}
