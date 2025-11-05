package com.wgeplant.ui.calendar
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.Task
import com.wgeplant.ui.theme.EventColors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DayViewScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockCalendarViewModel: ICalendarViewModel
    private lateinit var mockNavController: NavController

    private val uiStateFlow = MutableStateFlow(CalendarUiState())
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        mockCalendarViewModel = mock()
        mockNavController = mock()

        whenever(mockCalendarViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockCalendarViewModel.errorMessage).thenReturn(errorMessageFlow)
        whenever(mockCalendarViewModel.isLoading).thenReturn(isLoadingFlow)

        val testAppointment = Appointment(
            appointmentId = "1",
            title = "Movie night",
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusHours(1),
            color = EventColors.defaultEventColor,
            affectedUsers = emptyList(),
            description = null
        )
        val testTask = Task(
            taskId = "2",
            title = "Cleaning",
            stateOfTask = false,
            color = EventColors.defaultEventColor,
            affectedUsers = emptyList(),
            description = null
        )

        whenever(mockCalendarViewModel.getSelectedDayDetails()).thenReturn(
            CalendarDay(
                date = LocalDate.now(),
                appointmentSegments = listOf(
                    AppointmentDisplaySegment(
                        originalAppointment = testAppointment,
                        segmentStartDate = LocalDateTime.now().plusDays(1),
                        segmentEndDate = LocalDateTime.now().plusDays(1).plusHours(1),
                        date = LocalDate.now().plusDays(1),
                        startsOnThisDay = true,
                        endsOnThisDay = true,
                        isMultiDayStart = false,
                        isMultiDayEnd = false,
                        isMultiDayMiddle = false,
                        laneIndex = 1
                    )
                ),
                tasks = listOf(testTask),
                isCurrentMonth = true,
                hasEntries = true,
                isToday = true
            )
        )

        composeTestRule.setContent {
            DailyCalendarScreen(
                navController = mockNavController,
                calendarViewModel = mockCalendarViewModel
            )
        }
    }

    @Test
    fun appLogo_andTitle_areDisplayed() {
        composeTestRule.onNodeWithContentDescription(DailyCalendarScreenConstants.APP_LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            DailyCalendarScreenConstants.APP_TITLE_PART1 + DailyCalendarScreenConstants.APP_TITLE_PART2
        ).assertIsDisplayed()
    }

    @Test
    fun wgProfileCircle_isDisplayed_andClickable() {
        composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE).performClick()
        verify(mockCalendarViewModel).navigateToWGProfile(any())
    }

    @Test
    fun bottomBar_isDisplayed_andNavigatesCorrectly() {
        composeTestRule.onNodeWithContentDescription(MonthlyCalendarScreenConstants.TODO).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(MonthlyCalendarScreenConstants.TODO).performClick()
        verify(mockCalendarViewModel).navigateToToDo(any())
    }

    @Test
    fun calendarHeader_isDisplayed_andClickable() {
        composeTestRule.onNodeWithText(DailyCalendarScreenConstants.MONTH).assertIsDisplayed()
        composeTestRule.onNodeWithText(DailyCalendarScreenConstants.DAY).assertIsDisplayed()

        composeTestRule.onNodeWithText(DailyCalendarScreenConstants.MONTH).performClick()
        verify(mockCalendarViewModel).navigateToMonthlyView(any())

        composeTestRule.onNodeWithContentDescription(DailyCalendarScreenConstants.ADD_APPOINTMENT).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(DailyCalendarScreenConstants.ADD_APPOINTMENT).performClick()
        verify(mockCalendarViewModel).navigateToAppointmentCreation(any())
    }

    @Test
    fun dayOfWeekSelector_navigatesOnSwipe() {
        composeTestRule.onNodeWithTag(DailyCalendarScreenConstants.WEEK_TEST_TAG)
            .performTouchInput { swipeLeft() }
        verify(mockCalendarViewModel).showNextWeek()

        composeTestRule.onNodeWithTag(DailyCalendarScreenConstants.WEEK_TEST_TAG)
            .performTouchInput { swipeRight() }
        verify(mockCalendarViewModel).showPreviousWeek()
    }

    @Test
    fun dayOfWeekSelector_navigatesOnArrowClick() {
        composeTestRule.onNodeWithContentDescription(DailyCalendarScreenConstants.NEXT_WEEK).performClick()
        verify(mockCalendarViewModel).showNextWeek()

        composeTestRule.onNodeWithContentDescription(DailyCalendarScreenConstants.PREVIOUS_WEEK).performClick()
        verify(mockCalendarViewModel).showPreviousWeek()
    }

    @Test
    fun appointmentsAndTasks_areShown_whenListsAreNotEmpty() {
        val testTask = Task(
            taskId = "2",
            title = "Cleaning",
            stateOfTask = false,
            color = EventColors.defaultEventColor,
            affectedUsers = emptyList(),
            description = null
        )

        composeTestRule.onNodeWithText("Movie night").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cleaning").assertIsDisplayed()

        composeTestRule.onNodeWithText("Cleaning").performClick()
        verify(mockCalendarViewModel).changeTaskState(testTask)
    }

    @Test
    fun errorMessage_isDisplayed_whenAvailable() {
        errorMessageFlow.value = "Error!"
        composeTestRule.onNodeWithText("Error!").assertIsDisplayed()
    }
}
