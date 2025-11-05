package com.wgeplant.ui.toDo

import androidx.navigation.NavController
import com.wgeplant.model.domain.Task
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface that defines the contract for the To-Do screen ViewModel.
 *
 * Exposes UI state and navigation/event methods required for interaction
 * with the To-Do list and its related features.
 */
interface IToDoViewModel {

    /** Holds the current UI state of the To-Do screen */
    val uiState: StateFlow<ToDoUiState>

    /** Emits the current error message, if any */
    val errorMessage: StateFlow<String?>

    /** Indicates whether a loading operation is in progress */
    val isLoading: StateFlow<Boolean>

    /**
     * Toggles the completion state of a given task.
     *
     * @param task The task whose state should be updated
     */
    fun changeTaskState(task: Task)

    /**
     * Navigates to the task creation screen.
     *
     * @param navController NavController used to perform navigation
     */
    fun navigateToTaskCreation(navController: NavController)

    /**
     * Navigates to the task detail or edit screen for the given task.
     *
     * @param task The task to be viewed or edited
     * @param navController NavController used to perform navigation
     */
    fun navigateToTask(task: Task, navController: NavController)

    /**
     * Navigates to the calendar screen.
     *
     * @param navController NavController used to perform navigation
     */
    fun navigateToCalendar(navController: NavController)

    /**
     * Navigates to the WG profile screen.
     *
     * @param navController NavController used to perform navigation
     */
    fun navigateToWGProfile(navController: NavController)
}
