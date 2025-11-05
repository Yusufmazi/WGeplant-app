package com.wgeplant.viewmodel.user

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.model.interactor.userManagement.AuthInteractor
import com.wgeplant.model.interactor.userManagement.ManageUserProfileInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.ui.user.UserProfileViewModel
import com.wgeplant.util.MainCoroutineRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock private lateinit var mockAuthInteractor: AuthInteractor

    @Mock private lateinit var mockProfileInteractor: ManageUserProfileInteractor

    @Mock private lateinit var mockWGInteractor: ManageWGInteractor

    @Mock private lateinit var mockWGProfileInteractor: ManageWGProfileInteractor

    @Mock private lateinit var mockInitialDataInteractor: GetInitialDataInteractor

    @Mock private lateinit var mockNavController: NavController

    private lateinit var viewModel: UserProfileViewModel

    @Before
    fun setUp() = runTest {
        whenever(mockProfileInteractor.getUserData()).thenReturn(flowOf(Result.Success(null)))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(false)))

        viewModel = UserProfileViewModel(
            mockAuthInteractor,
            mockProfileInteractor,
            mockWGInteractor,
            mockWGProfileInteractor,
            mockInitialDataInteractor
        )

        advanceUntilIdle()
    }

    @Test
    fun `init triggers loadUserProfile and updates basic fields`() = runTest {
        verify(mockProfileInteractor).getUserData()
        verify(mockInitialDataInteractor).isUserInWG()
        assertEquals("", viewModel.uiState.value.userName)
        assertEquals(null, viewModel.uiState.value.profilePictureUri)
        assertEquals(null, viewModel.uiState.value.localProfileImage)
        assertEquals(false, viewModel.uiState.value.isInWG)
    }

    @Test
    fun `loadUserProfile handles domain error from getUserData`() = runTest {
        whenever(mockProfileInteractor.getUserData())
            .thenReturn(flowOf(Result.Error(DomainError.NetworkError)))
        viewModel.loadUserProfile()
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `loadUserProfile handles exception with generic error`() = runTest {
        whenever(mockProfileInteractor.getUserData()).thenReturn(flow { throw RuntimeException("boom") })
        viewModel.loadUserProfile()
        advanceUntilIdle()
        assertEquals("An unexpected error occurred.", viewModel.errorMessage.value)
    }

    // endregion

    // region display name editing

    @Test
    fun `onDisplayNameChanged updates ui state`() = runTest {
        viewModel.onDisplayNameChanged("Alice")
        assertEquals("Alice", viewModel.uiState.value.userName)
    }

    @Test
    fun `saveEditing with invalid name sets nameError and does not call interactor`() = runTest {
        viewModel.toggleEdit()
        viewModel.onDisplayNameChanged("   ")
        viewModel.saveEditing()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.nameError)
        verify(mockProfileInteractor, never()).executeDisplayNameChange(any())
        assertTrue(viewModel.uiState.value.isEditing)
    }

    @Test
    fun `saveEditing with valid name calls interactor and exits edit mode`() = runTest {
        whenever(mockProfileInteractor.executeDisplayNameChange("John Doe"))
            .thenReturn(Result.Success(Unit))
        viewModel.toggleEdit()
        viewModel.onDisplayNameChanged("John Doe")
        viewModel.saveEditing()
        advanceUntilIdle()
        verify(mockProfileInteractor).executeDisplayNameChange("John Doe")
        assertNull(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isEditing)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `saveEditing handles domain error`() = runTest {
        whenever(mockProfileInteractor.executeDisplayNameChange("Bob-1"))
            .thenReturn(Result.Error(DomainError.NetworkError))
        viewModel.toggleEdit()
        viewModel.onDisplayNameChanged("Bob-1")
        viewModel.saveEditing()
        advanceUntilIdle()
        verify(mockProfileInteractor).executeDisplayNameChange("Bob-1")
        assertFalse(viewModel.uiState.value.isEditing)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `saveEditing handles unexpected exception with generic error`() = runTest {
        whenever(mockProfileInteractor.executeDisplayNameChange(any())).thenThrow(RuntimeException("bad"))
        viewModel.toggleEdit()
        viewModel.onDisplayNameChanged("Clara")
        viewModel.saveEditing()
        advanceUntilIdle()
        assertEquals("An unexpected error occurred.", viewModel.errorMessage.value)
        assertFalse(viewModel.uiState.value.isEditing)
    }

    @Test
    fun `toggleEdit flips isEditing and clears nameError`() = runTest {
        viewModel.onDisplayNameChanged("   ")
        viewModel.saveEditing() // nameError set eder
        assertNotNull(viewModel.uiState.value.nameError)

        viewModel.toggleEdit()
        assertTrue(viewModel.uiState.value.isEditing)
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `logout success sets isLoggedOut`() = runTest {
        whenever(mockAuthInteractor.executeLogout()).thenReturn(Result.Success(Unit))
        viewModel.logout()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoggedOut)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `logout domain error sets errorMessage`() = runTest {
        whenever(mockAuthInteractor.executeLogout()).thenReturn(Result.Error(DomainError.NetworkError))
        viewModel.logout()
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
        assertFalse(viewModel.uiState.value.isLoggedOut)
    }

    @Test
    fun `deleteAccount success sets flag and navigates to AUTH_START with popUpTo root`() = runTest {
        whenever(mockAuthInteractor.executeAccountDeletion()).thenReturn(Result.Success(Unit))
        val navBlockCaptor = argumentCaptor<NavOptionsBuilder.() -> Unit>()
        viewModel.deleteAccount(mockNavController)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAccountDeleted)
        verify(mockNavController).navigate(eq(Routes.AUTH_START), navBlockCaptor.capture())
        assertNotNull(navBlockCaptor.firstValue)
    }

    @Test
    fun `deleteAccount domain error sets errorMessage and does not navigate`() = runTest {
        whenever(mockAuthInteractor.executeAccountDeletion()).thenReturn(Result.Error(DomainError.NetworkError))
        viewModel.deleteAccount(mockNavController)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAccountDeleted)
        assertNotNull(viewModel.errorMessage.value)
        verify(mockNavController, never()).navigate(eq(Routes.AUTH_START), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `leaveWG success updates isInWG to false`() = runTest {
        whenever(mockWGInteractor.executeLeaving()).thenReturn(Result.Success(Unit))
        viewModel.leaveWG()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isInWG)
    }

    @Test
    fun `leaveWG error shows error message`() = runTest {
        whenever(mockWGInteractor.executeLeaving()).thenReturn(Result.Error(DomainError.NetworkError))
        viewModel.leaveWG()
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `open and close absence dialog update both flag and uiState`() = runTest {
        viewModel.openAbsenceDialog()
        assertTrue(viewModel.showAbsenceDialog)
        assertTrue(viewModel.uiState.value.showAbsenceDialog)
        viewModel.closeAbsenceDialog()
        assertFalse(viewModel.showAbsenceDialog)
        assertFalse(viewModel.uiState.value.showAbsenceDialog)
    }

    @Test
    fun `addAbsence success closes dialog and reloads profile`() = runTest {
        whenever(mockProfileInteractor.executeAbsenceEntry(any<LocalDate>(), any<LocalDate>()))
            .thenReturn(Result.Success(Unit))
        viewModel.openAbsenceDialog()
        viewModel.addAbsence(LocalDate.now().toString(), LocalDate.now().plusDays(1).toString())
        advanceUntilIdle()
        assertFalse(viewModel.showAbsenceDialog)
        verify(mockProfileInteractor, atLeast(1)).getUserData()
    }

    @Test
    fun `addAbsence error sets errorMessage`() = runTest {
        whenever(mockProfileInteractor.executeAbsenceEntry(any<LocalDate>(), any<LocalDate>()))
            .thenReturn(Result.Error(DomainError.NetworkError))
        viewModel.addAbsence(LocalDate.now().toString(), LocalDate.now().plusDays(1).toString())
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addAbsence with invalid date shows exception message`() = runTest {
        viewModel.addAbsence("2025-13-40", "2025-01-01")
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `deleteAbsence success reloads profile`() = runTest {
        whenever(mockWGProfileInteractor.executeAbsenceDeletion("a1"))
            .thenReturn(Result.Success(Unit))
        viewModel.deleteAbsence("a1")
        advanceUntilIdle()
        verify(mockProfileInteractor, atLeastOnce()).getUserData()
    }

    @Test
    fun `deleteAbsence error handled as domain error`() = runTest {
        whenever(mockWGProfileInteractor.executeAbsenceDeletion("a2"))
            .thenReturn(Result.Error(DomainError.NetworkError))
        viewModel.deleteAbsence("a2")
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `editAbsence triggers interactor and reloads`() = runTest {
        whenever(mockWGProfileInteractor.executeAbsenceEditing(any(), any<LocalDate>(), any<LocalDate>()))
            .thenReturn(Result.Success(Unit))
        viewModel.editAbsence("idX", LocalDate.now(), LocalDate.now().plusDays(2))
        advanceUntilIdle()
        verify(mockWGProfileInteractor).executeAbsenceEditing(any(), any<LocalDate>(), any<LocalDate>())
        verify(mockProfileInteractor, atLeastOnce()).getUserData()
    }

    @Test
    fun `openConfirmDialog sets title message and shows dialog, onConfirmAction executes and closes`() = runTest {
        var called = false
        viewModel.openConfirmDialog("Title", "Message") { called = true }
        assertTrue(viewModel.showConfirmDialog)
        assertEquals("Title", viewModel.confirmDialogTitle)
        assertEquals("Message", viewModel.confirmDialogMessage)

        viewModel.onConfirmAction()
        assertTrue(called)
        assertFalse(viewModel.showConfirmDialog)
    }

    @Test
    fun `closeConfirmDialog hides the dialog`() = runTest {
        viewModel.openConfirmDialog("T", "M") {}
        viewModel.closeConfirmDialog()
        assertFalse(viewModel.showConfirmDialog)
    }

    @Test
    fun `undoEdits reloads profile and toggles edit off`() = runTest {
        viewModel.toggleEdit()
        viewModel.undoEdits()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isEditing)
        verify(mockProfileInteractor, atLeastOnce()).getUserData()
    }
}
