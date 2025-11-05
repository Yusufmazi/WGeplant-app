package com.wgeplant.e2eTests

import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wgeplant.ui.MainActivity
import com.wgeplant.ui.calendar.MonthlyCalendarScreenConstants
import com.wgeplant.ui.calendar.entry.TaskScreenConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * An e2e test class for the creation of a task.
 *
 * This test simulates the full process:
 * 1. registration of a user
 * 2. navigation to the to-do list screen
 * 3. fill out the creation formular
 * 4. check if the task gets display in the to-do list
 * 5. clean up: delete account (includes wg deletion, since wg without members gets deleted automatically)
 *
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class CreateTaskTest {

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
        val uniqueEmail = "${randomNumber}createTask@example.com"
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

    @After
    fun teardown() {
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onNodeWithTag(E2EConstants.TEST_TAG_WG_PROFILE_ICON).isDisplayed()
            }
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
            composeTestRule.onNodeWithTag(E2EConstants.TEST_TAG_WG_PROFILE_ICON).performClick()
            composeTestRule.onNodeWithText("Profil").performClick()
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule.onNodeWithText("Account löschen").isDisplayed()
            }
            TestUtils.accountDeletion(composeTestRule = composeTestRule)
        } catch (e: Exception) {
            // ignore
        }
    }

    @Test
    fun testSuccessfulTaskCreationProcess() {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithText("To-Do").isDisplayed()
        }
        Thread.sleep(2000)
        composeTestRule
            .onNode(
                hasAnyDescendant(hasText("To-Do")) and hasClickAction(),
                useUnmergedTree = true
            )
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("To-Do").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Aufgabe hinzufügen").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText("Titel").isDisplayed()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Titel").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Titel").performTextInput("E2E-Test-Task")
        TestUtils.hideKeyboard(composeTestRule = composeTestRule)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText(E2EConstants.TEST_USER).isDisplayed()
        }
        composeTestRule.onNodeWithTag(E2EConstants.TEST_USER).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(TaskScreenConstants.SAVE).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText("E2E-Test-Task").isDisplayed()
        }
    }
}
