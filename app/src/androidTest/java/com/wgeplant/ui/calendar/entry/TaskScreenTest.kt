package com.wgeplant.ui.calendar.entry
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wgeplant.ui.theme.EventColors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockTaskViewModel: ITaskViewModel

    private val initialUiState = TaskUiState(
        title = "Supermarket",
        date = LocalDate.now(),
        wgMembers = listOf(
            WGMemberSelection(id = "1", name = "Max", isSelected = true),
            WGMemberSelection(id = "2", name = "Lena", isSelected = false),
            WGMemberSelection(id = "3", name = "Tobi", isSelected = false)
        ),
        color = EventColors.defaultEventColor,
        description = "Fruits and Milk.",
        isValid = true
    )
    private val uiStateFlow = MutableStateFlow(initialUiState)

    @Before
    fun setUp() {
        mockNavController = mock()
        mockTaskViewModel = mock()

        whenever(mockTaskViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockTaskViewModel.isLoading).thenReturn(MutableStateFlow(false))
        whenever(mockTaskViewModel.errorMessage).thenReturn(MutableStateFlow(null))

        doNothing().whenever(mockTaskViewModel).navigateBack(any())
        doNothing().whenever(mockTaskViewModel).saveEntry(any())
        doNothing().whenever(mockTaskViewModel).delete(any())
        doNothing().whenever(mockTaskViewModel).undoEdits()

        whenever(mockTaskViewModel.onTitleChanged(any())).thenAnswer {
            val newTitle = it.getArgument<String>(0)
            uiStateFlow.value = uiStateFlow.value.copy(title = newTitle)
            null
        }

        whenever(mockTaskViewModel.onDescriptionChanged(any())).thenAnswer {
            val newDesc = it.getArgument<String>(0)
            uiStateFlow.value = uiStateFlow.value.copy(description = newDesc)
            null
        }

        whenever(mockTaskViewModel.onAssignmentChanged(any(), any())).thenAnswer {
            val id = it.getArgument<String>(0)
            val isSelected = it.getArgument<Boolean>(1)
            val updatedMembers = uiStateFlow.value.wgMembers.map { member ->
                if (member.id == id) member.copy(isSelected = isSelected) else member
            }
            uiStateFlow.value = uiStateFlow.value.copy(wgMembers = updatedMembers)
            null
        }

        whenever(mockTaskViewModel.setEditMode()).thenAnswer {
            uiStateFlow.value = uiStateFlow.value.copy(isEditing = true)
            null
        }

        whenever(mockTaskViewModel.saveEntry(mockNavController)).thenAnswer {
            uiStateFlow.value = uiStateFlow.value.copy(isEditing = true)
            null
        }

        whenever(mockTaskViewModel.undoEdits()).thenAnswer {
            uiStateFlow.value = uiStateFlow.value.copy(isEditing = false)
            null
        }

        composeTestRule.setContent {
            TaskScreen(navController = mockNavController, taskViewModel = mockTaskViewModel)
        }
    }

    @Test
    fun screenRendersCorrectly() {
        composeTestRule.onNodeWithText("Supermarket").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.BACK).assertIsDisplayed()

        composeTestRule.onNodeWithText(TaskScreenConstants.SAVE).assertDoesNotExist()
        composeTestRule.onNodeWithText(TaskScreenConstants.CANCEL).assertDoesNotExist()
    }

    @Test
    fun editButton_switchesToEditMode() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.DELETE).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.BACK).assertDoesNotExist()

        composeTestRule.onNodeWithText(TaskScreenConstants.SAVE).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskScreenConstants.CANCEL).assertIsDisplayed()
    }

    @Test
    fun cancelButton_switchesBackToViewMode() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TaskScreenConstants.CANCEL).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.BACK).assertIsDisplayed()

        composeTestRule.onNodeWithText(TaskScreenConstants.SAVE).assertDoesNotExist()
        composeTestRule.onNodeWithText(TaskScreenConstants.CANCEL).assertDoesNotExist()
    }

    @Test
    fun editMode_allowsTextChanges() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TaskScreenConstants.TITLE_FIELD_TEST_TAG).performTextClearance()
        composeTestRule.onNodeWithTag(TaskScreenConstants.DESCRIPTION_FIELD_TEST_TAG).performTextClearance()
        composeTestRule.onNodeWithTag(TaskScreenConstants.TITLE_FIELD_TEST_TAG).performTextInput("Dishwasher")
        composeTestRule.onNodeWithTag(TaskScreenConstants.DESCRIPTION_FIELD_TEST_TAG).performTextInput(
            "Our kitchen is a mess!"
        )

        composeTestRule.runOnIdle {
            verify(mockTaskViewModel, atLeastOnce()).onTitleChanged("Dishwasher")
            verify(mockTaskViewModel, atLeastOnce()).onDescriptionChanged("Our kitchen is a mess!")
        }
    }

    @Test
    fun deleteButton_showsConfirmationDialog() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.DELETE).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TaskScreenConstants.DELETE_TASK).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskScreenConstants.CANNOT_UNDO).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskScreenConstants.DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskScreenConstants.CANCEL).assertIsDisplayed()
    }

    @Test
    fun clickingDelete_onDialog_deletesTask() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.DELETE).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TaskScreenConstants.DELETE).performClick()
        composeTestRule.waitForIdle()

        verify(mockTaskViewModel).delete(mockNavController)
    }

    @Test
    fun clicking_colorSelector_opensBottomSheet_andChangesColor() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        val initialColorName = EventColors.getEventColorName(initialUiState.color)

        composeTestRule.onNodeWithText(initialColorName).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TaskScreenConstants.SELECT_COLOR).assertIsDisplayed()
        composeTestRule.onNodeWithTag("Blau").performClick()
        composeTestRule.waitForIdle()

        val selectedColor = EventColors.allEventColors.first { EventColors.getEventColorName(it) == "Blau" }

        verify(mockTaskViewModel).onColorChanged(selectedColor)
        composeTestRule.onNodeWithText(TaskScreenConstants.SELECT_COLOR).assertDoesNotExist()
    }

    @Test
    fun clicking_onParticipantCheckbox_callsOnAssignmentChanged() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("Max").performClick()
        composeTestRule.waitForIdle()

        verify(mockTaskViewModel).onAssignmentChanged("1", false)
    }

    @Test
    fun clickingSaveButton_callsSaveEntry() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TaskScreenConstants.SAVE).performClick()
        composeTestRule.waitForIdle()

        verify(mockTaskViewModel).saveEntry(mockNavController)
    }

    @Test
    fun backButton_callsNavigateBack() {
        composeTestRule.onNodeWithContentDescription(TaskScreenConstants.BACK).performClick()
        composeTestRule.waitForIdle()

        verify(mockTaskViewModel).navigateBack(mockNavController)
    }
}
