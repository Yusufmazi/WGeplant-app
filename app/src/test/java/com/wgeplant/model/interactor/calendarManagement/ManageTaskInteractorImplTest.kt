package com.wgeplant.model.interactor.calendarManagement

import androidx.compose.ui.graphics.Color
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.repository.TaskRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ManageTaskInteractorImplTest {
    companion object {
        private const val TEST_TASK_ID = "testTaskId"
        private const val TEST_TITLE = "Test Title"
        private const val TEST_DESCRIPTION = "Test Description"
        private const val TASK_STATE_NOT_CHECKED_OFF = false
        private val TEST_AFFECTED_USERS = listOf("userId1", "userId2")
        private val TEST_COLOR = mock<Color>()
        private val TEST_EDIT = "testEditedData"
    }

    // mock for dependency
    private lateinit var mockTaskRepo: TaskRepo

    // class that gets tested
    private lateinit var manageTaskInteractor: ManageTaskInteractorImpl

    @Before
    fun setUp() {
        // initialize mocks
        mockTaskRepo = mock()

        // initialize the classes with mocks
        manageTaskInteractor = ManageTaskInteractorImpl(
            taskRepo = mockTaskRepo
        )
    }

    @Test
    fun `executeEditing success with edited title and description`() = runTest {
        val currentTask = Task(
            taskId = TEST_TASK_ID,
            title = TEST_TITLE,
            description = TEST_DESCRIPTION,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            stateOfTask = TASK_STATE_NOT_CHECKED_OFF
        )
        val edits = CreateTaskInput(
            title = TEST_EDIT,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_EDIT
        )

        whenever(mockTaskRepo.getTaskById(TEST_TASK_ID)).thenReturn(flowOf(Result.Success(currentTask)))
        val editedTask = Task(
            taskId = TEST_TASK_ID,
            title = TEST_EDIT,
            description = TEST_EDIT,
            affectedUsers = currentTask.affectedUsers,
            color = currentTask.color,
            stateOfTask = TASK_STATE_NOT_CHECKED_OFF
        )
        whenever(mockTaskRepo.updateTask(editedTask)).thenReturn(Result.Success(Unit))

        val result = manageTaskInteractor.executeEditing(TEST_TASK_ID, edits)

        assertTrue(result is Result.Success)
        verify(mockTaskRepo).updateTask(editedTask)
    }
}
