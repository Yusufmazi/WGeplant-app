package com.wgeplant.ui.wg
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class ChooseWGScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockChooseWGViewModel: IChooseWGViewModel
    private val uiStateFlow = MutableStateFlow(ChooseWGUiState())
    private val errorMessageFlow = MutableStateFlow<String?>(null)

    @Before
    fun setUp() {
        mockNavController = mock()
        mockChooseWGViewModel = mock()

        whenever(mockChooseWGViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockChooseWGViewModel.errorMessage).thenReturn(errorMessageFlow)

        composeTestRule.setContent {
            ChooseWGScreen(
                navController = mockNavController,
                chooseWGViewModel = mockChooseWGViewModel
            )
        }
    }

    @Test
    fun screen_displaysCorrectGreeting() {
        uiStateFlow.value = ChooseWGUiState(userDisplayName = "TestUser")
        composeTestRule.waitForIdle()

        val expectedGreeting = "${ChooseWGScreenConstants.GREETING}TestUser${ChooseWGScreenConstants.SMILEY}"
        composeTestRule.onNodeWithText(expectedGreeting)
            .assertIsDisplayed()
    }

    @Test
    fun screen_displaysBothButtonsAndChooseText() {
        composeTestRule.onNodeWithText(ChooseWGScreenConstants.JOIN_WG)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(ChooseWGScreenConstants.CREATE_WG)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(ChooseWGScreenConstants.CHOOSE_OPTION_TEXT)
            .assertIsDisplayed()
    }

    @Test
    fun joinWGButton_navigatesToJoinWG() {
        composeTestRule.onNodeWithText(ChooseWGScreenConstants.JOIN_WG)
            .performClick()

        verify(mockChooseWGViewModel).navigateToJoinWG(mockNavController)
    }

    @Test
    fun createWGButton_navigatesToCreateWG() {
        composeTestRule.onNodeWithText(ChooseWGScreenConstants.CREATE_WG)
            .performClick()

        verify(mockChooseWGViewModel).navigateToCreateWG(mockNavController)
    }

    @Test
    fun userProfileIcon_navigatesToUserProfile() {
        composeTestRule.onNodeWithContentDescription(ChooseWGScreenConstants.STANDARD_PROFILE_ICON)
            .performClick()

        verify(mockChooseWGViewModel).navigateToUserProfile(mockNavController)
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
    fun defaultProfileIcon_isDisplayed_whenUrlIsNull() {
        uiStateFlow.value = ChooseWGUiState(userProfileImageUrl = null)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(ChooseWGScreenConstants.STANDARD_PROFILE_ICON)
            .assertIsDisplayed()
    }
}
