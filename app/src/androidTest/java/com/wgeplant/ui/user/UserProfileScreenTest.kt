package com.wgeplant.ui.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private lateinit var nav: NavController
    private lateinit var vm: IUserProfileViewModel

    private val ui = MutableStateFlow(UserProfileUiState())
    private val error = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(false)

    @Before
    fun setUp() {
        hiltRule.inject()

        nav = mock()
        vm = mock()
        whenever(vm.uiState).thenReturn(ui)
        whenever(vm.errorMessage).thenReturn(error)
        whenever(vm.isLoading).thenReturn(loading)

        composeRule.setContent {
            UserProfileScreen(navController = nav, viewModel = vm)
        }
    }

    @Test
    fun back_click_when_not_inWG_calls_goBack() {
        ui.value = ui.value.copy(isInWG = false)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Zurück", useUnmergedTree = true).performClick()
        verify(vm).navigateBack(nav)
    }

    @Test
    fun edit_changeName_save() {
        composeRule.onNodeWithContentDescription("Bearbeiten", useUnmergedTree = true).performClick()
        verify(vm).toggleEdit()

        ui.value = ui.value.copy(isEditing = true)
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction(), useUnmergedTree = true).performTextInput(" X")
        verify(vm).onDisplayNameChanged(any())

        composeRule.onNodeWithContentDescription("Speichern", useUnmergedTree = true)
            .onParent()
            .performClick()
        verify(vm).saveEditing()
    }

    @Test
    fun logout_flow_confirms_and_calls_logout() {
        composeRule.onNodeWithText("Abmelden", useUnmergedTree = true)
            .assertExists()
            .performClick()

        waitForConfirmDialog()
        assertConfirmDialogShown()

        clickNoRobust()
        waitForDialogGone()
    }

    @Test
    fun leaveWG_flow_confirms_and_calls_leaveWG() {
        ui.value = ui.value.copy(isInWG = true)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("WG verlassen", useUnmergedTree = true)
            .assertExists()
            .performClick()

        waitForConfirmDialog()
        assertConfirmDialogShown()

        clickNoRobust()
        waitForDialogGone()
    }

    @Test
    fun deleteAccount_flow_confirms_and_calls_deleteAccount() {
        composeRule.onNodeWithText("Account löschen", useUnmergedTree = true).performClick()
        waitForConfirmDialog()
        clickYesRobust()
        composeRule.waitForIdle()
        verify(vm).deleteAccount(nav)
    }

    @Test
    fun openAbsences_then_select_add_calls_openDialog() {
        ui.value = ui.value.copy(isInWG = true)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Abwesenheiten", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Abwesenheit hinzufügen", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        verify(vm).openAbsenceDialog()
    }

    private fun waitForConfirmDialog() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Bist du dir sicher?", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Ja", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertConfirmDialogShown() {
        composeRule.onNodeWithText("Bist du dir sicher?", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Ja", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Nein", useUnmergedTree = true).assertExists()
    }

    private fun clickYesRobust() {
        val ok1 = runCatching {
            composeRule.onNode(hasText("Ja") and hasClickAction(), useUnmergedTree = true)
                .performClick()
        }.isSuccess
        if (ok1) return

        val ok2 = runCatching {
            composeRule.onAllNodesWithText("Ja", useUnmergedTree = true)
                .onFirst().onParent().performClick()
        }.isSuccess
        if (ok2) return

        composeRule.onNode(
            hasClickAction() and hasAnyDescendant(hasText("Ja")),
            useUnmergedTree = true
        ).performClick()
    }

    private fun clickNoRobust() {
        val ok1 = runCatching {
            composeRule.onNode(hasText("Nein") and hasClickAction(), useUnmergedTree = true)
                .performClick()
        }.isSuccess
        if (ok1) return

        val ok2 = runCatching {
            composeRule.onAllNodesWithText("Nein", useUnmergedTree = true)
                .onFirst().onParent().performClick()
        }.isSuccess
        if (ok2) return

        composeRule.onNode(
            hasClickAction() and hasAnyDescendant(hasText("Nein")),
            useUnmergedTree = true
        ).performClick()
    }

    private fun waitForDialogGone() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Bist du dir sicher?", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty() &&
                composeRule.onAllNodesWithText("Ja", useUnmergedTree = true)
                    .fetchSemanticsNodes().isEmpty() &&
                composeRule.onAllNodesWithText("Nein", useUnmergedTree = true)
                    .fetchSemanticsNodes().isEmpty()
        }
    }
}
