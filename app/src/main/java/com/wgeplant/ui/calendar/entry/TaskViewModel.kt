package com.wgeplant.ui.calendar.entry

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.calendarManagement.CreateTaskInput
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.calendarManagement.ManageTaskInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel responsible for managing the UI state and logic of the task creation/editing screen.
 *
 * Implements [ITaskViewModel] and uses injected interactors to handle task
 * data, WG member information, and state restoration from navigation.
 *
 * Responsibilities include:
 * - Loading and initializing task data and WG members
 * - Handling form input changes and validation
 * - Executing creation, update, and deletion operations
 * - Managing navigation and undo behavior
 *
 * @param model Domain interactor for creating, editing, and deleting tasks.
 * @param manageWGProfileInteractor Interactor for fetching WG member data.
 * @param getCalendarDataInteractor Interactor for retrieving specific task data.
 * @param savedStateHandle Used to extract the task ID argument from navigation.
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    model: ManageTaskInteractor,
    private val manageWGProfileInteractor: ManageWGProfileInteractor,
    private val getCalendarDataInteractor: GetCalendarDataInteractor,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TaskUiState, ManageTaskInteractor>(TaskUiState(), model), ITaskViewModel {

    companion object {
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
        const val FIELD_REQUIRED = "Fülle dieses Feld aus."
        const val DATE_IN_PAST = "Datum liegt in der Vergangenheit."
        const val SELECT_AT_LEAST_ONE_USER = "Wähle mindestens einen Nutzer aus."
        const val CHANGE_INPUT = "Ändere bitte deine Eingaben."
    }

    private var taskObservationJob: Job? = null
    private var wgMembersObservationJob: Job? = null

    init {
        viewModelScope.launch {
            setLoading(true)
            clearError()

            loadWgMembers()
            observeWGMembers()

            if (uiState.value.wgMembers.isEmpty() || errorMessage.value != null) {
                setLoading(false)
                return@launch
            }

            val taskId: String? = savedStateHandle[Routes.TASK_ID_ARG]

            if (taskId != null) {
                loadTask(taskId)
                observeTask(taskId)
            }
            setLoading(false)
        }
    }

    private suspend fun loadWgMembers() {
        try {
            when (val membersResult = manageWGProfileInteractor.getWGMembers().first()) {
                is Result.Success -> {
                    val members = membersResult.data
                    updateUiState {
                        it.copy(
                            wgMembers = members.map { member ->
                                WGMemberSelection(member.userId, member.displayName, false)
                            }
                        )
                    }
                }

                is Result.Error -> {
                    handleDomainError(membersResult.error)
                }
            }
        } catch (e: Exception) {
            showError(UNEXPECTED_ERROR)
        }
    }

    private suspend fun loadTask(taskId: String) {
        try {
            when (val taskResult = getCalendarDataInteractor.getTask(taskId).first()) {
                is Result.Success -> {
                    val task = taskResult.data
                    updateUiState {
                        it.copy(
                            id = task.taskId,
                            title = task.title,
                            date = task.date,
                            description = task.description ?: "",
                            color = task.color
                        )
                    }
                    val currentMembers = uiState.value.wgMembers
                    val updatedMembers = currentMembers.map { member ->
                        member.copy(isSelected = task.affectedUsers.contains(member.id))
                    }
                    updateUiState { it.copy(wgMembers = updatedMembers) }
                }

                is Result.Error -> {
                    handleDomainError(taskResult.error)
                }
            }
        } catch (e: Exception) {
            showError(UNEXPECTED_ERROR)
        }
    }

    private fun observeTask(taskId: String) {
        taskObservationJob?.cancel()
        taskObservationJob = viewModelScope.launch {
            getCalendarDataInteractor.getTask(taskId)
                .distinctUntilChanged()
                .catch {
                    showError(UNEXPECTED_ERROR)
                }
                .collect { taskResult ->
                    when (taskResult) {
                        is Result.Success -> {
                            if (!uiState.value.isEditing) {
                                val task = taskResult.data
                                updateUiState {
                                    it.copy(
                                        id = task.taskId,
                                        title = task.title,
                                        date = task.date,
                                        description = task.description ?: "",
                                        color = task.color
                                    )
                                }
                                val currentMembers = uiState.value.wgMembers
                                val updatedMembers = currentMembers.map { member ->
                                    member.copy(isSelected = task.affectedUsers.contains(member.id))
                                }
                                updateUiState { it.copy(wgMembers = updatedMembers) }
                            }
                        }
                        is Result.Error -> {
                            updateUiState { it.copy(isExisting = false) }
                        }
                    }
                }
        }
    }

    private fun observeWGMembers() {
        wgMembersObservationJob?.cancel()
        wgMembersObservationJob = viewModelScope.launch {
            manageWGProfileInteractor.getWGMembers()
                .distinctUntilChanged()
                .catch {
                    showError(AppointmentViewModel.UNEXPECTED_ERROR)
                }
                .collect { membersResult ->
                    when (membersResult) {
                        is Result.Success -> {
                            val newMembers = membersResult.data
                            val updatedMembers = newMembers.map { newMember ->
                                val existingMember = uiState.value.wgMembers.find { it.id == newMember.userId }
                                WGMemberSelection(
                                    newMember.userId,
                                    newMember.displayName,
                                    existingMember?.isSelected ?: false
                                )
                            }
                            updateUiState {
                                it.copy(
                                    wgMembers = updatedMembers
                                )
                            }
                        }
                        is Result.Error -> {
                            handleDomainError(membersResult.error)
                        }
                    }
                }
        }
    }

    // ----- Input Change Handlers -----

    override fun onTitleChanged(newTitle: String) {
        updateUiState { it.copy(title = newTitle, titleError = null) }
    }

    override fun onDateChanged(newDate: LocalDate?) {
        updateUiState { it.copy(date = newDate, dateError = null) }
    }

    override fun onAssignmentChanged(memberId: String, isSelected: Boolean) {
        val updatedMembers = uiState.value.wgMembers.map { member ->
            if (member.id == memberId) {
                member.copy(isSelected = isSelected)
            } else {
                member
            }
        }
        updateUiState { it.copy(wgMembers = updatedMembers) }
    }

    override fun onColorChanged(newColor: Color) {
        updateUiState { it.copy(color = newColor) }
    }

    override fun onDescriptionChanged(newDescription: String) {
        updateUiState { it.copy(description = newDescription) }
    }

    // ----- Validation Methods -----

    private fun validateTitle(title: String) {
        val error = when {
            title.isBlank() -> FIELD_REQUIRED
            else -> null
        }
        updateUiState { it.copy(titleError = error) }
    }

    private fun validateDate(date: LocalDate?) {
        val now = LocalDate.now()
        val error = when {
            date == null -> null
            date.isBefore(now) -> DATE_IN_PAST
            else -> null
        }
        updateUiState { it.copy(dateError = error) }
    }

    private fun validateAffectedUsers(wgMembers: List<WGMemberSelection>) {
        val error = when {
            wgMembers.none { it.isSelected } -> SELECT_AT_LEAST_ONE_USER
            else -> null
        }
        updateUiState { it.copy(affectedUsersError = error) }
    }

    private fun validate() {
        val currentUiState = uiState.value
        val isValid = currentUiState.titleError == null &&
            currentUiState.dateError == null &&
            currentUiState.affectedUsersError == null
        updateUiState { it.copy(isValid = isValid) }
    }

    // ----- Data Submission Logic -----

    override fun saveEntry(navController: NavController) {
        validateTitle(uiState.value.title)
        validateDate(uiState.value.date)
        validateAffectedUsers(uiState.value.wgMembers)
        validate()

        viewModelScope.launch {
            setLoading(true)
            clearError()

            val currentState = uiState.value

            if (!currentState.isValid) {
                showError(CHANGE_INPUT)
                setLoading(false)
                return@launch
            }

            val input = CreateTaskInput(
                title = currentState.title,
                date = currentState.date,
                affectedUsers = currentState.wgMembers.filter { it.isSelected }.map { it.id },
                color = currentState.color,
                description = currentState.description
            )

            val taskId = currentState.id

            try {
                if (taskId != null) {
                    when (val result = model.executeEditing(taskId, input)) {
                        is Result.Success -> { updateUiState { it.copy(isEditing = false) } }
                        is Result.Error -> {
                            handleDomainError(result.error)
                        }
                    }
                } else {
                    when (val result = model.executeCreation(input)) {
                        is Result.Success -> {
                            navigateBack(navController)
                        }

                        is Result.Error -> {
                            handleDomainError(result.error)
                        }
                    }
                }
            } catch (e: Exception) {
                showError(UNEXPECTED_ERROR)
            } finally {
                setLoading(false)
            }
        }
    }

    override fun undoEdits() {
        viewModelScope.launch {
            setLoading(true)
            clearError()

            val taskId = uiState.value.id
            if (taskId != null) {
                loadTask(taskId)
                updateUiState { it.copy(isEditing = false) }
            }
            setLoading(false)
        }
    }

    override fun navigateToOtherEntryCreation(navController: NavController) {
        navController.navigate(Routes.CREATE_APPOINTMENT) {
            popUpTo(Routes.CREATE_TASK) { inclusive = true }
        }
    }

    override fun setEditMode() {
        updateUiState { it.copy(isEditing = true) }
    }

    override fun delete(navController: NavController) {
        val taskId = uiState.value.id
        if (taskId != null) {
            deleteTask(taskId, navController)
        }
    }

    private fun deleteTask(taskId: String, navController: NavController) {
        viewModelScope.launch {
            setLoading(true)
            clearError()

            try {
                when (val result = model.executeDeletion(taskId)) {
                    is Result.Success -> {
                        navigateBack(navController)
                    }

                    is Result.Error -> {
                        handleDomainError(result.error)
                    }
                }
            } catch (e: Exception) {
                showError(UNEXPECTED_ERROR)
            } finally {
                setLoading(false)
            }
        }
    }
}
