package com.wgeplant.e2eTests

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.wgeplant.ui.auth.RegisterScreenConstants
import com.wgeplant.ui.wg.ChooseWGScreenConstants
import com.wgeplant.ui.wg.CreateWGScreenConstants

object E2EConstants {
    const val TEST_USER = "Test User"
    const val TEST_E2E_WG = "E2E-Test-WG"
    const val ACCOUNT_DELETION = "Account löschen"
    const val CONFIRMATION = "Ja"
    const val TEST_TAG_WG_PROFILE_ICON = "WG Profile Icon"
}
object TestUtils {

    fun registerUser(
        composeTestRule: AndroidComposeTestRule<*, *>,
        email: String,
        password: String,
        displayName: String
    ) {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithText(RegisterScreenConstants.BUTTON_REGISTER).isDisplayed()
        }
        composeTestRule.onNodeWithText(RegisterScreenConstants.BUTTON_REGISTER).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_EMAIL).isDisplayed()
        }
        composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_EMAIL).performTextInput(email)
        composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_PASSWORD).performTextInput(password)
        composeTestRule.onNodeWithText(RegisterScreenConstants.LABEL_DISPLAY_NAME).performTextInput(displayName)
        hideKeyboard(composeTestRule)
        composeTestRule.onNodeWithTag(RegisterScreenConstants.REGISTER_BUTTON_TEST_TAG).performClick()
    }

    fun createWG(composeTestRule: AndroidComposeTestRule<*, *>) {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithText(
                ChooseWGScreenConstants.GREETING + E2EConstants.TEST_USER + ChooseWGScreenConstants.SMILEY
            ).isDisplayed()
        }
        Thread.sleep(2000)
        composeTestRule.onNodeWithText(ChooseWGScreenConstants.CREATE_WG).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText(CreateWGScreenConstants.CREATE_WG).isDisplayed()
        }
        composeTestRule.onNodeWithText(CreateWGScreenConstants.WG_NAME).performTextInput(E2EConstants.TEST_E2E_WG)
        hideKeyboard(composeTestRule)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText(CreateWGScreenConstants.CREATE_WG).isDisplayed()
        }
        composeTestRule.onNodeWithText(CreateWGScreenConstants.CREATE_WG).performClick()
    }

    fun accountDeletion(composeTestRule: AndroidComposeTestRule<*, *>) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText(E2EConstants.ACCOUNT_DELETION).isDisplayed()
        }
        composeTestRule.onNodeWithText(E2EConstants.ACCOUNT_DELETION).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText(E2EConstants.CONFIRMATION).isDisplayed()
        }
        composeTestRule.onNodeWithText(E2EConstants.CONFIRMATION).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithText(RegisterScreenConstants.BUTTON_REGISTER).isDisplayed()
        }
    }

    fun hideKeyboard(composeTestRule: AndroidComposeTestRule<*, *>): Boolean {
        return try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                composeTestRule.activity.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
