package com.wgeplant.viewmodel.toDo

import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.domain.WG
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.ui.toDo.ToDoViewModel
import com.wgeplant.util.MainCoroutineRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExperimentalCoroutinesApi
class ToDoViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockCalendarInteractor: GetCalendarDataInteractor

    @Mock
    private lateinit var mockWGProfileInteractor: ManageWGProfileInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: ToDoViewModel

    private fun createTask(id: String, date: LocalDate, title: String): Task {
        return Task(
            taskId = id,
            date = date,
            title = title,
            description = "",
            color = Color.White,
            stateOfTask = false,
            affectedUsers = emptyList()
        )
    }

    @Before
    fun setUp() {
        whenever(mockCalendarInteractor.getTaskList()).thenReturn(flowOf(Result.Success(emptyList())))
        whenever(mockWGProfileInteractor.getWGData()).thenReturn(flowOf(Result.Success(null)))

        viewModel = ToDoViewModel(mockCalendarInteractor, mockWGProfileInteractor)
    }

    @Test
    fun `init calls data observation methods`() = runTest {
        verify(mockCalendarInteractor).getTaskList()
        verify(mockWGProfileInteractor).getWGData()
    }

    @Test
    fun `observeTaskData on success updates uiState with sorted tasks`() = runTest {
        val today = LocalDate.now()
        val task1 = createTask(id = "1", date = today, title = "Task 1")
        val task3 = createTask(id = "3", date = today.plusDays(2), title = "Task 3")
        val task2 = createTask(id = "2", date = today.plusDays(1), title = "Task 2")
        val unsortedTasks = listOf(task1, task3, task2)
        whenever(mockCalendarInteractor.getTaskList()).thenReturn(flowOf(Result.Success(unsortedTasks)))

        viewModel = ToDoViewModel(mockCalendarInteractor, mockWGProfileInteractor)
        advanceUntilIdle()

        val expectedOrder = listOf(task1, task2, task3)
        assertEquals(expectedOrder, viewModel.uiState.value.tasks)
    }

    @Test
    fun `observeTaskData on error updates uiState with empty list`() = runTest {
        whenever(mockCalendarInteractor.getTaskList()).thenReturn(flowOf(Result.Error(DomainError.NetworkError)))
        viewModel = ToDoViewModel(mockCalendarInteractor, mockWGProfileInteractor)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
    }

    @Test
    fun `observeTaskData on exception shows error and updates with empty list`() = runTest {
        val errorMessage = "Database error"
        whenever(mockCalendarInteractor.getTaskList()).thenReturn(flow { throw RuntimeException(errorMessage) })
        viewModel = ToDoViewModel(mockCalendarInteractor, mockWGProfileInteractor)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    @Test
    fun `observeWgProfilePicture on success updates uiState with image URL`() = runTest {
        val imageUrl = "http://example.com/profile.jpg"
        val wg = WG(
            wgId = "wg1",
            displayName = "My WG",
            invitationCode = "12345",
            profilePicture = imageUrl
        )
        whenever(mockWGProfileInteractor.getWGData()).thenReturn(flowOf(Result.Success(wg)))
        viewModel = ToDoViewModel(mockCalendarInteractor, mockWGProfileInteractor)
        advanceUntilIdle()
        assertEquals(imageUrl, viewModel.uiState.value.wgProfileImageUrl)
    }

    @Test
    fun `observeWgProfilePicture on error updates uiState with null URL`() = runTest {
        whenever(mockWGProfileInteractor.getWGData()).thenReturn(flowOf(Result.Error(DomainError.NetworkError)))
        viewModel = ToDoViewModel(mockCalendarInteractor, mockWGProfileInteractor)
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.wgProfileImageUrl)
    }

    @Test
    fun `changeTaskState with valid task calls interactor`() = runTest {
        val task = createTask(id = "123", date = LocalDate.now(), title = "Test Task")
        whenever(mockCalendarInteractor.changeTaskState(any())).thenReturn(Result.Success(Unit))
        viewModel.changeTaskState(task)
        advanceUntilIdle()
        verify(mockCalendarInteractor).changeTaskState("123")
    }

    @Test
    fun `changeTaskState with null taskId does not call interactor`() = runTest {
        val task = createTask(id = "", date = LocalDate.now(), title = "Test Task").copy(taskId = null)
        viewModel.changeTaskState(task)
        advanceUntilIdle()
        verify(mockCalendarInteractor, never()).changeTaskState(any())
    }

    @Test
    fun `changeTaskState on error handles domain error`() = runTest {
        val task = createTask(id = "123", date = LocalDate.now(), title = "Test Task")
        val error = DomainError.NetworkError
        whenever(mockCalendarInteractor.changeTaskState(any())).thenReturn(Result.Error(error))
        viewModel.changeTaskState(task)
        advanceUntilIdle()
        assertEquals(error.message, viewModel.errorMessage.value)
    }

    @Test
    fun `navigateToTaskCreation calls navigate with correct route`() {
        viewModel.navigateToTaskCreation(mockNavController)
        verify(mockNavController).navigate(Routes.CREATE_TASK)
    }

    @Test
    fun `navigateToTask calls navigate with correct task route`() {
        val task = createTask(id = "task_abc", date = LocalDate.now(), title = "Test Task")
        viewModel.navigateToTask(task, mockNavController)
        verify(mockNavController).navigate(Routes.getTaskRoute("task_abc"))
    }

    @Test
    fun `navigateToCalendar calls navigate with correct route`() {
        viewModel.navigateToCalendar(mockNavController)
        verify(mockNavController).navigate(Routes.CALENDAR_GRAPH)
    }

    @Test
    fun `navigateToWGProfile calls navigate with correct route`() {
        viewModel.navigateToWGProfile(mockNavController)
        verify(mockNavController).navigate(Routes.PROFILE_WG)
    }

    @Test
    fun `observeWgProfilePicture on exception shows default error and sets URL to null`() = runTest {
        val errorMessage = "Unexpected failure"
        whenever(mockWGProfileInteractor.getWGData()).thenReturn(flow { throw RuntimeException(errorMessage) })

        viewModel = ToDoViewModel(mockCalendarInteractor, mockWGProfileInteractor)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.wgProfileImageUrl)
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    @Test
    fun `changeTaskState on success does not set error message`() = runTest {
        val task = createTask(id = "task_success", date = LocalDate.now(), title = "Task Success")
        whenever(mockCalendarInteractor.changeTaskState("task_success")).thenReturn(Result.Success(Unit))

        viewModel.changeTaskState(task)
        advanceUntilIdle()

        verify(mockCalendarInteractor).changeTaskState("task_success")
        assertEquals(null, viewModel.errorMessage.value)
    }
}
