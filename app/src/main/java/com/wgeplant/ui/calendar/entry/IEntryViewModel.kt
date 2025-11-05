package com.wgeplant.ui.calendar.entry

import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow

/**
 * Generic interface for entry ViewModels that manage UI state and user interactions
 * for editable content such as appointments or tasks.
 *
 * @param T The type of UI state managed by the ViewModel.
 */
interface IEntryViewModel<T> {

    /** Observable UI state for the entry screen. */
    val uiState: StateFlow<T>

    /** An optional error message to be shown in the UI. */
    val errorMessage: StateFlow<String?>

    /** Indicates whether a loading process is currently running. */
    val isLoading: StateFlow<Boolean>

    /**
     * Called when the title input changes.
     * @param newTitle The updated title value.
     */
    fun onTitleChanged(newTitle: String)

    /**
     * Called when a member is (de)selected for assignment.
     * @param memberId The ID of the member.
     * @param isSelected True if selected, false otherwise.
     */
    fun onAssignmentChanged(memberId: String, isSelected: Boolean)

    /**
     * Called when the color selection changes.
     * @param newColor The newly selected color.
     */
    fun onColorChanged(newColor: Color)

    /**
     * Called when the description input changes.
     * @param newDescription The updated description value.
     */
    fun onDescriptionChanged(newDescription: String)

    /**
     * Triggers saving the current entry.
     * @param navController The NavController used to handle navigation after saving.
     */
    fun saveEntry(navController: NavController)

    /** Cancels and reverts unsaved changes. */
    fun undoEdits()

    /**
     * Deletes the current entry.
     * @param navController The NavController used to handle navigation after deletion.
     */
    fun delete(navController: NavController)

    /**
     * Navigates to the other type of entry creation screen (e.g., task ↔ appointment).
     * @param navController The NavController used for navigation.
     */
    fun navigateToOtherEntryCreation(navController: NavController)

    /**
     * Navigates back to the previous screen.
     * @param navController The NavController used for navigation.
     */
    fun navigateBack(navController: NavController)

    /**
     * Sets the entry in edit mode.
     */
    fun setEditMode()
}
