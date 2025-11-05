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
class CreateWGScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockCreateWGViewModel: ICreateWGViewModel
    private val uiStateFlow = MutableStateFlow(CreateWGUiState())
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        mockNavController = mock()
        mockCreateWGViewModel = mock()

        whenever(mockCreateWGViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockCreateWGViewModel.errorMessage).thenReturn(errorMessageFlow)
        whenever(mockCreateWGViewModel.isLoading).thenReturn(isLoadingFlow)

        composeTestRule.setContent {
            CreateWGScreen(navController = mockNavController, createWGViewModel = mockCreateWGViewModel)
        }
    }

    @Test
    fun screen_displaysAllElements() {
        composeTestRule.onNodeWithContentDescription(CreateWGScreenConstants.APP_LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            CreateWGScreenConstants.APP_TITLE_PART1 + CreateWGScreenConstants.APP_TITLE_PART2
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(CreateWGScreenConstants.WG_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText(CreateWGScreenConstants.CREATE_WG).assertIsDisplayed()
    }

    @Test
    fun backButton_navigatesBack() {
        composeTestRule.onNodeWithContentDescription(CreateWGScreenConstants.BACK)
            .performClick()

        verify(mockCreateWGViewModel).navigateBack(mockNavController)
    }

    @Test
    fun wgNameInput_acceptsTextAndChangesState() {
        val testName = "TestWG"
        composeTestRule.onNodeWithText(CreateWGScreenConstants.WG_NAME)
            .performTextInput(testName)

        verify(mockCreateWGViewModel).onWGNameChanged(testName)
    }

    @Test
    fun createWGButton_triggersCreateWG_whenEnabled() {
        composeTestRule.onNodeWithTag(CreateWGScreenConstants.CREATE_WG_BUTTON_TEST_TAG)
            .performClick()

        verify(mockCreateWGViewModel).createWG(mockNavController)
    }

    @Test
    fun createWGButton_showsLoadingState_whenLoading() {
        isLoadingFlow.value = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CreateWGScreenConstants.LOADING_INDICATOR_TEST_TAG)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(CreateWGScreenConstants.CREATE_WG_BUTTON_TEST_TAG)
            .assertIsNotEnabled()
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
    fun wgNameError_isDisplayed_whenUiStateHasError() {
        val validationError = "Error!"
        uiStateFlow.value = uiStateFlow.value.copy(wgNameError = validationError)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(validationError)
            .assertIsDisplayed()
    }
}
