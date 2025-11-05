package com.wgeplant.ui.user

import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/**
 * This interface is responsible for managing the logic and state of the user profile screen.
 */
interface IUserProfileViewModel {

    /** Represents the current state of the user profile UI. */
    val uiState: StateFlow<UserProfileUiState>

    /** Emits error messages to be displayed in the UI. */
    val errorMessage: StateFlow<String?>

    /** Indicates whether a loading operation is currently active. */
    val isLoading: StateFlow<Boolean>

    /**
     * Navigates back to the previous screen.
     * @param navController: The navigation controller used to go back.
     */
    fun navigateBack(navController: NavController)

    /** Opens the absence creation or editing dialog. */
    fun openAbsenceDialog()

    /** Closes the absence dialog. */
    fun closeAbsenceDialog()

    /**
     * Opens a confirmation dialog with a specific title, message and confirm action.
     * @param title: The title of the dialog.
     * @param message: The message shown in the dialog.
     * @param onConfirm: The action to perform if confirmed.
     */
    fun openConfirmDialog(title: String, message: String, onConfirm: () -> Unit)

    /** Closes the currently shown confirmation dialog. */
    fun closeConfirmDialog()

    /** Executes the action associated with the confirmation dialog. */
    fun onConfirmAction()

    /** Indicates whether the confirmation dialog is visible. */
    val showConfirmDialog: Boolean

    /** The title currently set for the confirmation dialog. */
    val confirmDialogTitle: String

    /** The message currently set for the confirmation dialog. */
    val confirmDialogMessage: String

    /** Indicates whether the absence dialog is visible. */
    val showAbsenceDialog: Boolean

    /**
     * Updates the display name while editing.
     * @param newName: The new name input by the user.
     */
    fun onDisplayNameChanged(newName: String)

//    /**
//     * Changes the profile picture of the user.
//     * @param uri: The URI of the new profile picture.
//     */
//    fun changeProfilePicture(uri: Uri)
//
//    /** Removes the current profile picture of the user. */
//    fun onRemoveProfilePicture()

    /** Loads the user profile data. */
    fun loadUserProfile()

    /** Logs out the currently signed-in user. */
    fun logout()

    /**
     * Deletes the user account and navigates accordingly.
     * @param navController: Used for navigation after deletion.
     */
    fun deleteAccount(navController: NavController)

    /** Removes the user from the current WG (shared flat). */
    fun leaveWG()

    /**
     * Adds a new absence entry.
     * @param startDate: The start date in ISO-8601 format (yyyy-MM-dd).
     * @param endDate: The end date in ISO-8601 format (yyyy-MM-dd).
     */
    fun addAbsence(startDate: String, endDate: String)

    /**
     * Edits an existing absence entry.
     * @param absenceId: The ID of the absence to be updated.
     * @param startDate: The new start date.
     * @param endDate: The new end date.
     */
    fun editAbsence(absenceId: String, startDate: LocalDate, endDate: LocalDate)

    /**
     * Deletes a specific absence entry.
     * @param absenceId: The ID of the absence to be deleted.
     */
    fun deleteAbsence(absenceId: String)

    /** Enables or disables profile editing mode. */
    fun toggleEdit()

    /** Saves any changes made during editing. */
    fun saveEditing()

    /** Reverts any unsaved changes made during editing. */
    fun undoEdits()
}
