package com.wgeplant.viewmodel.calendar.entry

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.domain.User
import com.wgeplant.model.interactor.calendarManagement.CreateTaskInput
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.calendarManagement.ManageTaskInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.calendar.entry.TaskViewModel
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
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class TaskViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockManageTaskInteractor: ManageTaskInteractor

    @Mock
    private lateinit var mockManageWGProfileInteractor: ManageWGProfileInteractor

    @Mock
    private lateinit var mockGetCalendarDataInteractor: GetCalendarDataInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: TaskViewModel
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

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
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

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
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

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.wgMembers.size)
        assertFalse(viewModel.isLoading.value)
        assertEquals(TaskViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)

        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    @Test
    fun `ViewModel loads task data on initialization`() = runTest {
        val taskId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.TASK_ID_ARG to taskId))
        val mockTask = Task(
            taskId = taskId,
            title = "Test Task",
            date = LocalDate.of(2025, 1, 1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = "This is a test task",
            stateOfTask = false
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg"),
            User(userId = "3", displayName = "Member3", profilePicture = "http://example.com/member3.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getTask(taskId)).thenReturn(flowOf(Result.Success(mockTask)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(taskId, uiState.id)
        assertEquals("Test Task", uiState.title)
        assertEquals(LocalDate.of(2025, 1, 1), uiState.date)
        assertEquals(Color.Blue, uiState.color)
        assertEquals("This is a test task", uiState.description)
        assertTrue(uiState.wgMembers[0].isSelected)
        assertTrue(uiState.wgMembers[1].isSelected)
        assertFalse(uiState.wgMembers[2].isSelected)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)

        verify(mockGetCalendarDataInteractor, times(2)).getTask(taskId)
    }

    @Test
    fun `ViewModel handles domain error when loading task data`() = runTest {
        val taskId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.TASK_ID_ARG to taskId))

        val domainError = DomainError.NetworkError
        `when`(mockGetCalendarDataInteractor.getTask(taskId)).thenReturn(flowOf(Result.Error(domainError)))

        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg"),
            User(userId = "3", displayName = "Member3", profilePicture = "http://example.com/member3.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNull(uiState.id)
        assertEquals("", uiState.title)
        assertNull(uiState.date)
        assertEquals(EventColors.defaultEventColor, uiState.color)
        assertEquals("", uiState.description)
        assertFalse(uiState.wgMembers[0].isSelected)
        assertFalse(uiState.wgMembers[1].isSelected)
        assertFalse(viewModel.isLoading.value)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockGetCalendarDataInteractor, times(2)).getTask(taskId)
        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    @Test
    fun `ViewModel handles unexpected exception in flow when loading task data`() = runTest {
        val taskId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.TASK_ID_ARG to taskId))

        `when`(mockGetCalendarDataInteractor.getTask(taskId)).thenReturn(
            flow { throw RuntimeException("Netzwerkfehler") }
        )

        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg"),
            User(userId = "3", displayName = "Member3", profilePicture = "http://example.com/member3.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNull(uiState.id)
        assertEquals("", uiState.title)
        assertNull(uiState.date)
        assertEquals(EventColors.defaultEventColor, uiState.color)
        assertEquals("", uiState.description)
        assertFalse(uiState.wgMembers[0].isSelected)
        assertFalse(uiState.wgMembers[1].isSelected)
        assertFalse(viewModel.isLoading.value)
        assertEquals(TaskViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)

        verify(mockGetCalendarDataInteractor, times(2)).getTask(taskId)
        verify(mockManageWGProfileInteractor, times(2)).getWGMembers()
    }

    // input changes

    @Test
    fun `onTitleChanged updates title and clears error`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
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
    fun `onDateChanged updates date and clears error`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.onDateChanged(LocalDate.now().minusDays(1))
        viewModel.saveEntry(mockNavController)
        assertNotNull(viewModel.uiState.value.dateError)

        val newDate = LocalDate.now().plusDays(1)

        viewModel.onDateChanged(newDate)
        assertEquals(newDate, viewModel.uiState.value.date)
        assertNull(viewModel.uiState.value.dateError)
    }

    @Test
    fun `onAssignmentChanged correctly selects and deselects members`() = runTest {
        savedStateHandle = SavedStateHandle()
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
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
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
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
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
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
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(TaskViewModel.FIELD_REQUIRED, state.titleError)
    }

    @Test
    fun `error if no members are selected when saveEntry is called`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(TaskViewModel.SELECT_AT_LEAST_ONE_USER, state.value.affectedUsersError)
    }

    // saveEntry() Tests

    @Test
    fun `saveEntry with invalid input does not save and shows error`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
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

        verifyNoInteractions(mockManageTaskInteractor)
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `saveEntry with valid input saves new task and navigates back on success`() = runTest {
        savedStateHandle = SavedStateHandle()
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.onTitleChanged("Test Title")
        val date = LocalDate.now().plusDays(1)
        viewModel.onDateChanged(date)
        viewModel.onColorChanged(Color.Red)
        viewModel.onDescriptionChanged("Test Description")
        viewModel.onAssignmentChanged("1", false)
        viewModel.onAssignmentChanged("2", true)

        `when`(mockManageTaskInteractor.executeCreation(any())).thenReturn(Result.Success(Unit))
        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        val expectedInput = CreateTaskInput(
            title = "Test Title",
            date = date,
            color = Color.Red,
            description = "Test Description",
            affectedUsers = listOf("2")
        )
        verify(mockManageTaskInteractor).executeCreation(expectedInput)
        verify(mockNavController).popBackStack()
        verify(mockManageTaskInteractor, never()).executeEditing(anyString(), any())
    }

    @Test
    fun `saveEntry handles interactor error and shows error message on creation failure`() = runTest {
        savedStateHandle = SavedStateHandle()

        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.onTitleChanged("Test Title")
        val date = LocalDate.now().plusDays(1)
        viewModel.onDateChanged(date)
        viewModel.onColorChanged(Color.Red)
        viewModel.onDescriptionChanged("Test Description")
        viewModel.onAssignmentChanged("1", false)
        viewModel.onAssignmentChanged("2", true)

        val domainError = DomainError.NetworkError
        `when`(mockManageTaskInteractor.executeCreation(any())).thenReturn(Result.Error(domainError))

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageTaskInteractor).executeCreation(any())
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `saveEntry edits existing task on success`() = runTest {
        val taskId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.TASK_ID_ARG to taskId))
        val mockTask = Task(
            taskId = taskId,
            title = "Test Task",
            date = LocalDate.now().plusDays(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = "",
            stateOfTask = false
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getTask(taskId)).thenReturn(flowOf(Result.Success(mockTask)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val newTitle = "Updated Test Task"
        val newDate = LocalDate.now().plusDays(5)
        val newColor = Color.Red
        val newDescription = "This is a test task"

        viewModel.onTitleChanged(newTitle)
        viewModel.onDateChanged(newDate)
        viewModel.onColorChanged(newColor)
        viewModel.onAssignmentChanged("2", false)
        viewModel.onDescriptionChanged(newDescription)

        `when`(mockManageTaskInteractor.executeEditing(anyString(), any())).thenReturn(Result.Success(Unit))
        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        val expectedInput = CreateTaskInput(
            title = newTitle,
            date = newDate,
            color = newColor,
            description = newDescription,
            affectedUsers = listOf("1")
        )
        verify(mockManageTaskInteractor).executeEditing(taskId, expectedInput)
        verify(mockNavController, never()).popBackStack()
        verify(mockManageTaskInteractor, never()).executeCreation(any())
    }

    @Test
    fun `saveEntry handles interactor error and shows error message on editing failure`() = runTest {
        val taskId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.TASK_ID_ARG to taskId))
        val mockTask = Task(
            taskId = taskId,
            title = "Test Task",
            date = LocalDate.now().plusDays(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = "",
            stateOfTask = false
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getTask(taskId)).thenReturn(flowOf(Result.Success(mockTask)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val domainError = DomainError.NetworkError
        `when`(mockManageTaskInteractor.executeEditing(anyString(), any())).thenReturn(Result.Error(domainError))

        viewModel.saveEntry(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageTaskInteractor).executeEditing(anyString(), any())
        verifyNoInteractions(mockNavController)
    }

    // delete tests

    @Test
    fun `delete calls interactor and navigates back on success`() = runTest {
        val taskId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.TASK_ID_ARG to taskId))
        val mockTask = Task(
            taskId = taskId,
            title = "Test Task",
            date = LocalDate.now().plusDays(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = "",
            stateOfTask = false
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getTask(taskId)).thenReturn(flowOf(Result.Success(mockTask)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        `when`(mockManageTaskInteractor.executeDeletion(anyString())).thenReturn(Result.Success(Unit))

        viewModel.delete(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        verify(mockManageTaskInteractor).executeDeletion(taskId)
        verify(mockNavController).popBackStack()
    }

    @Test
    fun `delete handles interactor error and shows error message`() = runTest {
        val taskId = "123"
        savedStateHandle = SavedStateHandle(mapOf(Routes.TASK_ID_ARG to taskId))
        val mockTask = Task(
            taskId = taskId,
            title = "Test Task",
            date = LocalDate.now().plusDays(1),
            affectedUsers = listOf("1", "2"),
            color = Color.Blue,
            description = "",
            stateOfTask = false
        )
        val mockMembers = listOf(
            User(userId = "1", displayName = "Member1", profilePicture = "http://example.com/member1.jpg"),
            User(userId = "2", displayName = "Member2", profilePicture = "http://example.com/member2.jpg")
        )

        `when`(mockManageWGProfileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(mockMembers)))
        `when`(mockGetCalendarDataInteractor.getTask(taskId)).thenReturn(flowOf(Result.Success(mockTask)))

        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )
        advanceUntilIdle()

        val domainError = DomainError.NetworkError
        `when`(mockManageTaskInteractor.executeDeletion(anyString())).thenReturn(Result.Error(domainError))

        viewModel.delete(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageTaskInteractor).executeDeletion(taskId)
        verifyNoInteractions(mockNavController)
    }

    // navigation tests

    @Test
    fun `navigateToOtherEntryCreation navigates to create appointment`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.navigateToOtherEntryCreation(mockNavController)
        verify(mockNavController).navigate(eq(Routes.CREATE_APPOINTMENT), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `navigateBack calls popBackStack on navController`() = runTest {
        savedStateHandle = SavedStateHandle()
        viewModel = TaskViewModel(
            mockManageTaskInteractor,
            mockManageWGProfileInteractor,
            mockGetCalendarDataInteractor,
            savedStateHandle
        )

        viewModel.navigateBack(mockNavController)
        verify(mockNavController).popBackStack()
    }
}
