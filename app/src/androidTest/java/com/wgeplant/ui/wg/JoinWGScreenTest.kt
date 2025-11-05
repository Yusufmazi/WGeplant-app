package com.wgeplant.ui.wg
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class JoinWGScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockJoinWGViewModel: IJoinWGViewModel
    private val uiStateFlow = MutableStateFlow(JoinWGUiState())
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        mockNavController = mock()
        mockJoinWGViewModel = mock()

        whenever(mockJoinWGViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockJoinWGViewModel.errorMessage).thenReturn(errorMessageFlow)
        whenever(mockJoinWGViewModel.isLoading).thenReturn(isLoadingFlow)

        composeTestRule.setContent {
            JoinWGScreen(
                navController = mockNavController,
                joinWGViewModel = mockJoinWGViewModel
            )
        }
    }

    @Test
    fun screen_displaysAllElements() {
        composeTestRule.onNodeWithContentDescription(CreateWGScreenConstants.APP_LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            CreateWGScreenConstants.APP_TITLE_PART1 + CreateWGScreenConstants.APP_TITLE_PART2
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(JoinWGScreenConstants.INVITATION_CODE).assertIsDisplayed()
        composeTestRule.onNodeWithText(JoinWGScreenConstants.JOIN_WG).assertIsDisplayed()
    }

    @Test
    fun backButton_navigatesBack() {
        composeTestRule.onNodeWithContentDescription(CreateWGScreenConstants.BACK)
            .performClick()

        verify(mockJoinWGViewModel).navigateBack(mockNavController)
    }

    @Test
    fun invitationCodeInput_acceptsTextAndChangesState() {
        val testInvitationCode = "123456"
        composeTestRule.onNodeWithText(JoinWGScreenConstants.INVITATION_CODE).performTextInput(testInvitationCode)

        verify(mockJoinWGViewModel).onInvitationCodeChanged(testInvitationCode)
    }

    @Test
    fun joinWGButton_triggersJoinWG_whenEnabled() {
        composeTestRule.onNodeWithTag(JoinWGScreenConstants.JOIN_WG_BUTTON_TEST_TAG).performClick()

        verify(mockJoinWGViewModel).joinWG(mockNavController)
    }

    @Test
    fun joinWGButton_showsLoadingState_whenLoading() {
        isLoadingFlow.value = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(JoinWGScreenConstants.LOADING_INDICATOR_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(JoinWGScreenConstants.JOIN_WG_BUTTON_TEST_TAG).assertIsNotEnabled()
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
    fun invitationCodeError_isDisplayed_whenUiStateHasError() {
        val testError = "Error!"
        uiStateFlow.value = uiStateFlow.value.copy(invitationCodeError = testError)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(testError)
            .assertIsDisplayed()
    }
}
