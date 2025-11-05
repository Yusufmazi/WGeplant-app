package com.wgeplant.ui.calendar.entry

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.calendarManagement.CreateAppointmentInput
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.calendarManagement.ManageAppointmentInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel responsible for managing the UI state and logic of the appointment creation/editing screen.
 *
 * Implements [IAppointmentViewModel] and uses injected interactors to handle appointment
 * data, member information, and state restoration from navigation.
 *
 * Responsibilities include:
 * - Loading and initializing appointment data
 * - Handling form input and validation
 * - Executing creation, update, and deletion operations
 * - Managing navigation and undo behavior
 *
 * @param model Domain interactor for creating, editing, and deleting appointments.
 * @param manageWGProfileInteractor Interactor for fetching WG member data.
 * @param getCalendarDataInteractor Interactor for retrieving specific appointment data.
 * @param savedStateHandle Used to extract the appointment ID argument from navigation.
 */
@HiltViewModel
class AppointmentViewModel @Inject constructor(
    model: ManageAppointmentInteractor,
    private val manageWGProfileInteractor: ManageWGProfileInteractor,
    private val getCalendarDataInteractor: GetCalendarDataInteractor,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<AppointmentUiState, ManageAppointmentInteractor>(AppointmentUiState(), model), IAppointmentViewModel {

    companion object {
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
        const val INVALID_INPUT_ERROR = "Ändere bitte deine Eingaben."
        const val FIELD_EMPTY = "Fülle dieses Feld aus."
        const val DATE_PAST = "Datum liegt in der Vergangenheit."
        const val END_BEFORE_START = "Enddatum liegt vor Startdatum."
        const val END_EQUALS_START = "Start- und Endzeit sind identisch."
        const val NO_USERS_SELECTED = "Wähle mindestens einen Nutzer aus."
    }

    private var appointmentObservationJob: Job? = null
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
            val appointmentId: String? = savedStateHandle[Routes.APPOINTMENT_ID_ARG]
            if (appointmentId != null) {
                loadAppointment(appointmentId)
                observeAppointment(appointmentId)
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

    private suspend fun loadAppointment(appointmentId: String) {
        try {
            when (val appointmentResult = getCalendarDataInteractor.getAppointment(appointmentId).first()) {
                is Result.Success -> {
                    val appointment = appointmentResult.data
                    updateUiState {
                        it.copy(
                            id = appointment.appointmentId,
                            title = appointment.title,
                            startDate = appointment.startDate,
                            endDate = appointment.endDate,
                            description = appointment.description ?: "",
                            color = appointment.color
                        )
                    }
                    val currentMembers = uiState.value.wgMembers
                    val updatedMembers = currentMembers.map { member ->
                        member.copy(isSelected = appointment.affectedUsers.contains(member.id))
                    }
                    updateUiState { it.copy(wgMembers = updatedMembers) }
                }

                is Result.Error -> {
                    handleDomainError(appointmentResult.error)
                }
            }
        } catch (e: Exception) {
            showError(UNEXPECTED_ERROR)
        }
    }

    private fun observeAppointment(appointmentId: String) {
        appointmentObservationJob?.cancel()
        appointmentObservationJob = viewModelScope.launch {
            getCalendarDataInteractor.getAppointment(appointmentId)
                .distinctUntilChanged()
                .catch {
                    showError(UNEXPECTED_ERROR)
                }
                .collect { appointmentResult ->
                    when (appointmentResult) {
                        is Result.Success -> {
                            if (!uiState.value.isEditing) {
                                val appointment = appointmentResult.data
                                updateUiState {
                                    it.copy(
                                        id = appointment.appointmentId,
                                        title = appointment.title,
                                        startDate = appointment.startDate,
                                        endDate = appointment.endDate,
                                        description = appointment.description ?: "",
                                        color = appointment.color
                                    )
                                }
                                val currentMembers = uiState.value.wgMembers
                                val updatedMembers = currentMembers.map { member ->
                                    member.copy(isSelected = appointment.affectedUsers.contains(member.id))
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
                    showError(UNEXPECTED_ERROR)
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

    override fun onStartDateChanged(newStartDate: LocalDateTime) {
        updateUiState { it.copy(startDate = newStartDate, startDateError = null) }
    }

    override fun onEndDateChanged(newEndDate: LocalDateTime) {
        updateUiState { it.copy(endDate = newEndDate, endDateError = null) }
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

    // ------ Validation Methods ------

    private fun validateTitle(title: String) {
        val error = when {
            title.isBlank() -> FIELD_EMPTY
            else -> null
        }
        updateUiState { it.copy(titleError = error) }
    }

    private fun validateDates(startDate: LocalDateTime?, endDate: LocalDateTime?) {
        val now = LocalDateTime.now()
        val startDateError = when {
            startDate == null -> FIELD_EMPTY
            startDate.isBefore(now) -> DATE_PAST
            else -> null
        }
        val endDateError = when {
            endDate == null -> FIELD_EMPTY
            endDate.isBefore(now) -> DATE_PAST
            endDate.isBefore(startDate) -> END_BEFORE_START
            endDate.isEqual(startDate) -> END_EQUALS_START
            else -> null
        }

        updateUiState { it.copy(startDateError = startDateError, endDateError = endDateError) }
    }

    private fun validateAffectedUsers(wgMembers: List<WGMemberSelection>) {
        val error = when {
            wgMembers.none { it.isSelected } -> NO_USERS_SELECTED
            else -> null
        }
        updateUiState { it.copy(affectedUsersError = error) }
    }

    private fun validate() {
        val currentUiState = uiState.value
        val isValid = currentUiState.titleError == null &&
            currentUiState.startDateError == null &&
            currentUiState.endDateError == null &&
            currentUiState.affectedUsersError == null
        updateUiState { it.copy(isValid = isValid) }
    }

    // ----- Data Submission Logic -----

    override fun saveEntry(navController: NavController) {
        validateTitle(uiState.value.title)
        validateDates(uiState.value.startDate, uiState.value.endDate)
        validateAffectedUsers(uiState.value.wgMembers)
        validate()

        viewModelScope.launch {
            setLoading(true)
            clearError()

            val currentState = uiState.value

            if (!currentState.isValid) {
                showError(INVALID_INPUT_ERROR)
                setLoading(false)
                return@launch
            }

            val input = CreateAppointmentInput(
                title = currentState.title,
                startDate = currentState.startDate!!,
                endDate = currentState.endDate!!,
                affectedUsers = currentState.wgMembers.filter { it.isSelected }.map { it.id },
                color = currentState.color,
                description = currentState.description
            )

            val appointmentId = currentState.id

            try {
                if (appointmentId != null) {
                    when (val result = model.executeEditing(appointmentId, input)) {
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

            val appointmentId = uiState.value.id
            if (appointmentId != null) {
                loadAppointment(appointmentId)
                updateUiState { it.copy(isEditing = false) }
            }
            setLoading(false)
        }
    }

    override fun navigateToOtherEntryCreation(navController: NavController) {
        navController.navigate(Routes.CREATE_TASK) {
            popUpTo(Routes.CREATE_APPOINTMENT) { inclusive = true }
        }
    }

    override fun delete(navController: NavController) {
        val appointmentId = uiState.value.id
        if (appointmentId != null) {
            deleteAppointment(appointmentId, navController)
        }
    }

    private fun deleteAppointment(appointmentId: String, navController: NavController) {
        viewModelScope.launch {
            setLoading(true)
            clearError()

            try {
                when (val result = model.executeDeletion(appointmentId)) {
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

    override fun setEditMode() {
        updateUiState { it.copy(isEditing = true) }
    }
}
