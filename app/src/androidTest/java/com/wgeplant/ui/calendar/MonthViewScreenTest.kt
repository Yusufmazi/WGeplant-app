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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MonthViewScreenTest {

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

        val days = listOf(
            CalendarDay(
                date = LocalDate.of(2025, 1, 31),
                isCurrentMonth = false,
                hasEntries = false,
                appointmentSegments = emptyList(),
                tasks = emptyList(),
                isToday = false
            ),
            CalendarDay(
                date = LocalDate.of(2025, 2, 1),
                isCurrentMonth = true,
                hasEntries = false,
                appointmentSegments = emptyList(),
                tasks = emptyList(),
                isToday = false
            ),
            CalendarDay(
                date = LocalDate.of(2025, 2, 2),
                isCurrentMonth = true,
                hasEntries = false,
                appointmentSegments = emptyList(),
                tasks = emptyList(),
                isToday = false
            )
        )
        uiStateFlow.value = uiStateFlow.value.copy(daysForCalendarGrid = days)

        whenever(mockCalendarViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockCalendarViewModel.errorMessage).thenReturn(errorMessageFlow)
        whenever(mockCalendarViewModel.isLoading).thenReturn(isLoadingFlow)

        composeTestRule.setContent {
            MonthlyCalendarScreen(
                navController = mockNavController,
                calendarViewModel = mockCalendarViewModel
            )
        }
    }

    @Test
    fun appLogo_andTitle_areDisplayed() {
        composeTestRule.onNodeWithContentDescription(MonthlyCalendarScreenConstants.APP_LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            MonthlyCalendarScreenConstants.APP_TITLE_PART1 + DailyCalendarScreenConstants.APP_TITLE_PART2
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
        composeTestRule.onNodeWithText(MonthlyCalendarScreenConstants.MONTH).assertIsDisplayed()
        composeTestRule.onNodeWithText(MonthlyCalendarScreenConstants.DAY).assertIsDisplayed()

        composeTestRule.onNodeWithText(MonthlyCalendarScreenConstants.DAY).performClick()
        verify(mockCalendarViewModel).navigateToDailyView(any(), any())

        composeTestRule.onNodeWithContentDescription(MonthlyCalendarScreenConstants.ADD_APPOINTMENT).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(MonthlyCalendarScreenConstants.ADD_APPOINTMENT).performClick()
        verify(mockCalendarViewModel).navigateToAppointmentCreation(any())
    }

    @Test
    fun calendarGrid_navigatesOnSwipe() {
        composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.CALENDAR_GRID_TEST_TAG)
            .performTouchInput { swipeLeft() }
        verify(mockCalendarViewModel).showNextMonth()

        composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.CALENDAR_GRID_TEST_TAG)
            .performTouchInput { swipeRight() }
        verify(mockCalendarViewModel).showPreviousMonth()
    }

    @Test
    fun errorMessage_isDisplayed_whenAvailable() {
        errorMessageFlow.value = "Error!"
        composeTestRule.onNodeWithText("Error!").assertIsDisplayed()
    }

    @Test
    fun dayCells_areDisplayed_andAreClickable() {
        MonthlyCalendarScreenConstants.DAY_NAMES.forEach { dayName ->
            composeTestRule.onNodeWithText(dayName).assertIsDisplayed()
        }

        composeTestRule.onNodeWithText("31").performClick()
        verify(mockCalendarViewModel, never()).navigateToDailyView(any(), any())

        composeTestRule.onNodeWithText("1").performClick()
        verify(mockCalendarViewModel).navigateToDailyView(any(), any())
    }
}
