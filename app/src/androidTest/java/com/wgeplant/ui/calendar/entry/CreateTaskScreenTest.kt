package com.wgeplant.ui.calendar.entry
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateTaskScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockTaskViewModel: ITaskViewModel
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(false)
    private val initialUiState = TaskUiState(
        wgMembers = listOf(
            WGMemberSelection(id = "1", name = "Max", isSelected = false),
            WGMemberSelection(id = "2", name = "Lena", isSelected = false),
            WGMemberSelection(id = "3", name = "Tobi", isSelected = false)
        )
    )
    private val uiStateFlow = MutableStateFlow(initialUiState)

    @Before
    fun setUp() {
        mockNavController = mock()
        mockTaskViewModel = mock()

        whenever(mockTaskViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockTaskViewModel.errorMessage).thenReturn(errorMessageFlow)
        whenever(mockTaskViewModel.isLoading).thenReturn(isLoadingFlow)

        composeTestRule.setContent {
            CreateTaskScreen(navController = mockNavController, createTaskViewModel = mockTaskViewModel)
        }
    }

    @Test
    fun backButton_navigatesBack() {
        composeTestRule.onNodeWithContentDescription(CreateTaskScreenConstants.BACK).performClick()
        verify(mockTaskViewModel).navigateBack(mockNavController)
    }

    @Test
    fun textInputs_acceptsTextAndChangesState() {
        composeTestRule.onNodeWithTag(CreateTaskScreenConstants.TITLE_FIELD_TEST_TAG).performTextInput("Supermarket")
        composeTestRule.onNodeWithTag(CreateTaskScreenConstants.DESCRIPTION_FIELD_TEST_TAG).performTextInput(
            "Fruits and milk."
        )

        verify(mockTaskViewModel).onTitleChanged("Supermarket")
        verify(mockTaskViewModel).onDescriptionChanged("Fruits and milk.")
    }

    @Test
    fun clicking_colorSelector_opensBottomSheet_andChangesColor() {
        composeTestRule.onNodeWithText(EventColors.getEventColorName(EventColors.defaultEventColor)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(CreateTaskScreenConstants.SELECT_COLOR).assertIsDisplayed()
        composeTestRule.onNodeWithTag("Blau").performClick()
        composeTestRule.waitForIdle()

        val selectedColor = EventColors.allEventColors.first { EventColors.getEventColorName(it) == "Blau" }

        verify(mockTaskViewModel).onColorChanged(selectedColor)
        composeTestRule.onNodeWithText(CreateTaskScreenConstants.SELECT_COLOR).assertDoesNotExist()
    }

    @Test
    fun clicking_onParticipantCheckbox_callsOnAssignmentChanged() {
        composeTestRule.onNodeWithTag("Max").performClick()
        composeTestRule.waitForIdle()

        verify(mockTaskViewModel).onAssignmentChanged("1", true)
    }

    @Test
    fun clickingSaveButton_callsSaveEntry() {
        composeTestRule.onNodeWithText(CreateTaskScreenConstants.SAVE).performClick()
        composeTestRule.waitForIdle()

        verify(mockTaskViewModel).saveEntry(mockNavController)
    }

    @Test
    fun errorMessage_isDisplayed_whenStateHasError() {
        val testErrorMessage = "Error!"
        errorMessageFlow.value = testErrorMessage
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(testErrorMessage)
            .assertIsDisplayed()
    }

    @Test
    fun clickingNewAppointment_inDropdown_navigatesToOtherEntryCreation() {
        composeTestRule.onNodeWithTag(CreateTaskScreenConstants.DROP_DOWN_MENU_TEST_TAG).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(CreateTaskScreenConstants.APPOINTMENT).performClick()
        composeTestRule.waitForIdle()

        verify(mockTaskViewModel).navigateToOtherEntryCreation(mockNavController)
    }
}
