package com.wgeplant.ui.auth

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
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
class RegisterScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavController
    private lateinit var mockRegisterViewModel: IRegisterViewModel
    private val uiStateFlow = MutableStateFlow(RegisterUiState())
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        mockNavController = mock()
        mockRegisterViewModel = mock()

        whenever(mockRegisterViewModel.uiState).thenReturn(uiStateFlow)
        whenever(mockRegisterViewModel.errorMessage).thenReturn(errorMessageFlow)
        whenever(mockRegisterViewModel.isLoading).thenReturn(isLoadingFlow)

        composeTestRule.setContent {
            RegisterScreen(
                navController = mockNavController,
                registerViewModel = mockRegisterViewModel
            )
        }
    }

    @Test
    fun registerButton_isInitiallyEnabled() {
        composeTestRule.onNodeWithText(RegisterScreenConstants.BUTTON_REGISTER)
            .assertIsEnabled()
    }

    @Test
    fun backButton_navigatesBack() {
        composeTestRule.onNodeWithContentDescription(RegisterScreenConstants.DESCRIPTION_BACK)
            .performClick()

        verify(mockRegisterViewModel).navigateBack(mockNavController)
    }

    @Test
    fun textInputs_acceptsTextAndChangesState() {
        composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_EMAIL)
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_PASSWORD)
            .performTextInput("password123")
        composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_DISPLAY_NAME)
            .performTextInput("TestUser")

        verify(mockRegisterViewModel).onEmailChanged("test@example.com")
        verify(mockRegisterViewModel).onPasswordChanged("password123")
        verify(mockRegisterViewModel).onDisplayNameChanged("TestUser")
    }

    @Test
    fun passwordVisibility_togglesWhenClicked() {
        composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_PASSWORD)
            .assert(hasSetTextAction())

        composeTestRule.onNodeWithContentDescription(RegisterScreenConstants.DESCRIPTION_PASSWORD_TOGGLE)
            .performClick()

        verify(mockRegisterViewModel).onPasswordVisibilityChanged()
    }

    @Test
    fun registerButton_showsLoadingState_whenLoading() {
        isLoadingFlow.value = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(RegisterScreenConstants.LOADING_INDICATOR_TEST_TAG)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(RegisterScreenConstants.REGISTER_BUTTON_TEST_TAG)
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
}
