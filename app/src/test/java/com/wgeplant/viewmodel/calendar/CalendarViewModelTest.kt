package com.wgeplant.viewmodel.calendar

import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.domain.WG
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.calendar.CalendarViewModel
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CalendarViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockGetCalendarDataInteractor: GetCalendarDataInteractor

    @Mock
    private lateinit var mockManageWGProfileInteractor: ManageWGProfileInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: CalendarViewModel

    // data load tests

    @Test
    fun `ViewModel initializes and loads WG profile picture on success`() = runTest {
        val mockWG = WG(
            wgId = "1",
            displayName = "TestWG",
            profilePicture = "http://wg.com/image.jpg",
            invitationCode = "123456"
        )
        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(flowOf(Result.Success(mockWG)))
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("http://wg.com/image.jpg", uiState.wgProfileImageUrl)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        verify(mockManageWGProfileInteractor).getWGData()
    }

    @Test
    fun `ViewModel initializes and loads current month data correctly on success`() = runTest {
        val currentMonth = YearMonth.now()
        val today = LocalDate.now()
        val currentMonthGridDays = 35

        val mockWG = WG(
            wgId = "1",
            displayName = "TestWG",
            profilePicture = "http://wg.com/image.jpg",
            invitationCode = "123456"
        )

        val mockAppointments = listOf(
            Appointment(
                appointmentId = "a1",
                title = "Test Appointment 1",
                startDate = LocalDateTime.of(currentMonth.atDay(1), LocalTime.of(12, 0)),
                endDate = LocalDateTime.of(currentMonth.atDay(1), LocalTime.of(13, 0)),
                affectedUsers = listOf("u1"),
                color = Color.Blue,
                description = "This is a test appointment"
            ),
            Appointment(
                appointmentId = "a2",
                title = "Test Appointment 2",
                startDate = LocalDateTime.of(currentMonth.atDay(4), LocalTime.of(12, 0)),
                endDate = LocalDateTime.of(currentMonth.atDay(6), LocalTime.of(10, 0)),
                affectedUsers = listOf("u1", "u2"),
                color = Color.Red,
                description = "This is a test appointment"
            )
        )

        val mockTasks = listOf(
            Task(
                taskId = "t1",
                title = "Test Task 1",
                date = currentMonth.atDay(1),
                affectedUsers = listOf("u1"),
                color = Color.Blue,
                description = "This is a test task",
                stateOfTask = false
            ),
            Task(
                taskId = "t2",
                title = "Test Task 2",
                date = currentMonth.atDay(7),
                affectedUsers = listOf("u1", "u2"),
                color = Color.Green,
                description = "This is a test task",
                stateOfTask = false
            )
        )

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(flowOf(Result.Success(mockWG)))
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(currentMonth)).thenReturn(
            flowOf(Result.Success(mockAppointments))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(currentMonth)).thenReturn(
            flowOf(Result.Success(mockTasks))
        )

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(currentMonth, uiState.currentlyDisplayedMonth)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)

        assertFalse(uiState.daysForCalendarGrid.isEmpty())
        assertEquals(currentMonthGridDays, uiState.daysForCalendarGrid.size)

        val todayCalendarDay = uiState.daysForCalendarGrid.find { it.date == today }
        assertNotNull(todayCalendarDay)
        assertTrue(todayCalendarDay.isToday)

        val day1CalendarDay = uiState.daysForCalendarGrid.find { it.date == currentMonth.atDay(1) }
        assertNotNull(day1CalendarDay)
        assertTrue(day1CalendarDay.hasEntries)
        assertEquals(1, day1CalendarDay.appointmentSegments.size)
        assertEquals("Test Appointment 1", day1CalendarDay.appointmentSegments.first().originalAppointment.title)

        val day5CalendarDay = uiState.daysForCalendarGrid.find { it.date == currentMonth.atDay(5) }
        assertNotNull(day5CalendarDay)
        assertTrue(day5CalendarDay.hasEntries)
        assertTrue(day5CalendarDay.appointmentSegments.first().isMultiDayMiddle)

        val day8CalendarDay = uiState.daysForCalendarGrid.find { it.date == currentMonth.atDay(8) }
        assertNotNull(day8CalendarDay)
        assertFalse(day8CalendarDay.hasEntries)
    }

    // Navigation Tests

    @Test
    fun `navigateToAppointmentCreation navigates correctly`() = runTest {
        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)

        viewModel.navigateToAppointmentCreation(mockNavController)
        verify(mockNavController).navigate(Routes.CREATE_APPOINTMENT)
    }

    @Test
    fun `navigateToAppointment navigates correctly to appointment`() = runTest {
        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        val testAppointment = Appointment(
            appointmentId = "a1",
            title = "Test Appointment 1",
            startDate = LocalDateTime.now().plusDays(1),
            endDate = LocalDateTime.now().plusDays(1).plusHours(2),
            affectedUsers = listOf("u1"),
            color = Color.Blue,
            description = "This is a test appointment"
        )
        val expectedRoute = Routes.getAppointmentRoute("a1")
        viewModel.navigateToAppointment(testAppointment, mockNavController)
        verify(mockNavController).navigate(expectedRoute)
    }

    @Test
    fun `navigateToTask navigates correctly to task`() = runTest {
        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        val testTask = Task(
            taskId = "t1",
            title = "Test Task 1",
            date = LocalDate.now(),
            affectedUsers = listOf("u1"),
            color = Color.Blue,
            description = "This is a test task",
            stateOfTask = false
        )
        val expectedRoute = Routes.getTaskRoute("t1")
        viewModel.navigateToTask(testTask, mockNavController)
        verify(mockNavController).navigate(expectedRoute)
    }

    @Test
    fun `navigateToToDo navigates correctly`() = runTest {
        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        viewModel.navigateToToDo(mockNavController)
        verify(mockNavController).navigate(Routes.TODO)
    }

    @Test
    fun `navigateToWGProfile navigates correctly`() = runTest {
        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        viewModel.navigateToWGProfile(mockNavController)
        verify(mockNavController).navigate(Routes.PROFILE_WG)
    }

    @Test
    fun `navigateToDailyView triggers week data observation and navigates correctly`() = runTest {
        val today = LocalDate.now()
        val currentMonth = YearMonth.now()

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.navigateToDailyView(today, mockNavController)
        advanceUntilIdle()

        verify(mockGetCalendarDataInteractor, times(2)).getAppointmentsForMonth(currentMonth)
        verify(mockGetCalendarDataInteractor, times(2)).getTasksForMonth(currentMonth)
        verify(mockNavController).navigate(eq(Routes.CALENDAR_DAY), any<NavOptionsBuilder.() -> Unit>())

        assertEquals(today, viewModel.uiState.value.currentlyDisplayedDay)
        assertFalse(viewModel.uiState.value.daysForDisplayedWeek.isEmpty())
    }

    @Test
    fun `navigateToMonthlyView triggers month data observation and navigates correctly`() = runTest {
        val currentMonth = YearMonth.now()

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.navigateToMonthlyView(mockNavController)
        advanceUntilIdle()

        verify(mockGetCalendarDataInteractor, times(2)).getAppointmentsForMonth(currentMonth)
        verify(mockGetCalendarDataInteractor, times(2)).getTasksForMonth(currentMonth)
        verify(mockNavController).navigate(eq(Routes.CALENDAR_MONTH), any<NavOptionsBuilder.() -> Unit>())

        assertEquals(currentMonth, viewModel.uiState.value.currentlyDisplayedMonth)
        assertFalse(viewModel.uiState.value.daysForCalendarGrid.isEmpty())
    }

    // navigate through calendar tests

    @Test
    fun `showPreviousMonth updates currentlyDisplayedMonth and re-observes month data`() = runTest {
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.showPreviousMonth()
        advanceUntilIdle()

        assertEquals(previousMonth, viewModel.uiState.value.currentlyDisplayedMonth)
        verify(mockGetCalendarDataInteractor).getAppointmentsForMonth(previousMonth)
        verify(mockGetCalendarDataInteractor).getTasksForMonth(previousMonth)
    }

    @Test
    fun `showNextMonth updates currentlyDisplayedMonth and re-observes month data`() = runTest {
        val currentMonth = YearMonth.now()
        val nextMonth = currentMonth.plusMonths(1)

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.showNextMonth()
        advanceUntilIdle()

        assertEquals(nextMonth, viewModel.uiState.value.currentlyDisplayedMonth)
        verify(mockGetCalendarDataInteractor).getAppointmentsForMonth(nextMonth)
        verify(mockGetCalendarDataInteractor).getTasksForMonth(nextMonth)
    }

    @Test
    fun `showPreviousWeek updates currentlyDisplayedDay and re-observes week data`() = runTest {
        val today = LocalDate.now()
        val currentMonth = YearMonth.now()
        val previousWeekDay = today.minusWeeks(1)
        val monthOfPreviousWeekDay = YearMonth.from(previousWeekDay)

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.showPreviousWeek()
        advanceUntilIdle()
        if (monthOfPreviousWeekDay != currentMonth) {
            assertEquals(
                monthOfPreviousWeekDay.atDay(monthOfPreviousWeekDay.lengthOfMonth()),
                viewModel.uiState.value.currentlyDisplayedDay
            )
            verify(mockGetCalendarDataInteractor).getAppointmentsForMonth(monthOfPreviousWeekDay)
            verify(mockGetCalendarDataInteractor).getTasksForMonth(monthOfPreviousWeekDay)
        } else {
            assertEquals(previousWeekDay, viewModel.uiState.value.currentlyDisplayedDay)
            verify(mockGetCalendarDataInteractor, times(2)).getAppointmentsForMonth(currentMonth)
            verify(mockGetCalendarDataInteractor, times(2)).getTasksForMonth(currentMonth)
        }
    }

    @Test
    fun `showNextWeek updates currentlyDisplayedDay and re-observes week data`() = runTest {
        val today = LocalDate.now()
        val currentMonth = YearMonth.now()
        val nextWeekDay = today.plusWeeks(1)
        val monthOfNextWeekDay = YearMonth.from(nextWeekDay)

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.showNextWeek()
        advanceUntilIdle()
        assertEquals(nextWeekDay, viewModel.uiState.value.currentlyDisplayedDay)

        if (monthOfNextWeekDay != currentMonth) {
            verify(mockGetCalendarDataInteractor).getAppointmentsForMonth(monthOfNextWeekDay)
            verify(mockGetCalendarDataInteractor).getTasksForMonth(monthOfNextWeekDay)
        }
    }

    // changeTaskState() tests

    @Test
    fun `changeTaskState calls interactor and handles success`() = runTest {
        val testTask = Task(
            taskId = "t1",
            title = "Test Task 1",
            date = LocalDate.now(),
            affectedUsers = listOf("u1"),
            color = Color.Blue,
            description = "This is a test task",
            stateOfTask = false
        )
        `when`(mockGetCalendarDataInteractor.changeTaskState(testTask.taskId!!)).thenReturn(Result.Success(Unit))
        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.changeTaskState(testTask)
        advanceUntilIdle()

        verify(mockGetCalendarDataInteractor).changeTaskState(testTask.taskId!!)
        assertNull(viewModel.errorMessage.value)
    }

    // viewSelectedDay() tests

    @Test
    fun `viewSelectedDay updates currentlyDisplayedDay in same week`() = runTest {
        val today = LocalDate.now()
        val newSelectedDay = if (today.dayOfWeek == DayOfWeek.SUNDAY) today.minusDays(1) else today.plusDays(1)

        `when`(mockManageWGProfileInteractor.getWGData()).thenReturn(
            flowOf(
                Result.Success(WG(wgId = "w1", displayName = "WG", profilePicture = null, invitationCode = "123456"))
            )
        )
        `when`(mockGetCalendarDataInteractor.getAppointmentsForMonth(any())).thenReturn(
            flowOf(Result.Success(emptyList()))
        )
        `when`(mockGetCalendarDataInteractor.getTasksForMonth(any())).thenReturn(flowOf(Result.Success(emptyList())))

        viewModel = CalendarViewModel(mockGetCalendarDataInteractor, mockManageWGProfileInteractor)
        advanceUntilIdle()

        viewModel.viewSelectedDay(newSelectedDay)
        advanceUntilIdle()

        assertEquals(newSelectedDay, viewModel.uiState.value.currentlyDisplayedDay)
    }
}
