package com.wgeplant.model.interactor.initialDataRequest

import com.wgeplant.model.datasource.remote.api.HeaderConfiguration
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.WG
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.repository.AuthRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class getInitialDataInteractorImplTest {

    companion object {
        private const val TEST_USER_ID = "testUserId"
        private const val TEST_DEVICE_ID = "testDeviceId"
        private const val TEST_WG_ID = "testWGId"
        private const val TEST_WG_NAME = "Test WG"
        private const val TEST_INVITATION_CODE = "testInvitationCode"
        val mockRuntimeException = RuntimeException("Device ID fetch failed")
        val mockStateException = IllegalStateException("Header config failed")
    }

    private lateinit var mockInitialDataRepo: InitialDataRepo
    private lateinit var mockAuthRepo: AuthRepo
    private lateinit var mockWGRepo: WGRepo
    private lateinit var mockManageDeviceInteractor: ManageDeviceInteractor
    private lateinit var mockHeaderConfiguration: HeaderConfiguration
    private lateinit var mockUserRepo: UserRepo

    private lateinit var getInitialDataInteractor: GetInitialDataInteractorImpl

    @Before
    fun setUp() {
        mockInitialDataRepo = mock()
        mockAuthRepo = mock()
        mockWGRepo = mock()
        mockManageDeviceInteractor = mock()
        mockHeaderConfiguration = mock()
        mockUserRepo = mock()

        getInitialDataInteractor = GetInitialDataInteractorImpl(
            initialDataRepo = mockInitialDataRepo,
            authRepo = mockAuthRepo,
            wgRepo = mockWGRepo,
            manageDeviceInteractor = mockManageDeviceInteractor,
            headerConfiguration = mockHeaderConfiguration,
            userRepo = mockUserRepo
        )
    }

    @Test
    fun `execute success`() = runTest {
        whenever(mockInitialDataRepo.getInitialData())
            .thenReturn(Result.Success(Unit))
        whenever(mockAuthRepo.getLocalUserId())
            .thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.setLocalUserId(TEST_USER_ID))
            .thenReturn(Result.Success(Unit))
        val result = getInitialDataInteractor.execute()
        assertTrue(result is Result.Success)
        verify(mockInitialDataRepo).getInitialData()
        verify(mockAuthRepo).getLocalUserId()
        verify(mockUserRepo).setLocalUserId(TEST_USER_ID)
    }

    @Test
    fun `execute failure on getInitialData`() = runTest {
        whenever(mockInitialDataRepo.getInitialData())
            .thenReturn(Result.Error(DomainError.Unknown(Exception())))
        val result = getInitialDataInteractor.execute()
        assertTrue(result is Result.Error)
        verify(mockInitialDataRepo).getInitialData()
    }

    @Test
    fun `execute failure on getLocalUserId`() = runTest {
        whenever(mockInitialDataRepo.getInitialData())
            .thenReturn(Result.Success(Unit))
        whenever(mockAuthRepo.getLocalUserId())
            .thenReturn(Result.Error(DomainError.Unknown(Exception())))
        val result = getInitialDataInteractor.execute()
        assertTrue(result is Result.Error)
        verify(mockInitialDataRepo).getInitialData()
        verify(mockAuthRepo).getLocalUserId()
    }

    @Test
    fun `execute failure on setLocalUserId`() = runTest {
        whenever(mockInitialDataRepo.getInitialData())
            .thenReturn(Result.Success(Unit))
        whenever(mockAuthRepo.getLocalUserId())
            .thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockUserRepo.setLocalUserId(TEST_USER_ID))
            .thenReturn(Result.Error(DomainError.Unknown(Exception())))
        val result = getInitialDataInteractor.execute()
        assertTrue(result is Result.Error)
        verify(mockInitialDataRepo).getInitialData()
        verify(mockAuthRepo).getLocalUserId()
        verify(mockUserRepo).setLocalUserId(TEST_USER_ID)
    }

    @Test
    fun `isUserLoggedIn success with logged in user`() = runTest {
        whenever(mockAuthRepo.getAuthStateFlow())
            .thenReturn(flowOf(Result.Success(true)))
        whenever(mockAuthRepo.isLoggedIn())
            .thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockManageDeviceInteractor.getSavedDeviceId())
            .thenReturn(TEST_DEVICE_ID)
        val result = getInitialDataInteractor.isUserLoggedIn().first()
        assertTrue(result is Result.Success)
        verify(mockAuthRepo).isLoggedIn()
        verify(mockManageDeviceInteractor).getSavedDeviceId()
        verify(mockHeaderConfiguration).setAuthData(TEST_USER_ID, TEST_DEVICE_ID)
    }

    @Test
    fun `isUserLoggedIn success with not logged in user`() = runTest {
        whenever(mockAuthRepo.getAuthStateFlow())
            .thenReturn(flowOf(Result.Success(false)))
        val result = getInitialDataInteractor.isUserLoggedIn().first()
        assertTrue(result is Result.Success)
    }

    @Test
    fun `isUserLoggedIn emits Unknown error when getSavedDeviceId throws exception`() = runTest {
        whenever(mockAuthRepo.getAuthStateFlow()).thenReturn(flowOf(Result.Success(true)))
        whenever(mockAuthRepo.isLoggedIn()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockManageDeviceInteractor.getSavedDeviceId()).doThrow(mockRuntimeException)
        val result = getInitialDataInteractor.isUserLoggedIn().first()
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is DomainError.Unknown)
    }

    @Test
    fun `isUserLoggedIn emits Unknown error when setAuthData throws exception`() = runTest {
        whenever(mockAuthRepo.getAuthStateFlow()).thenReturn(flowOf(Result.Success(true)))
        whenever(mockAuthRepo.isLoggedIn()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockManageDeviceInteractor.getSavedDeviceId()).thenReturn(TEST_DEVICE_ID)
        whenever(mockHeaderConfiguration.setAuthData(TEST_USER_ID, TEST_DEVICE_ID)).doThrow(mockStateException)
        val result = getInitialDataInteractor.isUserLoggedIn().first()
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is DomainError.Unknown)
    }

    @Test
    fun `isUserLoggedIn failure on getAuthStateFlow`() = runTest {
        whenever(mockAuthRepo.getAuthStateFlow())
            .thenReturn(flowOf(Result.Error(DomainError.Unknown(Exception()))))
        val result = getInitialDataInteractor.isUserLoggedIn().first()
        assertTrue(result is Result.Error)
    }

    @Test
    fun `isUserLoggedIn failure on isLoggedIn`() = runTest {
        whenever(mockAuthRepo.getAuthStateFlow())
            .thenReturn(flowOf(Result.Success(true)))
        whenever(mockAuthRepo.isLoggedIn())
            .thenReturn(Result.Error(DomainError.Unknown(Exception())))
        val result = getInitialDataInteractor.isUserLoggedIn().first()
        assertTrue(result is Result.Error)
    }

    @Test
    fun `isUserInWG success`() = runTest {
        val testWG = WG(
            wgId = TEST_WG_ID,
            displayName = TEST_WG_NAME,
            invitationCode = TEST_INVITATION_CODE,
            profilePicture = null
        )
        whenever(mockWGRepo.getWG())
            .thenReturn(flowOf(Result.Success(testWG)))
        val result = getInitialDataInteractor.isUserInWG().first()
        assertTrue(result is Result.Success)
    }

    @Test
    fun `isUserInWG failure on getWG`() = runTest {
        whenever(mockWGRepo.getWG())
            .thenReturn(flowOf(Result.Error(DomainError.Unknown(Exception()))))
        val result = getInitialDataInteractor.isUserInWG().first()
        assertTrue(result is Result.Error)
    }

    @Test
    fun `isUserInWG failure on getWG persistence error`() = runTest {
        whenever(mockWGRepo.getWG())
            .thenReturn(flowOf(Result.Error(DomainError.PersistenceError)))
        val result = getInitialDataInteractor.isUserInWG().first()
        assertTrue(result is Result.Success)
    }
}
