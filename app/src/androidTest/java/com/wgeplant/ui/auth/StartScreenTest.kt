package com.wgeplant.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StartScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockNavigationController: NavController
    private lateinit var mockStartViewModel: IStartViewModel

    @Before
    fun setUp() {
        mockNavigationController = mock()
        mockStartViewModel = mock()

        composeTestRule.setContent {
            StartScreen(navController = mockNavigationController, startViewModel = mockStartViewModel)
        }
    }

    @Test
    fun loginButton_navigatesToLoginScreen() {
        composeTestRule.onNodeWithText(StartScreenConstants.BUTTON_LOGIN).performClick()
        verify(mockStartViewModel).navigateToLogin(mockNavigationController)
    }

    @Test
    fun registerButton_navigatesToRegisterScreen() {
        composeTestRule.onNodeWithText(StartScreenConstants.BUTTON_REGISTER).performClick()
        verify(mockStartViewModel).navigateToRegister(mockNavigationController)
    }

    @Test
    fun screen_displaysAllElements() {
        composeTestRule.onNodeWithContentDescription(StartScreenConstants.DESCRIPTION_LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithText(StartScreenConstants.APP_TITLE_PART1 + StartScreenConstants.APP_TITLE_PART2)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(StartScreenConstants.BUTTON_LOGIN).assertIsDisplayed()
        composeTestRule.onNodeWithText(StartScreenConstants.BUTTON_REGISTER).assertIsDisplayed()
    }
}
