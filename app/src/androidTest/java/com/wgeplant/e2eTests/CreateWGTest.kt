package com.wgeplant.e2eTests

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wgeplant.ui.MainActivity
import com.wgeplant.ui.calendar.MonthlyCalendarScreenConstants
import com.wgeplant.ui.wg.ChooseWGScreenConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * An e2e test class for the creation of a wg.
 *
 * This test simulates the full process:
 * 1. registration of a user
 * 2. navigation to the wg creation screen
 * 3. fill out the creation formular
 * 4. check if the screen switches to the calendar screen
 * 5. clean up: delete account (includes wg deletion, since wg without members gets deleted automatically)
 *
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class CreateWGTest {

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
        val uniqueEmail = "${randomNumber}createWG@example.com"
        TestUtils.registerUser(
            composeTestRule = composeTestRule,
            email = uniqueEmail,
            password = "Password123?",
            displayName = "Test User"
        )
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithText(ChooseWGScreenConstants.CHOOSE_OPTION_TEXT).isDisplayed()
        }
    }

    @After
    fun teardown() {
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE).isDisplayed()
            }
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
            composeTestRule.onNodeWithTag(MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE).performClick()
            composeTestRule.onNodeWithText("Profil").performClick()
            TestUtils.accountDeletion(composeTestRule = composeTestRule)
        } catch (e: Exception) {
            // ignore
        }
    }

    @Test
    fun testSuccessfulWgCreationProcess() {
        TestUtils.createWG(composeTestRule = composeTestRule)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(MonthlyCalendarScreenConstants.CALENDAR).fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
