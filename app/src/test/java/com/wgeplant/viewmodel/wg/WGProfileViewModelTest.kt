package com.wgeplant.viewmodel.wg

import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.WG
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.ui.wg.WGProfileViewModel
import com.wgeplant.util.MainCoroutineRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WGProfileViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var profileInteractor: ManageWGProfileInteractor

    @Mock
    private lateinit var wgInteractor: ManageWGInteractor

    @Mock
    private lateinit var userRepo: UserRepo

    private lateinit var vm: WGProfileViewModel

    @Before
    fun setUp() = runTest {
        whenever(profileInteractor.getWGData()).thenReturn(flowOf(Result.Success(null)))
        whenever(profileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(emptyList())))
        whenever(profileInteractor.getAbsence(any())).thenReturn(flowOf(Result.Success(emptyList())))
        whenever(userRepo.getLocalUserId()).thenReturn(Result.Success("u-123"))

        vm = WGProfileViewModel(
            interactor = profileInteractor,
            wgInteractor = wgInteractor,
            userRepo = userRepo
        )
        advanceUntilIdle()
    }

    @Test
    fun `init loads WG data, members and current user id`() = runTest {
        verify(profileInteractor, atLeastOnce()).getWGData()
        verify(profileInteractor, atLeastOnce()).getWGMembers()
        verify(userRepo, atLeastOnce()).getLocalUserId()
        assertEquals("u-123", vm.uiState.value.currentUserId)
    }

    @Test
    fun `loadWGMembers success updates users, error sets errorMessage`() = runTest {
        val u = mock<User> { on { userId } doReturn "u1" }
        whenever(profileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(listOf(u))))
        vm.loadWGMembers()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.users.size)

        whenever(profileInteractor.getWGMembers())
            .thenReturn(flowOf(Result.Error(DomainError.NetworkError)))
        vm.loadWGMembers()
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)
    }

    @Test
    fun `loadWGData populates future absences and handles errors`() = runTest {
        val wg = mock<WG> {
            on { displayName } doReturn "MyWG"
            on { invitationCode } doReturn "INV123"
            on { profilePicture } doReturn "http://img"
        }
        val u1 = mock<User> { on { userId } doReturn "u1"; on { displayName } doReturn "Alex" }
        val today = LocalDate.now()
        val futureAbs = mock<Absence> {
            on { startDate } doReturn today
            on { endDate } doReturn today.plusDays(2)
        }
        val pastAbs = mock<Absence> {
            on { startDate } doReturn today.minusDays(5)
            on { endDate } doReturn today.minusDays(1)
        }

        whenever(profileInteractor.getWGData()).thenReturn(flowOf(Result.Success(wg)))
        whenever(profileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(listOf(u1))))
        whenever(profileInteractor.getAbsence(eq("u1")))
            .thenReturn(flowOf(Result.Success(listOf(pastAbs, futureAbs))))

        vm.loadWGData()
        advanceUntilIdle()

        assertEquals("MyWG", vm.uiState.value.wgName)
        assertEquals("INV123", vm.uiState.value.invitationCode)
        assertEquals(1, vm.uiState.value.users.size)
        assertEquals(1, vm.uiState.value.userAbsences["u1"]?.size)

        whenever(profileInteractor.getWGData())
            .thenReturn(flowOf(Result.Error(DomainError.NetworkError)))
        vm.loadWGData()
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)

        // members error path
        whenever(profileInteractor.getWGData()).thenReturn(flowOf(Result.Success(wg)))
        whenever(profileInteractor.getWGMembers())
            .thenReturn(flowOf(Result.Error(DomainError.NetworkError)))
        vm.loadWGData()
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)
    }

    @Test
    fun `toggleEditMode and toggleEditing update isEditing and clear error`() {
        vm.onWGNameChanged("   ")
        vm.saveEditing()
        assertNotNull(vm.uiState.value.wgNameError)

        vm.toggleEditMode()
        assertTrue(vm.uiState.value.isEditing)
        assertNull(vm.uiState.value.wgNameError)

        vm.toggleEditing()
        assertFalse(vm.uiState.value.isEditing)
        assertNull(vm.uiState.value.wgNameError)
    }

    @Test
    fun `onWGNameChanged updates wgName`() {
        vm.onWGNameChanged("New Name")
        assertEquals("New Name", vm.uiState.value.wgName)
    }

    @Test
    fun `saveEditing validates name variants`() = runTest {
        vm.toggleEditing()

        vm.onWGNameChanged("   ")
        vm.saveEditing()
        assertEquals("Name darf nicht leer sein.", vm.uiState.value.wgNameError)

        vm.onWGNameChanged("ABCDEFGHIJKLMNOP") // 16 chars
        vm.saveEditing()
        assertEquals("Maximal 15 Zeichen erlaubt.", vm.uiState.value.wgNameError)

        vm.onWGNameChanged("Bad#Name")
        vm.saveEditing()
        assertEquals("Nur Buchstaben, Ziffern, '.', '-' und Leerzeichen erlaubt.", vm.uiState.value.wgNameError)

        vm.onWGNameChanged("12345")
        vm.saveEditing()
        assertEquals("Mindestens ein Buchstabe erforderlich.", vm.uiState.value.wgNameError)

        verify(profileInteractor, never()).executeDisplayNameChange(any())
        assertTrue(vm.uiState.value.isEditing)
    }

    @Test
    fun `saveEditing success calls interactor and exits edit mode`() = runTest {
        whenever(profileInteractor.executeDisplayNameChange("My WG"))
            .thenReturn(Result.Success(Unit))
        vm.toggleEditing()
        vm.onWGNameChanged("My WG")
        vm.saveEditing()
        advanceUntilIdle()

        verify(profileInteractor).executeDisplayNameChange("My WG")
        assertFalse(vm.uiState.value.isEditing)
        assertNull(vm.uiState.value.wgNameError)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `saveEditing domain error sets errorMessage and closes edit`() = runTest {
        vm.toggleEditing()
        vm.onWGNameChanged("OkayName")
        whenever(profileInteractor.executeDisplayNameChange("OkayName"))
            .thenReturn(Result.Error(DomainError.NetworkError))

        vm.saveEditing()
        advanceUntilIdle()

        assertNotNull(vm.errorMessage.value)
        assertFalse(vm.uiState.value.isEditing)
    }

    @Test
    fun `saveEditing exception shows generic error and closes edit`() = runTest {
        vm.toggleEditing()
        vm.onWGNameChanged("OkayName")
        whenever(profileInteractor.executeDisplayNameChange("OkayName"))
            .thenThrow(RuntimeException("boom"))

        vm.saveEditing()
        advanceUntilIdle()

        assertEquals("Ein unerwarteter Fehler ist aufgetreten.", vm.errorMessage.value)
        assertFalse(vm.uiState.value.isEditing)
    }

    @Test
    fun `invitation dialog toggles flag`() {
        vm.showInvitationDialog()
        assertTrue(vm.uiState.value.showInvitationDialog)
        vm.hideInvitationDialog()
        assertFalse(vm.uiState.value.showInvitationDialog)
    }

    @Test
    fun `onUserSelected and clearSelectedUser`() = runTest {
        val u = mock<User> { on { userId } doReturn "u42"; on { displayName } doReturn "Chris" }
        whenever(profileInteractor.getWGMembers()).thenReturn(flowOf(Result.Success(listOf(u))))
        vm.loadWGMembers()
        advanceUntilIdle()

        vm.onUserSelected("u42")
        assertEquals("u42", vm.uiState.value.selectedUser?.userId)

        vm.clearSelectedUser()
        assertNull(vm.uiState.value.selectedUser)
    }

    @Test
    fun `removeUserFromWG success reloads members, error sets message`() = runTest {
        clearInvocations(profileInteractor)

        whenever(wgInteractor.executeMemberKickOut("u-9"))
            .thenReturn(Result.Success(Unit))
        vm.removeUserFromWG("u-9")
        advanceUntilIdle()
        verify(profileInteractor, atLeastOnce()).getWGMembers()

        whenever(wgInteractor.executeMemberKickOut("u-9"))
            .thenReturn(Result.Error(DomainError.NetworkError))
        vm.removeUserFromWG("u-9")
        advanceUntilIdle()
        assertEquals("Fehler beim Entfernen des Benutzers aus der WG.", vm.errorMessage.value)
    }

    @Test
    fun `undoEdits reloads WG data and toggles edit off`() = runTest {
        vm.toggleEditing()
        vm.undoEdits()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isEditing)
        verify(profileInteractor, atLeastOnce()).getWGData()
    }
}
