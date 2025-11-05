package com.wgeplant.model.interactor.userManagement

import android.net.Uri
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.UNKNOWN_ERROR
import com.wgeplant.model.domain.User
import com.wgeplant.model.repository.AbsenceRepo
import com.wgeplant.model.repository.StorageRepo
import com.wgeplant.model.repository.UserRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@ExperimentalCoroutinesApi
class ManageUserProfileInteractorImplTest {

    companion object {
        private const val TEST_USER_ID = "testUserId123"
        private const val TEST_DISPLAY_NAME = "New Display Name"
        private const val TEST_URL = "testUrl"
        private const val TEST_EXISTING_URL = "existingUrl"
        private const val TEST_ERROR_MESSAGE = "Test Error Message"
        private val testUri = mock<Uri>()
        private val startDate = LocalDate.now()
        private val endDate = LocalDate.now().plusDays(1)
    }

    // mocks for dependencies
    private lateinit var mockUserRepo: UserRepo
    private lateinit var mockAbsenceRepo: AbsenceRepo
    private lateinit var mockStorageRepo: StorageRepo

    // class that gets tested
    private lateinit var manageUserProfileInteractor: ManageUserProfileInteractorImpl

    @Before
    fun setUp() {
        mockUserRepo = mock()
        mockAbsenceRepo = mock()
        mockStorageRepo = mock()
        manageUserProfileInteractor = ManageUserProfileInteractorImpl(
            userRepo = mockUserRepo,
            absenceRepo = mockAbsenceRepo,
            storageRepo = mockStorageRepo
        )
    }

    @Test
    fun `getUserData success`() = runTest {
        val mockUser = User(userId = TEST_USER_ID, displayName = "Test User")

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.getUserById(TEST_USER_ID)).thenReturn(flowOf(Result.Success(mockUser)))

        val result = manageUserProfileInteractor.getUserData().first()

        assertTrue(result is Result.Success)
        assertEquals(mockUser, result.data)
        verify(mockUserRepo).getLocalUserId()
    }

    @Test
    fun `getUserData failure on getLocalUserId`() = runTest {
        val expectedError = DomainError.NotFoundError

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Error(expectedError))

        val result = manageUserProfileInteractor.getUserData().first()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getUserData failure on getUserById`() = runTest {
        val expectedError = DomainError.NotFoundError

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.getUserById(TEST_USER_ID)).thenReturn(flowOf(Result.Error(expectedError)))

        val result = manageUserProfileInteractor.getUserData().first()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getUserData exception on getUserById`() = runTest {
        val testException = RuntimeException(TEST_ERROR_MESSAGE)
        val expectedDomainError = DomainError.Unknown(testException)
        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.getUserById(TEST_USER_ID)).thenReturn(
            flow {
                throw testException
            }
        )
        val resultFlow = manageUserProfileInteractor.getUserData()
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
    fun `executeDisplayNameChange success`() = runTest {
        val mockUser = User(userId = TEST_USER_ID, displayName = TEST_DISPLAY_NAME)

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.getUserById(TEST_USER_ID)).thenReturn(flowOf(Result.Success(mockUser)))
        whenever(mockUserRepo.updateUser(mockUser)).thenReturn(Result.Success(Unit))

        val result = manageUserProfileInteractor.executeDisplayNameChange(TEST_DISPLAY_NAME)

        assertTrue(result is Result.Success)
        verify(mockUserRepo).getLocalUserId()
        verify(mockUserRepo).updateUser(mockUser)
    }

    @Test
    fun `executeDisplayNameChange failure on updateUser`() = runTest {
        val mockUser = User(userId = TEST_USER_ID, displayName = TEST_DISPLAY_NAME)
        val expectedError = DomainError.NotFoundError

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.getUserById(TEST_USER_ID)).thenReturn(flowOf(Result.Success(mockUser)))
        whenever(mockUserRepo.updateUser(mockUser)).thenReturn(Result.Error(expectedError))

        val result = manageUserProfileInteractor.executeDisplayNameChange(TEST_DISPLAY_NAME)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `executeProfilePictureChange success without existing profile picture`() = runTest {
        val mockUser = User(userId = TEST_USER_ID, displayName = TEST_DISPLAY_NAME)

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.getUserById(TEST_USER_ID)).thenReturn(flowOf(Result.Success(mockUser)))
        whenever(mockStorageRepo.uploadUserImage(TEST_USER_ID, testUri)).thenReturn(Result.Success(TEST_URL))
        val expectedUpdatedUser = mockUser.copy(profilePicture = TEST_URL)
        whenever(mockUserRepo.updateUser(expectedUpdatedUser)).thenReturn(Result.Success(Unit))

        val result = manageUserProfileInteractor.executeProfilePictureChange(testUri)

        assertTrue(result is Result.Success)
        verify(mockUserRepo).getLocalUserId()
        verify(mockStorageRepo).uploadUserImage(TEST_USER_ID, testUri)
        verify(mockUserRepo).updateUser(expectedUpdatedUser)
    }

    @Test
    fun `executeProfilePictureChange success with existing profile picture`() = runTest {
        val mockUser = User(userId = TEST_USER_ID, displayName = TEST_DISPLAY_NAME, profilePicture = TEST_EXISTING_URL)

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.getUserById(TEST_USER_ID)).thenReturn(flowOf(Result.Success(mockUser)))
        whenever(mockStorageRepo.deleteUserImage()).thenReturn(Result.Success(Unit))
        whenever(mockStorageRepo.uploadUserImage(TEST_USER_ID, testUri)).thenReturn(Result.Success(TEST_URL))
        val expectedUpdatedUser = mockUser.copy(profilePicture = TEST_URL)
        whenever(mockUserRepo.updateUser(expectedUpdatedUser)).thenReturn(Result.Success(Unit))

        val result = manageUserProfileInteractor.executeProfilePictureChange(testUri)

        assertTrue(result is Result.Success)
        verify(mockUserRepo).getLocalUserId()
        verify(mockStorageRepo).uploadUserImage(TEST_USER_ID, testUri)
        verify(mockUserRepo).updateUser(expectedUpdatedUser)
    }

    @Test
    fun `executeAbsenceEntry success`() = runTest {
        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        val newAbsence = Absence(
            userId = TEST_USER_ID,
            startDate = startDate,
            endDate = endDate
        )
        whenever(mockAbsenceRepo.createAbsence(newAbsence)).thenReturn(Result.Success(Unit))
        val result = manageUserProfileInteractor.executeAbsenceEntry(startDate, endDate)
        assertTrue(result is Result.Success)
        verify(mockUserRepo).getLocalUserId()
        verify(mockAbsenceRepo).createAbsence(newAbsence)
    }

    @Test
    fun `executeAbsenceEntry failure on getLocalUserId`() = runTest {
        val expectedError = DomainError.NotFoundError

        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Error(expectedError))

        val result = manageUserProfileInteractor.executeAbsenceEntry(mock(), mock())

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockUserRepo).getLocalUserId()
    }
}
