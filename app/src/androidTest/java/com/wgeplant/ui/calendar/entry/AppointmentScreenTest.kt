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
import java.time.LocalDateTime

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppointmentScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockAppointmentViewModel: IAppointmentViewModel

    private val initialUiState = AppointmentUiState(
        title = "Movie night",
        description = "Bring snacks!",
        startDate = LocalDateTime.of(2025, 4, 20, 20, 0),
        endDate = LocalDateTime.of(2025, 4, 20, 23, 0),
        wgMembers = listOf(
            WGMemberSelection("1", "Max", true),
            WGMemberSelection("2", "Lena", false),
            WGMemberSelection("3", "Tobi", true)
        ),
        color = EventColors.defaultEventColor,
        isValid = true
    )
    private val uiStateFlow = MutableStateFlow(initialUiState)

    @Before
    fun setUp() {
        mockNavController = mock()
        mockAppointmentViewModel = mock()

        whenever(mockAppointmentViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockAppointmentViewModel.isLoading).thenReturn(MutableStateFlow(false))
        whenever(mockAppointmentViewModel.errorMessage).thenReturn(MutableStateFlow(null))

        doNothing().whenever(mockAppointmentViewModel).navigateBack(any())
        doNothing().whenever(mockAppointmentViewModel).saveEntry(any())
        doNothing().whenever(mockAppointmentViewModel).delete(any())
        doNothing().whenever(mockAppointmentViewModel).undoEdits()

        whenever(mockAppointmentViewModel.onTitleChanged(any())).thenAnswer {
            val newTitle = it.getArgument<String>(0)
            uiStateFlow.value = uiStateFlow.value.copy(title = newTitle)
            null
        }

        whenever(mockAppointmentViewModel.onDescriptionChanged(any())).thenAnswer {
            val newDesc = it.getArgument<String>(0)
            uiStateFlow.value = uiStateFlow.value.copy(description = newDesc)
            null
        }

        whenever(mockAppointmentViewModel.onAssignmentChanged(any(), any())).thenAnswer {
            val id = it.getArgument<String>(0)
            val isSelected = it.getArgument<Boolean>(1)
            val updatedMembers = uiStateFlow.value.wgMembers.map { member ->
                if (member.id == id) member.copy(isSelected = isSelected) else member
            }
            uiStateFlow.value = uiStateFlow.value.copy(wgMembers = updatedMembers)
            null
        }

        whenever(mockAppointmentViewModel.setEditMode()).thenAnswer {
            uiStateFlow.value = uiStateFlow.value.copy(isEditing = true)
            null
        }

        whenever(mockAppointmentViewModel.saveEntry(mockNavController)).thenAnswer {
            uiStateFlow.value = uiStateFlow.value.copy(isEditing = true)
            null
        }

        whenever(mockAppointmentViewModel.undoEdits()).thenAnswer {
            uiStateFlow.value = uiStateFlow.value.copy(isEditing = false)
            null
        }

        composeTestRule.setContent {
            AppointmentScreen(navController = mockNavController, appointmentViewModel = mockAppointmentViewModel)
        }
    }

    @Test
    fun screenRendersCorrectly() {
        composeTestRule.onNodeWithText("Movie night").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.BACK).assertIsDisplayed()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.SAVE).assertDoesNotExist()
        composeTestRule.onNodeWithText(AppointmentScreenConstants.CANCEL).assertDoesNotExist()
    }

    @Test
    fun editButton_switchesToEditMode() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.DELETE).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.BACK).assertDoesNotExist()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.SAVE).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppointmentScreenConstants.CANCEL).assertIsDisplayed()
    }

    @Test
    fun cancelButton_switchesBackToViewMode() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.CANCEL).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.BACK).assertIsDisplayed()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.SAVE).assertDoesNotExist()
        composeTestRule.onNodeWithText(AppointmentScreenConstants.CANCEL).assertDoesNotExist()
    }

    @Test
    fun editMode_allowsTextChanges() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(AppointmentScreenConstants.TITLE_FIELD_TEST_TAG).performTextClearance()
        composeTestRule.onNodeWithTag(AppointmentScreenConstants.DESCRIPTION_FIELD_TEST_TAG).performTextClearance()
        composeTestRule.onNodeWithTag(AppointmentScreenConstants.TITLE_FIELD_TEST_TAG).performTextInput("Cooking night")
        composeTestRule.onNodeWithTag(AppointmentScreenConstants.DESCRIPTION_FIELD_TEST_TAG).performTextInput(
            "Bring ingredients!"
        )

        composeTestRule.runOnIdle {
            verify(mockAppointmentViewModel, atLeastOnce()).onTitleChanged("Cooking night")
            verify(mockAppointmentViewModel, atLeastOnce()).onDescriptionChanged("Bring ingredients!")
        }
    }

    @Test
    fun deleteButton_showsConfirmationDialog() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.DELETE).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.DELETE_APPOINTMENT).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppointmentScreenConstants.CANNOT_UNDO).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppointmentScreenConstants.DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppointmentScreenConstants.CANCEL).assertIsDisplayed()
    }

    @Test
    fun clickingDelete_onDialog_deletesAppointment() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.DELETE).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.DELETE).performClick()
        composeTestRule.waitForIdle()

        verify(mockAppointmentViewModel).delete(mockNavController)
    }

    @Test
    fun clicking_colorSelector_opensBottomSheet_andChangesColor() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        val initialColorName = EventColors.getEventColorName(initialUiState.color)

        composeTestRule.onNodeWithText(initialColorName).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.SELECT_COLOR).assertIsDisplayed()
        composeTestRule.onNodeWithTag("Blau").performClick()
        composeTestRule.waitForIdle()

        val selectedColor = EventColors.allEventColors.first { EventColors.getEventColorName(it) == "Blau" }

        verify(mockAppointmentViewModel).onColorChanged(selectedColor)
        composeTestRule.onNodeWithText(AppointmentScreenConstants.SELECT_COLOR).assertDoesNotExist()
    }

    @Test
    fun clicking_onParticipantCheckbox_callsOnAssignmentChanged() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("Max").performClick()
        composeTestRule.waitForIdle()

        verify(mockAppointmentViewModel).onAssignmentChanged("1", false)
    }

    @Test
    fun clickingSaveButton_callsSaveEntry() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.EDIT).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(AppointmentScreenConstants.SAVE).performClick()
        composeTestRule.waitForIdle()

        verify(mockAppointmentViewModel).saveEntry(mockNavController)
    }

    @Test
    fun backButton_callsNavigateBack() {
        composeTestRule.onNodeWithContentDescription(AppointmentScreenConstants.BACK).performClick()
        composeTestRule.waitForIdle()

        verify(mockAppointmentViewModel).navigateBack(mockNavController)
    }
}
