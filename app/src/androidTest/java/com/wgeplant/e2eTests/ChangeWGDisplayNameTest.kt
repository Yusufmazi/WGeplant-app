package com.wgeplant.e2eTests

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wgeplant.ui.MainActivity
import com.wgeplant.ui.calendar.MonthlyCalendarScreenConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * An e2e test class for the logout functionality.
 *
 * This test simulates the full process:
 * 1. registration of a user
 * 2. creation of a wg
 * 3. navigation to the wg profile
 * 4. changing wg display name
 * 5. clean up: delete account (includes wg deletion, since wg without members gets deleted automatically)
 *
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class ChangeWGDisplayNameTest {

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

    @Before
    fun setUp() {
        val randomNumber = Random.nextInt(1000, 10000)
        val uniqueEmail = "${randomNumber}changeWGName@example.com"
        TestUtils.registerUser(
            composeTestRule = composeTestRule,
            email = uniqueEmail,
            password = "Password123?",
            displayName = E2EConstants.TEST_USER
        )
        TestUtils.createWG(composeTestRule = composeTestRule)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(MonthlyCalendarScreenConstants.CALENDAR).fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * This method is executed after each test to reset the situation of the user.
     */
    @After
    fun teardown() {
        try {
            // navigate to user profile
            composeTestRule.onNodeWithText("Profil").performClick()
            TestUtils.accountDeletion(composeTestRule = composeTestRule)
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Test the creation of a wg.
     */
    @Test
    fun testSuccessfulWGDisplayNameChangeProcess() {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE).isDisplayed()
        }
        Thread.sleep(2000)
        composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText("WG").isDisplayed()
        }
        composeTestRule.onNodeWithContentDescription("Bearbeiten").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText("WG-Name").isDisplayed()
        }
        composeTestRule.onNodeWithText(E2EConstants.TEST_E2E_WG).performTextClearance()
        composeTestRule.onNodeWithText("WG-Name").performTextInput("Test Edit")
        composeTestRule.onNodeWithContentDescription("Speichern").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText("Test Edit").isDisplayed()
        }
    }
}
