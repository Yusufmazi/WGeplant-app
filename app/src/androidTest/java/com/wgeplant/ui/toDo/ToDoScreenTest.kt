package com.wgeplant.ui.toDo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wgeplant.model.domain.Task
import com.wgeplant.ui.theme.EventColors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ToDoScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavController
    private lateinit var viewModel: IToDoViewModel

    private val t1 = Task(
        taskId = "1",
        title = "Geschirr spülen",
        stateOfTask = false,
        color = EventColors.defaultEventColor,
        affectedUsers = emptyList(),
        description = null
    )
    private val t2 = Task(
        taskId = "2",
        title = "Staubsaugen",
        stateOfTask = false,
        color = EventColors.defaultEventColor,
        affectedUsers = emptyList(),
        description = null
    )

    private val ui = MutableStateFlow(ToDoUiState(tasks = listOf(t1, t2)))
    private val error = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(false)

    @Before
    fun setUp() {
        hiltRule.inject()

        navController = mock()
        viewModel = mock()

        whenever(viewModel.uiState).thenReturn(ui)
        whenever(viewModel.errorMessage).thenReturn(error)
        whenever(viewModel.isLoading).thenReturn(loading)

        composeTestRule.setContent {
            ToDoScreen(navController = navController, toDoViewModel = viewModel)
        }
    }

    @Test
    fun addFab_navigatesToCreateTask() {
        composeTestRule.onNodeWithContentDescription("Aufgabe hinzufügen").performClick()
        verify(viewModel).navigateToTaskCreation(navController)
    }

    @Test
    fun bottomBar_calendar_navigates() {
        composeTestRule
            .onNode(
                hasAnyDescendant(hasText("Kalender")) and hasClickAction(),
                useUnmergedTree = true
            )
            .performClick()
        verify(viewModel).navigateToCalendar(navController)
    }

    @Test
    fun clickingTask_togglesState() {
        composeTestRule
            .onNode(
                hasAnyDescendant(hasText("Geschirr spülen")) and hasClickAction(),
                useUnmergedTree = true
            )
            .performClick()
        verify(viewModel).changeTaskState(t1)
    }

    @Test
    fun longClickTask_navigatesToDetail() {
        composeTestRule
            .onNode(
                hasAnyDescendant(hasText("Staubsaugen")) and hasClickAction(),
                useUnmergedTree = true
            )
            .performTouchInput { longClick() }
        verify(viewModel).navigateToTask(t2, navController)
    }

    @Test
    fun noDate_header_shown() {
        composeTestRule.onNodeWithText("Kein Datum").assertIsDisplayed()
    }

    @Test
    fun errorMessage_shown() {
        error.value = "Error!"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Error!").assertIsDisplayed()
    }
}
