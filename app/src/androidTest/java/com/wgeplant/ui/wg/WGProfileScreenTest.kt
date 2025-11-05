package com.wgeplant.ui.wg

import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wgeplant.model.domain.User
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
class WGProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private lateinit var nav: NavController
    private lateinit var vm: IWGProfileViewModel

    private val ui = MutableStateFlow(
        WGProfileUiState(
            wgName = "TestWG",
            users = emptyList(),
            isEditing = false,
            currentUserId = ""
        )
    )
    private val error = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(false)

    @Before
    fun setup() {
        hiltRule.inject()

        nav = mock()
        vm = mock()
        whenever(vm.uiState).thenReturn(ui)
        whenever(vm.errorMessage).thenReturn(error)
        whenever(vm.isLoading).thenReturn(loading)

        composeRule.setContent {
            WGProfileScreen(navController = nav, viewModel = vm)
        }
    }

    @Test
    fun back_navigatesBack() {
        composeRule.onNodeWithContentDescription("Zurück", useUnmergedTree = true).performClick()
        verify(vm).navigateBack(nav)
    }

    @Test
    fun open_invitation_close() {
        composeRule.onNodeWithText("Mitbewohner einladen").performClick()
        verify(vm).showInvitationDialog()

        ui.value = ui.value.copy(showInvitationDialog = true, invitationCode = "ABC123")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Schließen", useUnmergedTree = true).performClick()
        verify(vm).hideInvitationDialog()
    }

    @Test
    fun remove_other_user_confirms_and_callsRemove() {
        val me = user("u1", "Me")
        val other = user("u2", "Max")
        ui.value = ui.value.copy(isEditing = true, currentUserId = "u1", users = listOf(me, other))
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Entfernen", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText("Ja", useUnmergedTree = true).performClick()

        verify(vm).removeUserFromWG("u2")
    }

    @Test
    fun click_user_opens_absence_dialog_then_close() {
        val me = user("u1", "Me")
        val other = user("u2", "Max")
        ui.value = ui.value.copy(isEditing = false, currentUserId = "u1", users = listOf(me, other))
        composeRule.waitForIdle()

        composeRule.onNode(
            hasAnyDescendant(hasText("Max")) and hasClickAction(),
            useUnmergedTree = true
        ).performClick()
        verify(vm).onUserSelected("u2")

        ui.value = ui.value.copy(selectedUser = other, userAbsences = mapOf("u2" to emptyList()))
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Schließen", useUnmergedTree = true).performClick()
        verify(vm).clearSelectedUser()
    }

    @Test
    fun edit_changeName_save() {
        composeRule.onNodeWithContentDescription("Bearbeiten", useUnmergedTree = true).performClick()
        verify(vm).toggleEditMode()

        ui.value = ui.value.copy(isEditing = true)
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction(), useUnmergedTree = true).performTextInput(" New WG")
        verify(vm).onWGNameChanged(any())

        val saveNode = composeRule.onNode(hasContentDescription("Speichern"), useUnmergedTree = true)
        runCatching { saveNode.performClick() }.getOrElse {
            saveNode.onParent().performClick()
        }
        verify(vm).saveEditing()
    }

    private fun user(id: String, name: String) =
        User(userId = id, displayName = name, profilePicture = null)
}
