package com.wgeplant.model.interactor.calendarManagement

import androidx.compose.ui.graphics.Color
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.domain.UNKNOWN_ERROR
import com.wgeplant.model.repository.AppointmentRepo
import com.wgeplant.model.repository.TaskRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class GetCalendarDataInteractorImplTest {

    companion object {
        private const val TEST_TASK_ID = "testId"
        private const val TEST_TITLE = "Test Title"
        private const val TEST_DESCRIPTION = "Test Description"
        private const val TASK_STATE_CHECKED_OFF = true
        private const val TASK_STATE_NOT_CHECKED_OFF = false
        private const val TEST_APPOINTMENT_ID = "testAppointmentId"
        private const val TEST_APPOINTMENT_TITLE = "Test Appointment"
        private const val TEST_ERROR_MESSAGE = "Test Error Message"
        private val TEST_AFFECTED_USERS = listOf("userId1", "userId2")
        private val TEST_COLOR = mock<Color>()
        private val TEST_START_DATE = LocalDateTime.now()
        private val TEST_END_DATE = LocalDateTime.now().plusDays(1)
        private val TEST_MONTH = YearMonth.now()
    }

    // mocks for dependency
    private lateinit var mockTaskRepo: TaskRepo
    private lateinit var mockAppointmentRepo: AppointmentRepo

    // class that gets tested
    private lateinit var getCalendarDataInteractor: GetCalendarDataInteractorImpl

    @Before
    fun setUp() {
        // initialize mocks
        mockTaskRepo = mock()
        mockAppointmentRepo = mock()
        // initialize the classes with mocks
        getCalendarDataInteractor = GetCalendarDataInteractorImpl(
            taskRepo = mockTaskRepo,
            appointmentRepo = mockAppointmentRepo
        )
    }

    @Test
    fun `getAppointmentsForMonth success`() = runTest {
        val mockAppointments = listOf(
            Appointment(
                appointmentId = TEST_APPOINTMENT_ID,
                title = TEST_APPOINTMENT_TITLE,
                startDate = TEST_START_DATE,
                endDate = TEST_END_DATE,
                affectedUsers = TEST_AFFECTED_USERS,
                color = TEST_COLOR,
                description = TEST_DESCRIPTION
            )
        )
        whenever(mockAppointmentRepo.getMonthlyAppointments(TEST_MONTH)).thenReturn(
            flowOf(Result.Success(mockAppointments))
        )
        val result = getCalendarDataInteractor.getAppointmentsForMonth(TEST_MONTH)
        assertTrue(result.first() is Result.Success)
        assertEquals(mockAppointments, (result.first() as Result.Success).data)
    }

    @Test
    fun `getAppointmentsForMonth exception`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockAppointmentRepo.getMonthlyAppointments(TEST_MONTH)).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = getCalendarDataInteractor.getAppointmentsForMonth(TEST_MONTH)
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
        assertEquals(expectedDomainError, actualDomainError, TEST_ERROR_MESSAGE)
    }

    @Test
    fun `getTaskList success`() = runTest {
        val mockTaskList = listOf(
            Task(
                taskId = TEST_TASK_ID,
                title = TEST_TITLE,
                affectedUsers = TEST_AFFECTED_USERS,
                color = TEST_COLOR,
                description = TEST_DESCRIPTION,
                stateOfTask = TASK_STATE_NOT_CHECKED_OFF
            )
        )
        whenever(mockTaskRepo.getTaskList()).thenReturn(flowOf(Result.Success(mockTaskList)))
        val result = getCalendarDataInteractor.getTaskList()
        assertTrue(result.first() is Result.Success)
        assertEquals(mockTaskList, (result.first() as Result.Success).data)
    }

    @Test
    fun `getTaskList failure`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockTaskRepo.getTaskList()).thenReturn(flowOf(Result.Error(expectedError)))
        val result = getCalendarDataInteractor.getTaskList()
        assertTrue(result.first() is Result.Error)
        assertEquals(expectedError, (result.first() as Result.Error).error)
    }

    @Test
    fun `getTaskList exception`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockTaskRepo.getTaskList()).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = getCalendarDataInteractor.getTaskList()
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
        assertEquals(expectedDomainError, actualDomainError, TEST_ERROR_MESSAGE)
    }

    @Test
    fun `getTasksForMonth success`() = runTest {
        val mockTasks = listOf(
            Task(
                taskId = TEST_TASK_ID,
                title = TEST_TITLE,
                affectedUsers = TEST_AFFECTED_USERS,
                color = TEST_COLOR,
                description = TEST_DESCRIPTION,
                stateOfTask = TASK_STATE_NOT_CHECKED_OFF
            )
        )
        whenever(mockTaskRepo.getMonthlyTasks(TEST_MONTH)).thenReturn(flowOf(Result.Success(mockTasks)))
        val result = getCalendarDataInteractor.getTasksForMonth(TEST_MONTH)
        assertTrue(result.first() is Result.Success)
        assertEquals(mockTasks, (result.first() as Result.Success).data)
    }

    @Test
    fun `getTasksForMonth exception`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockTaskRepo.getMonthlyTasks(TEST_MONTH)).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = getCalendarDataInteractor.getTasksForMonth(TEST_MONTH)
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
        assertEquals(expectedDomainError, actualDomainError, TEST_ERROR_MESSAGE)
    }

    @Test
    fun `getAppointment success`() = runTest {
        val mockAppointment = Appointment(
            appointmentId = TEST_APPOINTMENT_ID,
            title = TEST_APPOINTMENT_TITLE,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_DESCRIPTION
        )
        whenever(mockAppointmentRepo.getAppointmentById(TEST_APPOINTMENT_ID)).thenReturn(
            flowOf(Result.Success(mockAppointment))
        )
        val result = getCalendarDataInteractor.getAppointment(TEST_APPOINTMENT_ID)
        assertTrue(result.first() is Result.Success)
        assertEquals(mockAppointment, (result.first() as Result.Success).data)
    }

    @Test
    fun `getAppointment exception`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockAppointmentRepo.getAppointmentById(TEST_APPOINTMENT_ID)).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = getCalendarDataInteractor.getAppointment(TEST_APPOINTMENT_ID)
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
        assertEquals(expectedDomainError, actualDomainError, TEST_ERROR_MESSAGE)
    }

    @Test
    fun `getTask success`() = runTest {
        val mockTask = Task(
            taskId = TEST_TASK_ID,
            title = TEST_TITLE,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_DESCRIPTION,
            stateOfTask = TASK_STATE_NOT_CHECKED_OFF
        )
        whenever(mockTaskRepo.getTaskById(TEST_TASK_ID)).thenReturn(flowOf(Result.Success(mockTask)))
        val result = getCalendarDataInteractor.getTask(TEST_TASK_ID)
        assertTrue(result.first() is Result.Success)
        assertEquals(mockTask, (result.first() as Result.Success).data)
    }

    @Test
    fun `getTask exception`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        whenever(mockTaskRepo.getTaskById(TEST_TASK_ID)).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = getCalendarDataInteractor.getTask(TEST_TASK_ID)
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
    }

    @Test
    fun `changeTaskState success with not checked off task`() = runTest {
        val mockTask = Task(
            taskId = TEST_TASK_ID,
            title = TEST_TITLE,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_DESCRIPTION,
            stateOfTask = TASK_STATE_NOT_CHECKED_OFF
        )

        whenever(mockTaskRepo.getTaskById(TEST_TASK_ID)).thenReturn(flowOf(Result.Success(mockTask)))
        val expectedUpdatedTask = mockTask.copy(stateOfTask = TASK_STATE_CHECKED_OFF)
        whenever(mockTaskRepo.updateTask(expectedUpdatedTask)).thenReturn(Result.Success(Unit))

        val result = getCalendarDataInteractor.changeTaskState(TEST_TASK_ID)

        assertTrue(result is Result.Success)
        verify(mockTaskRepo).updateTask(expectedUpdatedTask)
    }
}
