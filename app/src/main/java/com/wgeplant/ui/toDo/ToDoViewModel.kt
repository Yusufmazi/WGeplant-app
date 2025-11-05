package com.wgeplant.ui.toDo

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_ERROR_MESSAGE = "Ein unerwarteter Fehler ist aufgetreten."

/**
 * Handles logic and state for the To-Do screen.
 */

@HiltViewModel
class ToDoViewModel @Inject constructor(
    model: GetCalendarDataInteractor,
    private val manageWGProfileInteractor: ManageWGProfileInteractor
) : BaseViewModel<ToDoUiState, GetCalendarDataInteractor>(ToDoUiState(), model), IToDoViewModel {

    private var taskDataObservationJob: Job? = null
    private var profilePictureObservationJob: Job? = null

    init {
        observeWgProfilePicture()
        observeTaskData()
    }

    /**
     * Observes WG profile picture updates.
     * Updates the UI state with the latest WG profile image.
     */
    private fun observeWgProfilePicture() {
        profilePictureObservationJob?.cancel()

        profilePictureObservationJob = viewModelScope.launch {
            setLoading(true)

            manageWGProfileInteractor.getWGData()
                .distinctUntilChanged()
                .catch { exception ->
                    showError(exception.message ?: DEFAULT_ERROR_MESSAGE)
                    updateUiState { it.copy(wgProfileImageUrl = null) }
                    setLoading(false)
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val wg = result.data
                            updateUiState { it.copy(wgProfileImageUrl = wg?.profilePicture) }
                        }

                        is Result.Error -> {
                            handleDomainError(result.error)
                            updateUiState { it.copy(wgProfileImageUrl = null) }
                        }
                    }
                    setLoading(false)
                }
        }
    }

    /**
     * Observes the task list and updates UI state accordingly.
     */
    private fun observeTaskData() {
        taskDataObservationJob?.cancel()

        taskDataObservationJob = viewModelScope.launch {
            setLoading(true)

            model.getTaskList()
                .distinctUntilChanged()
                .catch { exception ->
                    showError(exception.message ?: DEFAULT_ERROR_MESSAGE)
                    updateUiState { it.copy(tasks = emptyList()) }
                    setLoading(false)
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Sort tasks by date
                            updateUiState {
                                it.copy(tasks = result.data.sortedBy { task -> task.date })
                            }
                        }

                        is Result.Error -> {
                            handleDomainError(result.error)
                            updateUiState { it.copy(tasks = emptyList()) }
                        }
                    }
                    setLoading(false)
                }
        }
    }

    /**
     * Toggles the completion state of the given task.
     *
     * @param task The task to update
     */
    override fun changeTaskState(task: Task) {
        viewModelScope.launch {
            val result = task.taskId?.let { model.changeTaskState(it) }
            if (result is Result.Error) {
                handleDomainError(result.error)
            }
        }
    }

    /**
     * Navigates to the task creation screen.
     */
    override fun navigateToTaskCreation(navController: NavController) {
        navController.navigate(Routes.CREATE_TASK)
    }

    /**
     * Navigates to the task detail screen.
     *
     * @param task The task to view/edit
     */
    override fun navigateToTask(task: Task, navController: NavController) {
        navController.navigate(Routes.getTaskRoute(task.taskId!!))
    }

    /**
     * Navigates to the calendar screen.
     */
    override fun navigateToCalendar(navController: NavController) {
        navController.navigate(Routes.CALENDAR_GRAPH)
    }

    /**
     * Navigates to the WG profile screen.
     */
    override fun navigateToWGProfile(navController: NavController) {
        navController.navigate(Routes.PROFILE_WG)
    }
}
