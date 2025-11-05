package com.wgeplant.e2eTests

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wgeplant.ui.MainActivity
import com.wgeplant.ui.wg.ChooseWGScreenConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * An e2e test class for the registration screen, simulating a complete new user registration.
 *
 * This test simulates the full process:
 * 1. clicking the "Registrieren" button
 * 2. filling out the registration form
 * 3. checking if the registration is successful through checking the screen switch to the ChooseWG screen
 * 5. clean up: delete account
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RegistrationTest {

    /**
     * The Hilt rule that initializes Hilt before the test runs.
     * It ensures all dependencies are correctly provided.
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * The main rule for testing composables, which now starts the main app activity.
     */
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @After
    fun teardown() {
        // This gets executed after each test to reset the situation of the user.
        // simulate the account deletion of the user
        try {
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
            composeTestRule.onNodeWithContentDescription(ChooseWGScreenConstants.STANDARD_PROFILE_ICON)
                .performClick()
            TestUtils.accountDeletion(composeTestRule = composeTestRule)
        } catch (e: Exception) {
            // ignore
        }
    }

    @Test
    fun testSuccessfulRegistrationProcess() {
        val randomNumber = Random.nextInt(1000, 10000)
        val uniqueEmail = "${randomNumber}registration@example.com"
        TestUtils.registerUser(
            composeTestRule = composeTestRule,
            email = uniqueEmail,
            password = "Password123?",
            displayName = E2EConstants.TEST_USER
        )
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithText(
                ChooseWGScreenConstants.GREETING +
                    E2EConstants.TEST_USER +
                    ChooseWGScreenConstants.SMILEY
            )
                .isDisplayed()
        }
    }
}
