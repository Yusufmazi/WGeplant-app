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
class CreateAppointmentScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockAppointmentViewModel: IAppointmentViewModel
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(false)
    private val initialUiState = AppointmentUiState(
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
        mockAppointmentViewModel = mock()

        whenever(mockAppointmentViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockAppointmentViewModel.errorMessage).thenReturn(errorMessageFlow)
        whenever(mockAppointmentViewModel.isLoading).thenReturn(isLoadingFlow)

        composeTestRule.setContent {
            CreateAppointmentScreen(navController = mockNavController, appointmentViewModel = mockAppointmentViewModel)
        }
    }

    @Test
    fun backButton_navigatesBack() {
        composeTestRule.onNodeWithContentDescription(CreateAppointmentScreenConstants.BACK).performClick()
        verify(mockAppointmentViewModel).navigateBack(mockNavController)
    }

    @Test
    fun textInputs_acceptsTextAndChangesState() {
        composeTestRule.onNodeWithTag(CreateAppointmentScreenConstants.TITLE_FIELD_TEST_TAG).performTextInput(
            "Movie night"
        )
        composeTestRule.onNodeWithTag(CreateAppointmentScreenConstants.DESCRIPTION_FIELD_TEST_TAG).performTextInput(
            "Bring snacks!"
        )

        verify(mockAppointmentViewModel).onTitleChanged("Movie night")
        verify(mockAppointmentViewModel).onDescriptionChanged("Bring snacks!")
    }

    @Test
    fun clicking_colorSelector_opensBottomSheet_andChangesColor() {
        composeTestRule.onNodeWithText(EventColors.getEventColorName(EventColors.defaultEventColor)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(CreateAppointmentScreenConstants.SELECT_COLOR).assertIsDisplayed()
        composeTestRule.onNodeWithTag("Blau").performClick()
        composeTestRule.waitForIdle()

        val selectedColor = EventColors.allEventColors.first { EventColors.getEventColorName(it) == "Blau" }

        verify(mockAppointmentViewModel).onColorChanged(selectedColor)
        composeTestRule.onNodeWithText(CreateAppointmentScreenConstants.SELECT_COLOR).assertDoesNotExist()
    }

    @Test
    fun clicking_onParticipantCheckbox_callsOnAssignmentChanged() {
        composeTestRule.onNodeWithTag("Max").performClick()
        composeTestRule.waitForIdle()

        verify(mockAppointmentViewModel).onAssignmentChanged("1", true)
    }

    @Test
    fun clickingSaveButton_callsSaveEntry() {
        composeTestRule.onNodeWithText(CreateAppointmentScreenConstants.SAVE).performClick()
        composeTestRule.waitForIdle()

        verify(mockAppointmentViewModel).saveEntry(mockNavController)
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
    fun clickingNewTask_inDropdown_navigatesToOtherEntryCreation() {
        composeTestRule.onNodeWithTag(CreateAppointmentScreenConstants.DROP_DOWN_MENU_TEST_TAG).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(CreateAppointmentScreenConstants.TASK).performClick()
        composeTestRule.waitForIdle()

        verify(mockAppointmentViewModel).navigateToOtherEntryCreation(mockNavController)
    }
}
