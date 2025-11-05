package com.wgeplant.model.interactor.userManagement

import com.wgeplant.model.datasource.remote.api.HeaderConfiguration
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractorImpl
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractorImpl
import com.wgeplant.model.interactor.remoteUpdateManagement.RemoteUpdateInteractor
import com.wgeplant.model.repository.AuthRepo
import com.wgeplant.model.repository.DeleteLocalDataRepo
import com.wgeplant.model.repository.DeviceRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.UserRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthInteractorImplTest {
    companion object {
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "password123"
        private const val TEST_DISPLAY_NAME = "Test User"
        private const val TEST_USER_ID = "testUserId"
        private const val TEST_DEVICE_ID = "testDeviceID"
        private const val TEST_FIREBASE_ID_TOKEN = "testFirebaseIdToken"
        private const val TEST_FCM_DEVICE_TOKEN = "testFcmDeviceToken"
    }

    // mocks for dependencies
    private lateinit var mockAuthRepo: AuthRepo
    private lateinit var mockUserRepo: UserRepo
    private lateinit var mockDeleteLocalDataRepo: DeleteLocalDataRepo
    private lateinit var mockInitialDataRepo: InitialDataRepo
    private lateinit var mockDeviceRepo: DeviceRepo
    private lateinit var mockHeaderConfiguration: HeaderConfiguration
    private lateinit var mockRemoteUpdateInteractor: RemoteUpdateInteractor
    private lateinit var mockManageDeviceInteractor: ManageDeviceInteractorImpl
    private lateinit var mockGetInitialDataInteractorImpl: GetInitialDataInteractorImpl

    // class that gets tested
    private lateinit var authInteractor: AuthInteractorImpl

    @Before
    fun setUp() {
        mockAuthRepo = mock()
        mockUserRepo = mock()
        mockDeleteLocalDataRepo = mock()
        mockInitialDataRepo = mock()
        mockDeviceRepo = mock()
        mockHeaderConfiguration = mock()
        mockRemoteUpdateInteractor = mock()
        mockManageDeviceInteractor = mock()

        // forgot to initialize
        mockGetInitialDataInteractorImpl = mock()

        authInteractor = AuthInteractorImpl(
            authRepo = mockAuthRepo,
            userRepo = mockUserRepo,
            deleteLocalDataRepo = mockDeleteLocalDataRepo,
            initialDataRepo = mockInitialDataRepo,
            deviceRepo = mockDeviceRepo,
            headerConfiguration = mockHeaderConfiguration,
            remoteUpdateInteractor = mockRemoteUpdateInteractor,
            manageDeviceInteractor = mockManageDeviceInteractor,
            getInitialDataInteractorImpl = mockGetInitialDataInteractorImpl
        )
    }

    @Test
    fun `executeRegistration success`() = runTest {
        whenever(mockAuthRepo.register(anyString(), anyString()))
            .thenReturn(Result.Success(TEST_FIREBASE_ID_TOKEN))
        whenever(mockAuthRepo.getLocalUserId())
            .thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockManageDeviceInteractor.getGeneratedDeviceId())
            .thenReturn(TEST_DEVICE_ID)
        whenever(mockManageDeviceInteractor.saveDeviceIdInLocalDevice(TEST_DEVICE_ID))
            .thenReturn(Unit)
        whenever(mockManageDeviceInteractor.saveAuthDataEMailInLocalDevice(TEST_EMAIL))
            .thenReturn(Unit)
        whenever(mockManageDeviceInteractor.saveAuthDataPasswordInLocalDevice(TEST_PASSWORD))
            .thenReturn(Unit)
        whenever(mockRemoteUpdateInteractor.getDeviceTokenFromFCM())
            .thenReturn(Result.Success(TEST_FCM_DEVICE_TOKEN))
        whenever(mockDeviceRepo.addDeviceToken(TEST_FCM_DEVICE_TOKEN))
            .thenReturn(Result.Success(Unit))
        whenever(mockUserRepo.createUser(TEST_USER_ID, TEST_DISPLAY_NAME))
            .thenReturn(Result.Success(Unit))

        // execution of the method that should get tested
        val result = authInteractor.executeRegistration(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME)

        // check the result
        assertTrue(result is Result.Success)
        verify(mockAuthRepo).register(TEST_EMAIL, TEST_PASSWORD)
        verify(mockAuthRepo).getLocalUserId()
        verify(mockManageDeviceInteractor).getGeneratedDeviceId()
        verify(mockManageDeviceInteractor).saveDeviceIdInLocalDevice(TEST_DEVICE_ID)
        verify(mockManageDeviceInteractor).saveAuthDataEMailInLocalDevice(TEST_EMAIL)
        verify(mockManageDeviceInteractor).saveAuthDataPasswordInLocalDevice(TEST_PASSWORD)
        verify(mockUserRepo).setLocalUserId(TEST_USER_ID)
        verify(mockHeaderConfiguration).setAuthData(TEST_FIREBASE_ID_TOKEN, TEST_DEVICE_ID)
        verify(mockRemoteUpdateInteractor).getDeviceTokenFromFCM()
        verify(mockUserRepo).createUser(TEST_USER_ID, TEST_DISPLAY_NAME)
    }

    @Test
    fun `executeRegistration failure on authRepo_register`() = runTest {
        val expectedError = DomainError.NetworkError // error example
        whenever(mockAuthRepo.register(TEST_EMAIL, TEST_PASSWORD))
            .thenReturn(Result.Error(expectedError))

        val result = authInteractor.executeRegistration(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
    }

    @Test
    fun `executeRegistration failure on authRepo_getLocalUserId`() = runTest {
        val expectedError = DomainError.NotFoundError // error example

        whenever(mockAuthRepo.register(TEST_EMAIL, TEST_PASSWORD))
            .thenReturn(Result.Success(TEST_FIREBASE_ID_TOKEN))
        whenever(mockAuthRepo.getLocalUserId())
            .thenReturn(Result.Error(expectedError))

        val result = authInteractor.executeRegistration(TEST_EMAIL, TEST_PASSWORD, TEST_DISPLAY_NAME)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
    }

    @Test
    fun `executeLogin success`() = runTest {
        whenever(mockAuthRepo.login(TEST_EMAIL, TEST_PASSWORD))
            .thenReturn(Result.Success(TEST_FIREBASE_ID_TOKEN))
        whenever(mockManageDeviceInteractor.getGeneratedDeviceId())
            .thenReturn(TEST_DEVICE_ID)
        whenever(mockManageDeviceInteractor.saveDeviceIdInLocalDevice(TEST_DEVICE_ID))
            .thenReturn(Unit)
        whenever(mockManageDeviceInteractor.saveAuthDataEMailInLocalDevice(TEST_EMAIL))
            .thenReturn(Unit)
        whenever(mockManageDeviceInteractor.saveAuthDataPasswordInLocalDevice(TEST_PASSWORD))
            .thenReturn(Unit)
        whenever(mockAuthRepo.getLocalUserId())
            .thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockRemoteUpdateInteractor.getDeviceTokenFromFCM())
            .thenReturn(Result.Success(TEST_FCM_DEVICE_TOKEN))
        whenever(mockDeviceRepo.addDeviceToken(TEST_FCM_DEVICE_TOKEN))
            .thenReturn(Result.Success(Unit))
        whenever(mockInitialDataRepo.getInitialData())
            .thenReturn(Result.Success(Unit))

        val result = authInteractor.executeLogin(TEST_EMAIL, TEST_PASSWORD)

        assertTrue(result is Result.Success)
        verify(mockAuthRepo).login(TEST_EMAIL, TEST_PASSWORD)
        verify(mockManageDeviceInteractor).getGeneratedDeviceId()
        verify(mockManageDeviceInteractor).saveDeviceIdInLocalDevice(TEST_DEVICE_ID)
        verify(mockHeaderConfiguration).setAuthData(TEST_FIREBASE_ID_TOKEN, TEST_DEVICE_ID)
        verify(mockManageDeviceInteractor).saveAuthDataEMailInLocalDevice(TEST_EMAIL)
        verify(mockManageDeviceInteractor).saveAuthDataPasswordInLocalDevice(TEST_PASSWORD)
        verify(mockAuthRepo).getLocalUserId()
        verify(mockUserRepo).setLocalUserId(TEST_USER_ID)
        verify(mockRemoteUpdateInteractor).getDeviceTokenFromFCM()
        verify(mockDeviceRepo).addDeviceToken(TEST_FCM_DEVICE_TOKEN)
        verify(mockInitialDataRepo).getInitialData()
    }

    @Test
    fun `executeLogin failure on remoteUpdateInteractor_getDeviceTokenFromFCM`() = runTest {
        val expectedError = DomainError.FirebaseError.FcmTokenFetchFailed // error example

        whenever(mockAuthRepo.login(TEST_EMAIL, TEST_PASSWORD))
            .thenReturn(Result.Success(TEST_FIREBASE_ID_TOKEN))
        whenever(mockManageDeviceInteractor.getGeneratedDeviceId())
            .thenReturn(TEST_DEVICE_ID)
        whenever(mockManageDeviceInteractor.saveDeviceIdInLocalDevice(TEST_DEVICE_ID))
            .thenReturn(Unit)
        whenever(mockManageDeviceInteractor.saveAuthDataEMailInLocalDevice(TEST_EMAIL))
            .thenReturn(Unit)
        whenever(mockManageDeviceInteractor.saveAuthDataPasswordInLocalDevice(TEST_PASSWORD))
            .thenReturn(Unit)
        whenever(mockAuthRepo.getLocalUserId())
            .thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockRemoteUpdateInteractor.getDeviceTokenFromFCM())
            .thenReturn(Result.Error(expectedError))

        val result = authInteractor.executeLogin(TEST_EMAIL, TEST_PASSWORD)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
    }

    @Test
    fun `executeLogout success`() = runTest {
        whenever(mockDeviceRepo.deleteDeviceToken()).thenReturn(Result.Success(Unit))
        whenever(mockManageDeviceInteractor.clearDeviceIdFromLocalDevice()).thenReturn(Unit)
        whenever(mockManageDeviceInteractor.getAuthDataEMailInLocalDevice()).thenReturn(TEST_EMAIL)
        whenever(mockManageDeviceInteractor.getAuthDataPasswordInLocalDevice()).thenReturn(TEST_PASSWORD)
        whenever(mockAuthRepo.logout()).thenReturn(Result.Success(Unit))
        whenever(mockManageDeviceInteractor.clearAuthDataEMailFromLocalDevice()).thenReturn(Unit)
        whenever(mockManageDeviceInteractor.clearAuthDataPasswordFromLocalDevice()).thenReturn(Unit)
        whenever(mockDeleteLocalDataRepo.deleteAllLocalData()).thenReturn(Result.Success(Unit))

        val result = authInteractor.executeLogout()

        assertTrue(result is Result.Success)
        verify(mockDeviceRepo).deleteDeviceToken()
        verify(mockManageDeviceInteractor).clearDeviceIdFromLocalDevice()
        verify(mockManageDeviceInteractor).getAuthDataEMailInLocalDevice()
        verify(mockManageDeviceInteractor).getAuthDataPasswordInLocalDevice()
        verify(mockHeaderConfiguration).clearAuthData()
        verify(mockAuthRepo).logout()
        verify(mockManageDeviceInteractor).clearAuthDataEMailFromLocalDevice()
        verify(mockManageDeviceInteractor).clearAuthDataPasswordFromLocalDevice()
        verify(mockDeleteLocalDataRepo).deleteAllLocalData()
    }

    @Test
    fun `executeLogout failure on authRepo_logout`() = runTest {
        val expectedError = DomainError.NetworkError // error example

        whenever(mockDeviceRepo.deleteDeviceToken()).thenReturn(Result.Success(Unit))
        whenever(mockManageDeviceInteractor.clearDeviceIdFromLocalDevice()).thenReturn(Unit)
        whenever(mockManageDeviceInteractor.getAuthDataEMailInLocalDevice()).thenReturn(TEST_EMAIL)
        whenever(mockManageDeviceInteractor.getAuthDataPasswordInLocalDevice()).thenReturn(TEST_PASSWORD)
        whenever(mockAuthRepo.logout()).thenReturn(Result.Error(expectedError))

        val result = authInteractor.executeLogout()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
    }

    @Test
    fun `executeLogout continues and succeeds after authRepo_logout failed previously`() = runTest {
        val expectedError = DomainError.NetworkError // error example
        val notFoundError = DomainError.NotFoundError

        whenever(mockDeviceRepo.deleteDeviceToken()).thenReturn(Result.Success(Unit))
        whenever(mockManageDeviceInteractor.clearDeviceIdFromLocalDevice()).thenReturn(Unit)
        whenever(mockManageDeviceInteractor.getAuthDataEMailInLocalDevice()).thenReturn(TEST_EMAIL)
        whenever(mockManageDeviceInteractor.getAuthDataPasswordInLocalDevice()).thenReturn(TEST_PASSWORD)
        whenever(mockAuthRepo.logout()).thenReturn(Result.Error(expectedError))

        var result = authInteractor.executeLogout()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
        verify(mockDeviceRepo).deleteDeviceToken()
        verify(mockManageDeviceInteractor).clearDeviceIdFromLocalDevice()
        verify(mockManageDeviceInteractor).getAuthDataEMailInLocalDevice()
        verify(mockManageDeviceInteractor).getAuthDataPasswordInLocalDevice()
        verify(mockHeaderConfiguration).clearAuthData()
        verify(mockAuthRepo).logout()
        Mockito.reset(mockDeviceRepo)
        Mockito.reset(mockManageDeviceInteractor)
        Mockito.reset(mockHeaderConfiguration)
        Mockito.reset(mockAuthRepo)

        whenever(mockDeviceRepo.deleteDeviceToken()).thenReturn(Result.Error(notFoundError))
        whenever(mockManageDeviceInteractor.clearDeviceIdFromLocalDevice()).thenReturn(Unit)
        whenever(mockManageDeviceInteractor.getAuthDataEMailInLocalDevice()).thenReturn(TEST_EMAIL)
        whenever(mockManageDeviceInteractor.getAuthDataPasswordInLocalDevice()).thenReturn(TEST_PASSWORD)
        whenever(mockAuthRepo.logout()).thenReturn(Result.Success(Unit))
        whenever(mockManageDeviceInteractor.clearAuthDataEMailFromLocalDevice()).thenReturn(Unit)
        whenever(mockManageDeviceInteractor.clearAuthDataPasswordFromLocalDevice()).thenReturn(Unit)
        whenever(mockDeleteLocalDataRepo.deleteAllLocalData()).thenReturn(Result.Success(Unit))

        result = authInteractor.executeLogout()

        assertTrue(result is Result.Success)
        verify(mockAuthRepo).logout()
        verify(mockDeleteLocalDataRepo).deleteAllLocalData()
    }
}
