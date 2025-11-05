package com.wgeplant.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavController
    private lateinit var viewModel: ILoginViewModel

    private val ui = MutableStateFlow(LoginUiState())
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
            LoginScreen(navController = navController, loginViewModel = viewModel)
        }
    }

    @Test
    fun back_navigatesBack() {
        composeTestRule.onNodeWithContentDescription("Zurück").performClick()
        verify(viewModel).navigateBack(navController)
    }

    @Test
    fun inputs_callViewModel() {
        composeTestRule.onNodeWithText("E-Mail").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Passwort").performTextInput("secret123")
        verify(viewModel).onEmailChanged("test@example.com")
        verify(viewModel).onPasswordChanged("secret123")
    }

    @Test
    fun passwordVisibility_toggle() {
        composeTestRule.onNodeWithContentDescription("Passwort anzeigen/verbergen").performClick()
        verify(viewModel).onPasswordVisibilityChanged()
    }

    @Test
    fun login_click_callsLogin() {
        composeTestRule.onNode(hasText("Login") and hasClickAction()).performClick()
        verify(viewModel).login(navController)
    }

    @Test
    fun error_isShown() {
        error.value = "Error!"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Error!").assertIsDisplayed()
    }
}
