package com.wgeplant.model.interactor.wgManagement

import android.net.Uri
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.UNKNOWN_ERROR
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.WG
import com.wgeplant.model.repository.AbsenceRepo
import com.wgeplant.model.repository.StorageRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManageWGProfileInteractorImplTest {
    companion object {
        private const val TEST_WG_ID = "testWGId"
        private const val TEST_WG_NAME = "Test WG"
        private const val TEST_INVITATION_CODE = "testInvitationCode"
        private const val TEST_NEW_WG_NAME = "New Test WG"
        private const val TEST_URL = "testUrl"
        private const val TEST_USER_ID = "testUserId"
        private const val TEST_ABSENCE_ID = "testAbsenceId"
        private const val TEST_ERROR_MESSAGE = "Test Error Message"
        private val testUri = mock<Uri>()
        private val testStartDate = LocalDate.now()
        private val testEndDate = LocalDate.now().plusDays(1)
    }
    private lateinit var mockWGRepo: WGRepo
    private lateinit var mockAbsenceRepo: AbsenceRepo
    private lateinit var mockUserRepo: UserRepo
    private lateinit var mockStorageRepo: StorageRepo

    // class that gets tested
    private lateinit var manageWGProfileInteractor: ManageWGProfileInteractorImpl

    @Before
    fun setUp() {
        mockWGRepo = mock()
        mockAbsenceRepo = mock()
        mockUserRepo = mock()
        mockStorageRepo = mock()
        manageWGProfileInteractor = ManageWGProfileInteractorImpl(
            wgRepo = mockWGRepo,
            absenceRepo = mockAbsenceRepo,
            userRepo = mockUserRepo,
            storageRepo = mockStorageRepo
        )
    }

    @Test
    fun `getWGData success`() = runTest {
        val testWG = WG(
            wgId = TEST_WG_ID,
            displayName = TEST_WG_NAME,
            invitationCode = TEST_INVITATION_CODE,
            profilePicture = null
        )
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Success(testWG)))
        val result = manageWGProfileInteractor.getWGData()
        assertTrue(result.first() is Result.Success)
        assertEquals(testWG, (result.first() as Result.Success).data)
    }

    @Test
    fun `getWGData failure on getWGData`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Error(expectedError)))
        val result = manageWGProfileInteractor.getWGData()
        assertTrue(result.first() is Result.Error)
        assertEquals(expectedError, (result.first() as Result.Error).error)
    }

    @Test
    fun `getWGData exception on getWG`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockWGRepo.getWG()).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = manageWGProfileInteractor.getWGData()
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
        assertEquals(expectedDomainError, actualDomainError, TEST_ERROR_MESSAGE)
    }

    @Test
    fun `getWGMembers success`() = runTest {
        val testUsers = listOf(
            User(
                userId = TEST_USER_ID,
                displayName = TEST_WG_NAME,
                profilePicture = null
            )
        )
        whenever(mockUserRepo.getAllUsers()).thenReturn(flowOf(Result.Success(testUsers)))
        val result = manageWGProfileInteractor.getWGMembers()
        assertTrue(result.first() is Result.Success)
        assertEquals(testUsers, (result.first() as Result.Success).data)
    }

    @Test
    fun `getWGMembers exception on getAllUsers`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockUserRepo.getAllUsers()).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = manageWGProfileInteractor.getWGMembers()
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
        assertEquals(expectedDomainError, actualDomainError, TEST_ERROR_MESSAGE)
    }

    @Test
    fun `executeInvitationCodeInquiry success`() = runTest {
        val testWG = WG(
            wgId = TEST_WG_ID,
            displayName = TEST_WG_NAME,
            invitationCode = TEST_INVITATION_CODE,
            profilePicture = null
        )
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Success(testWG)))
        val result = manageWGProfileInteractor.executeInvitationCodeInquiry()
        assertTrue(result is Result.Success)
        assertEquals(TEST_INVITATION_CODE, result.data)
    }

    @Test
    fun `executeInvitationCodeInquiry failure on getWG`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Error(expectedError)))
        val result = manageWGProfileInteractor.executeInvitationCodeInquiry()
        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `executeDisplayNameChange success`() = runTest {
        val testWG = WG(
            wgId = TEST_WG_ID,
            displayName = TEST_WG_NAME,
            invitationCode = TEST_INVITATION_CODE,
            profilePicture = null
        )
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Success(testWG)))
        val newDisplayName = TEST_NEW_WG_NAME
        val expectedUpdatedWG = testWG.copy(displayName = newDisplayName)
        whenever(mockWGRepo.updateWG(expectedUpdatedWG)).thenReturn(Result.Success(Unit))
        val result = manageWGProfileInteractor.executeDisplayNameChange(newDisplayName)
        assertTrue(result is Result.Success)
        verify(mockWGRepo).updateWG(expectedUpdatedWG)
    }

    @Test
    fun `executeDisplayNameChange failure on getWG`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Error(expectedError)))
        val result = manageWGProfileInteractor.executeDisplayNameChange(TEST_NEW_WG_NAME)
        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `executeProfilePictureChange success without existing profile picture`() = runTest {
        val testWG = WG(
            wgId = TEST_WG_ID,
            displayName = TEST_WG_NAME,
            invitationCode = TEST_INVITATION_CODE,
            profilePicture = null
        )
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Success(testWG)))
        whenever(mockStorageRepo.uploadWGImage(testWG.wgId, testUri)).thenReturn(Result.Success(TEST_URL))
        val expectedUpdatedWG = testWG.copy(profilePicture = TEST_URL)
        whenever(mockWGRepo.updateWG(expectedUpdatedWG)).thenReturn(Result.Success(Unit))
        val result = manageWGProfileInteractor.executeProfilePictureChange(testUri)
        assertTrue(result is Result.Success)
        verify(mockStorageRepo).uploadWGImage(testWG.wgId, testUri)
    }

    @Test
    fun `executeProfilePictureChange success with existing profile picture`() = runTest {
        val testWG = WG(
            wgId = TEST_WG_ID,
            displayName = TEST_WG_NAME,
            invitationCode = TEST_INVITATION_CODE,
            profilePicture = TEST_URL
        )
        whenever(mockWGRepo.getWG()).thenReturn(flowOf(Result.Success(testWG)))
        whenever(mockStorageRepo.deleteWGImage(TEST_WG_ID)).thenReturn(Result.Success(Unit))
        whenever(mockStorageRepo.uploadWGImage(testWG.wgId, testUri)).thenReturn(Result.Success(TEST_URL))
        val expectedUpdatedWG = testWG.copy(profilePicture = TEST_URL)
        whenever(mockWGRepo.updateWG(expectedUpdatedWG)).thenReturn(Result.Success(Unit))
        val result = manageWGProfileInteractor.executeProfilePictureChange(testUri)
        assertTrue(result is Result.Success)
        verify(mockStorageRepo).uploadWGImage(testWG.wgId, testUri)
        verify(mockWGRepo).updateWG(expectedUpdatedWG)
    }

    @Test
    fun `executeAbsenceEditing success`() = runTest {
        val testAbsence = Absence(
            absenceId = TEST_ABSENCE_ID,
            userId = TEST_USER_ID,
            startDate = testStartDate,
            endDate = testEndDate
        )
        whenever(mockAbsenceRepo.getAbsenceById(TEST_ABSENCE_ID)).thenReturn(flowOf(Result.Success(testAbsence)))
        val expectedUpdatedAbsence = testAbsence.copy(startDate = testStartDate, endDate = testEndDate)
        whenever(mockAbsenceRepo.updateAbsence(expectedUpdatedAbsence)).thenReturn(Result.Success(Unit))
        val result = manageWGProfileInteractor.executeAbsenceEditing(TEST_ABSENCE_ID, testStartDate, testEndDate)
        assertTrue(result is Result.Success)
        verify(mockAbsenceRepo).updateAbsence(expectedUpdatedAbsence)
    }

    @Test
    fun `executeAbsenceEditing failure on getAbsence`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockAbsenceRepo.getAbsenceById(TEST_ABSENCE_ID)).thenReturn(flowOf(Result.Error(expectedError)))
        val result = manageWGProfileInteractor.executeAbsenceEditing(TEST_ABSENCE_ID, testStartDate, testEndDate)
        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `executeAbsenceDeletion success`() = runTest {
        whenever(mockAbsenceRepo.deleteAbsence(TEST_ABSENCE_ID)).thenReturn(Result.Success(Unit))
        val result = manageWGProfileInteractor.executeAbsenceDeletion(TEST_ABSENCE_ID)
        assertTrue(result is Result.Success)
        verify(mockAbsenceRepo).deleteAbsence(TEST_ABSENCE_ID)
    }

    @Test
    fun `getAbsence success`() = runTest {
        val testAbsences = listOf(
            Absence(
                absenceId = TEST_ABSENCE_ID,
                userId = TEST_USER_ID,
                startDate = testStartDate,
                endDate = testEndDate
            )
        )
        whenever(mockAbsenceRepo.getAbsencesByUserId(TEST_USER_ID)).thenReturn(flowOf(Result.Success(testAbsences)))
        val result = manageWGProfileInteractor.getAbsence(TEST_USER_ID)
        assertTrue(result.first() is Result.Success)
        assertEquals(testAbsences, (result.first() as Result.Success).data)
    }

    @Test
    fun `getAbsence failure on getAbsencesByUserId`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockAbsenceRepo.getAbsencesByUserId(TEST_USER_ID)).thenReturn(flowOf(Result.Error(expectedError)))
        val result = manageWGProfileInteractor.getAbsence(TEST_USER_ID)
        assertTrue(result.first() is Result.Error)
        assertEquals(expectedError, (result.first() as Result.Error).error)
    }

    @Test
    fun `getAbsence exception on getAbsencesByUserId`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockAbsenceRepo.getAbsencesByUserId(TEST_USER_ID)).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = manageWGProfileInteractor.getAbsence(TEST_USER_ID)
        val actualResult = resultFlow.first()
        assertTrue(actualResult is Result.Error<*>, TEST_ERROR_MESSAGE)
        val errorResult = actualResult as Result.Error<DomainError>
        assertTrue(errorResult.error is DomainError.Unknown, TEST_ERROR_MESSAGE)
        val actualDomainError = errorResult.error
        assertEquals(testException.localizedMessage ?: UNKNOWN_ERROR, actualDomainError.message, TEST_ERROR_MESSAGE)
        assertEquals(testException, actualDomainError.originalThrowable, TEST_ERROR_MESSAGE)
        assertEquals(expectedDomainError, actualDomainError, TEST_ERROR_MESSAGE)
    }
}
