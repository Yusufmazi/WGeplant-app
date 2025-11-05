package com.wgeplant.viewmodel.calendar.entry

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.interactor.calendarManagement.CreateAppointmentInput
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.calendarManagement.ManageAppointmentInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.calendar.entry.AppointmentViewModel
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.ui.theme.EventColors
import com.wgeplant.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyNoInteractions
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class AppointmentViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockManageAppointmentInteractor: ManageAppointmentInteractor

    @Mock
    private lateinit var mockManageWGProfileInteractor: ManageWGProfileInteractor

    @Mock
    private lateinit var mockGetCalendarDataInteractor: GetCalendarDataInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    // load data tests

    @Test
    fun `ViewModel loads WG members on initialization`() = runTest {
        savedStateHandle = SavedStateHandle()
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.wgMembers.size)
        assertEquals("Member1", uiState.wgMembers[0].name)
        assertEquals("Member2", uiState.wgMembers[1].name)
        assertFalse(uiState.wgMembers[0].isSelected)
        assertFalse(uiState.wgMembers[1].isSelected)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)

        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    @Test
    fun `ViewModel handles domain error when loading WG members`() = runTest {
        savedStateHandle = SavedStateHandle()

        val domainError = DomainError.NetworkError
        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Error(domainError)))

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.wgMembers.size)
        assertFalse(viewModel.isLoading.value)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    @Test
    fun `ViewModel handles unexpected exception in flow when loading WG members`() = runTest {
        savedStateHandle = SavedStateHandle()

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(
            flow { throw RuntimeException("Netzwerkfehler") }
        )

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.wgMembers.size)
        assertFalse(viewModel.isLoading.value)
        assertEquals(AppointmentViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)

        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    @Test
    fun `ViewModel loads appointment data on initialization`() = runTest {
        val appointmentId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.APPOINTMENT_ID_ARG to appointmentId))
        val mockAppointment = Appointment(
            appointmentId = appointmentId,
            title = "Test Appointment",
            startDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            endDate = LocalDateTime.of(2025, 1, 1, 13, 0),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = "This is a test appointment"
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg"),
            User(userId = "3", displayName = "Member3", profilePicture = "http://example.com/member3.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getAppointment(appointmentId)).thenReturn(
            flowOf(Result.Success(mockAppointment))
        )

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(appointmentId, uiState.id)
        assertEquals("Test Appointment", uiState.title)
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0), uiState.startDate)
        assertEquals(LocalDateTime.of(2025, 1, 1, 13, 0), uiState.endDate)
        assertEquals(Color.Blue, uiState.color)
        assertEquals("This is a test appointment", uiState.description)
        assertTrue(uiState.wgMembers[0].isSelected)
        assertTrue(uiState.wgMembers[1].isSelected)
        assertFalse(uiState.wgMembers[2].isSelected)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)

        verify(mockGetCalendarDataInteractor, times(2)).getAppointment(appointmentId)
    }

    @Test
    fun `ViewModel handles domain error when loading appointment data`() = runTest {
        val appointmentId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.APPOINTMENT_ID_ARG to appointmentId))

        val domainError = DomainError.NetworkError
        `when`(mockGetCalendarDataInteractor.getAppointment(appointmentId)).thenReturn(
            flowOf(Result.Error(domainError))
        )

        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg"),
            User(userId = "3", displayName = "Member3", profilePicture = "http://example.com/member3.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNull(uiState.id)
        assertEquals("", uiState.title)
        assertNull(uiState.startDate)
        assertNull(uiState.endDate)
        assertEquals(EventColors.defaultEventColor, uiState.color)
        assertEquals("", uiState.description)
        assertFalse(uiState.wgMembers[0].isSelected)
        assertFalse(uiState.wgMembers[1].isSelected)
        assertFalse(viewModel.isLoading.value)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockGetCalendarDataInteractor, times(2)).getAppointment(appointmentId)
        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    @Test
    fun `ViewModel handles unexpected exception in flow when loading appointment data`() = runTest {
        val appointmentId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.APPOINTMENT_ID_ARG to appointmentId))

        `when`(mockGetCalendarDataInteractor.getAppointment(appointmentId)).thenReturn(
            flow { throw RuntimeException("Netzwerkfehler") }
        )

        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg"),
            User(userId = "3", displayName = "Member3", profilePicture = "http://example.com/member3.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNull(uiState.id)
        assertEquals("", uiState.title)
        assertNull(uiState.startDate)
        assertNull(uiState.endDate)
        assertEquals(EventColors.defaultEventColor, uiState.color)
        assertEquals("", uiState.description)
        assertFalse(uiState.wgMembers[0].isSelected)
        assertFalse(uiState.wgMembers[1].isSelected)
        assertFalse(viewModel.isLoading.value)
        assertEquals(AppointmentViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)

        verify(mockGetCalendarDataInteractor, times(2)).getAppointment(appointmentId)
        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    // input changes tests

    @Test
    fun `onTitleChanged updates title and clears error`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.onTitleChanged("")
        viewModel.saveEntry(mockNavController)
        assertNotNull(viewModel.uiState.value.titleError)

        viewModel.onTitleChanged("New Title")
        assertEquals("New Title", viewModel.uiState.value.title)
        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `onStartDateChanged updates startDate and clears error`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.saveEntry(mockNavController)
        assertNotNull(viewModel.uiState.value.startDateError)

        val newStartDate = LocalDateTime.now().plusDays(1)

        viewModel.onStartDateChanged(newStartDate)
        assertEquals(newStartDate, viewModel.uiState.value.startDate)
        assertNull(viewModel.uiState.value.startDateError)
    }

    @Test
    fun `onEndDateChanged updates endDate and clears error`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.saveEntry(mockNavController)
        assertNotNull(viewModel.uiState.value.endDateError)

        val newEndDate = LocalDateTime.now().plusDays(1)

        viewModel.onEndDateChanged(newEndDate)
        assertEquals(newEndDate, viewModel.uiState.value.endDate)
        assertNull(viewModel.uiState.value.endDateError)
    }

    @Test
    fun `onAssignmentChanged correctly selects and deselects members`() = runTest {
        savedStateHandle = SavedStateHandle()
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.onAssignmentChanged("1", true)
        assertTrue(viewModel.uiState.value.wgMembers[0].isSelected)
        assertFalse(viewModel.uiState.value.wgMembers[1].isSelected)

        viewModel.onAssignmentChanged("1", false)
        assertFalse(viewModel.uiState.value.wgMembers[0].isSelected)
        assertFalse(viewModel.uiState.value.wgMembers[1].isSelected)
    }

    @Test
    fun `onColorChanged updates color`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        val newColor = Color.Red
        viewModel.onColorChanged(newColor)
        assertEquals(newColor, viewModel.uiState.value.color)
    }

    @Test
    fun `onDescriptionChanged updates description`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        val newDescription = "New Description"
        viewModel.onDescriptionChanged(newDescription)
        assertEquals(newDescription, viewModel.uiState.value.description)
    }

    // input validation tests

    @Test
    fun `error for blank input fields when saveEntry is called`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(AppointmentViewModel.FIELD_EMPTY, state.titleError)
        assertEquals(AppointmentViewModel.FIELD_EMPTY, state.startDateError)
        assertEquals(AppointmentViewModel.FIELD_EMPTY, state.endDateError)
    }

    @Test
    fun `error for invalid dates when saveEntry is called`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.onStartDateChanged(LocalDateTime.now().minusDays(2))
        viewModel.onEndDateChanged(LocalDateTime.now().minusDays(1))

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(AppointmentViewModel.DATE_PAST, state.value.startDateError)
        assertEquals(AppointmentViewModel.DATE_PAST, state.value.endDateError)

        viewModel.onStartDateChanged(LocalDateTime.now().plusDays(2))
        viewModel.onEndDateChanged(LocalDateTime.now().plusDays(1))

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(AppointmentViewModel.END_BEFORE_START, state.value.endDateError)

        val newDate = LocalDateTime.now().plusHours(1)

        viewModel.onStartDateChanged(newDate)
        viewModel.onEndDateChanged(newDate)

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(AppointmentViewModel.END_EQUALS_START, state.value.endDateError)
    }

    @Test
    fun `error if no members are selected when saveEntry is called`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(AppointmentViewModel.NO_USERS_SELECTED, state.value.affectedUsersError)
    }

    // saveEntry() Tests

    @Test
    fun `saveEntry with invalid input does not save and shows error`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.errorMessage.value)

        verifyNoInteractions(mockManageAppointmentInteractor)
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `saveEntry with valid input saves new appointment and navigates back on success`() = runTest {
        savedStateHandle = SavedStateHandle()
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.onTitleChanged("Test Title")
        val startDate = LocalDateTime.now().plusDays(1)
        val endDate = startDate.plusHours(1)
        viewModel.onStartDateChanged(startDate)
        viewModel.onEndDateChanged(endDate)
        viewModel.onColorChanged(Color.Red)
        viewModel.onDescriptionChanged("Test Description")
        viewModel.onAssignmentChanged("1", false)
        viewModel.onAssignmentChanged("2", true)

        `when`(mockManageAppointmentInteractor.executeCreation(any())).thenReturn(Result.Success(Unit))
        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        val expectedInput = CreateAppointmentInput(
            title = "Test Title",
            startDate = startDate,
            endDate = endDate,
            color = Color.Red,
            description = "Test Description",
            affectedUsers = listOf("2")
        )
        verify(mockManageAppointmentInteractor).executeCreation(expectedInput)
        verify(mockNavController).popBackStack()
        verify(mockManageAppointmentInteractor, never()).executeEditing(anyString(), any())
    }

    @Test
    fun `saveEntry handles interactor error and shows error message on creation failure`() = runTest {
        savedStateHandle = SavedStateHandle()

        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.onTitleChanged("Test Title")
        val startDate = LocalDateTime.now().plusDays(1)
        val endDate = startDate.plusHours(1)
        viewModel.onStartDateChanged(startDate)
        viewModel.onEndDateChanged(endDate)
        viewModel.onColorChanged(Color.Red)
        viewModel.onDescriptionChanged("Test Description")
        viewModel.onAssignmentChanged("1", false)
        viewModel.onAssignmentChanged("2", true)

        val domainError = DomainError.NetworkError
        `when`(mockManageAppointmentInteractor.executeCreation(any())).thenReturn(Result.Error(domainError))

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageAppointmentInteractor).executeCreation(any())
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `saveEntry edits existing appointment on success`() = runTest {
        val appointmentId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.APPOINTMENT_ID_ARG to appointmentId))
        val mockAppointment = Appointment(
            appointmentId = appointmentId,
            title = "Test Appointment",
            startDate = LocalDateTime.now().plusDays(1),
            endDate = LocalDateTime.now().plusDays(1).plusHours(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = ""
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getAppointment(appointmentId)).thenReturn(
            flowOf(Result.Success(mockAppointment))
        )

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val newTitle = "Updated Test Appointment"
        val newStartDate = LocalDateTime.now().plusDays(5)
        val newEndDate = newStartDate.plusHours(1)
        val newColor = Color.Red
        val newDescription = "This is a test appointment"

        viewModel.onTitleChanged(newTitle)
        viewModel.onStartDateChanged(newStartDate)
        viewModel.onEndDateChanged(newEndDate)
        viewModel.onColorChanged(newColor)
        viewModel.onAssignmentChanged("2", false)
        viewModel.onDescriptionChanged(newDescription)

        `when`(mockManageAppointmentInteractor.executeEditing(anyString(), any())).thenReturn(Result.Success(Unit))
        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        val expectedInput = CreateAppointmentInput(
            title = newTitle,
            startDate = newStartDate,
            endDate = newEndDate,
            color = newColor,
            description = newDescription,
            affectedUsers = listOf("1")
        )
        verify(mockManageAppointmentInteractor).executeEditing(appointmentId, expectedInput)
        verify(mockNavController, never()).popBackStack()
        verify(mockManageAppointmentInteractor, never()).executeCreation(any())
    }

    @Test
    fun `saveEntry handles interactor error and shows error message on editing failure`() = runTest {
        val appointmentId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.APPOINTMENT_ID_ARG to appointmentId))
        val mockAppointment = Appointment(
            appointmentId = appointmentId,
            title = "Test Appointment",
            startDate = LocalDateTime.now().plusDays(1),
            endDate = LocalDateTime.now().plusDays(1).plusHours(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = ""
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getAppointment(appointmentId)).thenReturn(
            flowOf(Result.Success(mockAppointment))
        )

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val domainError = DomainError.NetworkError
        `when`(mockManageAppointmentInteractor.executeEditing(anyString(), any())).thenReturn(Result.Error(domainError))

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageAppointmentInteractor).executeEditing(anyString(), any())
        verifyNoInteractions(mockNavController)
    }

    // delete() tests

    @Test
    fun `delete calls interactor and navigates back on success`() = runTest {
        val appointmentId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.APPOINTMENT_ID_ARG to appointmentId))
        val mockAppointment = Appointment(
            appointmentId = appointmentId,
            title = "Test Appointment",
            startDate = LocalDateTime.now().plusDays(1),
            endDate = LocalDateTime.now().plusDays(1).plusHours(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = ""
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getAppointment(appointmentId)).thenReturn(
            flowOf(Result.Success(mockAppointment))
        )

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        `when`(mockManageAppointmentInteractor.executeDeletion(anyString())).thenReturn(Result.Success(Unit))

        viewModel.delete(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)

        verify(mockManageAppointmentInteractor).executeDeletion(appointmentId)
        verify(mockNavController).popBackStack()
    }

    @Test
    fun `delete handles interactor error and shows error message`() = runTest {
        val appointmentId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.APPOINTMENT_ID_ARG to appointmentId))
        val mockAppointment = Appointment(
            appointmentId = appointmentId,
            title = "Test Appointment",
            startDate = LocalDateTime.now().plusDays(1),
            endDate = LocalDateTime.now().plusDays(1).plusHours(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = ""
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getAppointment(appointmentId)).thenReturn(
            flowOf(Result.Success(mockAppointment))
        )

        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val domainError = DomainError.NetworkError
        `when`(mockManageAppointmentInteractor.executeDeletion(anyString())).thenReturn(Result.Error(domainError))

        viewModel.delete(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageAppointmentInteractor).executeDeletion(appointmentId)
        verifyNoInteractions(mockNavController)
    }

    // navigation tests

    @Test
    fun `navigateToOtherEntryCreation navigates to create task`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.navigateToOtherEntryCreation(mockNavController)
        verify(mockNavController).navigate(eq(Routes.CREATE_TASK), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `navigateBack calls popBackStack on navController`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = AppointmentViewModel(
            mockManageAppointmentInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.navigateBack(mockNavController)
        verify(mockNavController).popBackStack()
    }
}
