package com.wgeplant.model.interactor.wgManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.repository.DeleteLocalDataRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ManageWGInteractorImplTest {

    companion object {
        private const val TEST_DISPLAY_NAME = "Test WG"
        private const val TEST_INVITATION_CODE = "testInvitationCode"
        private const val TEST_USER_ID = "testUserId"
    }

    // mocks for dependencies
    private lateinit var mockWGRepo: WGRepo
    private lateinit var mockUserRepo: UserRepo
    private lateinit var mockInitialDataRepo: InitialDataRepo
    private lateinit var mockDeleteLocalDataRepo: DeleteLocalDataRepo

    // class that gets tested
    private lateinit var manageWGInteractor: ManageWGInteractorImpl

    @Before
    fun setUp() {
        mockWGRepo = mock()
        mockUserRepo = mock()
        mockInitialDataRepo = mock()
        mockDeleteLocalDataRepo = mock()
        manageWGInteractor = ManageWGInteractorImpl(
            wgRepo = mockWGRepo,
            userRepo = mockUserRepo,
            initialDataRepo = mockInitialDataRepo,
            deleteLocalDataRepo = mockDeleteLocalDataRepo
        )
    }

    @Test
    fun `executeCreation success`() = runTest {
        whenever(mockWGRepo.createWG(TEST_DISPLAY_NAME))
            .thenReturn(Result.Success(TEST_INVITATION_CODE))
        whenever(mockUserRepo.joinWGByInvitationCode(TEST_INVITATION_CODE))
            .thenReturn(Result.Success(Unit))

        val result = manageWGInteractor.executeCreation(TEST_DISPLAY_NAME)

        assertTrue(result is Result.Success)
        verify(mockWGRepo).createWG(TEST_DISPLAY_NAME)
        verify(mockUserRepo).joinWGByInvitationCode(TEST_INVITATION_CODE)
    }

    @Test
    fun `executeCreation failure on joinWGByInvitationCode`() = runTest {
        val expectedError = DomainError.NetworkError // error example
        whenever(mockWGRepo.createWG(TEST_DISPLAY_NAME))
            .thenReturn(Result.Success(TEST_INVITATION_CODE))
        whenever(mockUserRepo.joinWGByInvitationCode(TEST_INVITATION_CODE))
            .thenReturn(Result.Error(expectedError))

        val result = manageWGInteractor.executeCreation(TEST_DISPLAY_NAME)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
        verify(mockWGRepo).createWG(TEST_DISPLAY_NAME)
    }

    @Test
    fun `executeJoining success`() = runTest {
        whenever(mockUserRepo.joinWGByInvitationCode(TEST_INVITATION_CODE))
            .thenReturn(Result.Success(Unit))
        whenever(mockInitialDataRepo.getInitialData())
            .thenReturn(Result.Success(Unit))

        val result = manageWGInteractor.executeJoining(TEST_INVITATION_CODE)

        assertTrue(result is Result.Success)
        verify(mockUserRepo).joinWGByInvitationCode(TEST_INVITATION_CODE)
        verify(mockInitialDataRepo).getInitialData()
    }

    @Test
    fun `executeJoining failure on joinWGByInvitationCode`() = runTest {
        val expectedError = DomainError.NetworkError // error example
        whenever(mockUserRepo.joinWGByInvitationCode(TEST_INVITATION_CODE))
            .thenReturn(Result.Error(expectedError))
        val result = manageWGInteractor.executeJoining(TEST_INVITATION_CODE)
        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
    }

    @Test
    fun `executeMemberKickOut success`() = runTest {
        whenever(mockWGRepo.removeUserFromWG(TEST_USER_ID))
            .thenReturn(Result.Success(Unit))
        val result = manageWGInteractor.executeMemberKickOut(TEST_USER_ID)
        assertTrue(result is Result.Success)
        verify(mockWGRepo).removeUserFromWG(TEST_USER_ID)
    }

    @Test
    fun `executeLeaving success`() = runTest {
        whenever(mockUserRepo.leaveWG()).thenReturn(Result.Success(Unit))
        whenever(mockDeleteLocalDataRepo.deleteAllWGRelatedData()).thenReturn(Result.Success(Unit))
        val result = manageWGInteractor.executeLeaving()
        assertTrue(result is Result.Success)
        verify(mockUserRepo).leaveWG()
        verify(mockDeleteLocalDataRepo).deleteAllWGRelatedData()
    }

    @Test
    fun `executeLeaving failure on leaveWG`() = runTest {
        val expectedError = DomainError.NetworkError // error example
        whenever(mockUserRepo.leaveWG()).thenReturn(Result.Error(expectedError))
        val result = manageWGInteractor.executeLeaving()
        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
    }
}
