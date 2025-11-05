package com.wgeplant.viewmodel

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.ui.navigation.SplashViewModel
import com.wgeplant.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.never
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SplashViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockGetInitialDataInteractor: GetInitialDataInteractor

    @Mock
    private lateinit var mockManageDeviceInteractor: ManageDeviceInteractor

    private lateinit var viewModel: SplashViewModel

    @Test
    fun `user is logged in and member of wg with successful initialization`() = runTest {
        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenReturn(flowOf(Result.Success(true)))
        `when`(mockGetInitialDataInteractor.execute()).thenReturn(Result.Success(Unit))
        `when`(mockGetInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(true, viewModel.isUserLoggedIn.value)
        assertEquals(true, viewModel.isUserMember.value)
        assertTrue(viewModel.isAppReady.value)
        assertNull(viewModel.errorState.value)

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor).execute()
        verify(mockGetInitialDataInteractor).isUserInWG()
    }

    @Test
    fun `user is logged in but no member of wg with successful initialization`() = runTest {
        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenReturn(flowOf(Result.Success(true)))
        `when`(mockGetInitialDataInteractor.execute()).thenReturn(Result.Success(Unit))
        `when`(mockGetInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(false)))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(true, viewModel.isUserLoggedIn.value)
        assertEquals(false, viewModel.isUserMember.value)
        assertTrue(viewModel.isAppReady.value)
        assertNull(viewModel.errorState.value)

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor).execute()
        verify(mockGetInitialDataInteractor).isUserInWG()
    }

    @Test
    fun `user is not logged in`() = runTest {
        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenReturn(flowOf(Result.Success(false)))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(false, viewModel.isUserLoggedIn.value)
        assertNull(viewModel.isUserMember.value)
        assertTrue(viewModel.isAppReady.value)
        assertNull(viewModel.errorState.value)

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor, never()).execute()
        verify(mockGetInitialDataInteractor, never()).isUserInWG()
    }

    @Test
    fun `error during login check`() = runTest {
        val domainError = DomainError.NetworkError
        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenReturn(flowOf(Result.Error(domainError)))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(null, viewModel.isUserLoggedIn.value)
        assertNull(viewModel.isUserMember.value)
        assertFalse(viewModel.isAppReady.value)
        assertEquals(domainError.message, viewModel.errorState.value)

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor, never()).execute()
        verify(mockGetInitialDataInteractor, never()).isUserInWG()
    }

    @Test
    fun `error during initial data load after successful login check`() = runTest {
        val domainError = DomainError.NetworkError
        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenReturn(flowOf(Result.Success(true)))
        `when`(mockGetInitialDataInteractor.execute()).thenReturn(Result.Error(domainError))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(true, viewModel.isUserLoggedIn.value)
        assertNull(viewModel.isUserMember.value)
        assertFalse(viewModel.isAppReady.value)
        assertEquals(domainError.message, viewModel.errorState.value)

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor).execute()
        verify(mockGetInitialDataInteractor, never()).isUserInWG()
    }

    @Test
    fun `error during wg check after successful login check and initial data load`() = runTest {
        val domainError = DomainError.NetworkError
        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenReturn(flowOf(Result.Success(true)))
        `when`(mockGetInitialDataInteractor.execute()).thenReturn(Result.Success(Unit))
        `when`(mockGetInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Error(domainError)))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(true, viewModel.isUserLoggedIn.value)
        assertEquals(null, viewModel.isUserMember.value)
        assertFalse(viewModel.isAppReady.value)
        assertEquals(domainError.message, viewModel.errorState.value)

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor).execute()
        verify(mockGetInitialDataInteractor).isUserInWG()
    }

    @Test
    fun `unexpected exception during app initialization`() = runTest {
        val exceptionMessage = "Netzwerkfehler"

        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenThrow(RuntimeException(exceptionMessage))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(null, viewModel.isUserLoggedIn.value)
        assertNull(viewModel.isUserMember.value)
        assertFalse(viewModel.isAppReady.value)
        assertEquals(SplashViewModel.UNEXPECTED_ERROR, viewModel.errorState.value)
        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor, never()).execute()
        verify(mockGetInitialDataInteractor, never()).isUserInWG()
    }

    @Test
    fun `startAppInitialization retries and completes successfully`() = runTest {
        val domainError = DomainError.NetworkError
        `when`(mockGetInitialDataInteractor.isUserLoggedIn()).thenReturn(flowOf(Result.Error(domainError))).thenReturn(
            flowOf(Result.Success(true))
        )
        `when`(mockGetInitialDataInteractor.execute()).thenReturn(Result.Success(Unit))
        `when`(mockGetInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))
        `when`(mockManageDeviceInteractor.getNetworkConnectionStatus()).thenReturn(flowOf(true))

        viewModel = SplashViewModel(mockGetInitialDataInteractor, mockManageDeviceInteractor)
        advanceUntilIdle()

        assertEquals(domainError.message, viewModel.errorState.value)
        assertFalse(viewModel.isAppReady.value)

        viewModel.startAppInitialization()
        advanceUntilIdle()

        assertEquals(true, viewModel.isUserLoggedIn.value)
        assertEquals(true, viewModel.isUserMember.value)
        assertTrue(viewModel.isAppReady.value)
        assertNull(viewModel.errorState.value)

        verify(mockGetInitialDataInteractor, times(2)).isUserLoggedIn()
        verify(mockGetInitialDataInteractor).execute()
        verify(mockGetInitialDataInteractor).isUserInWG()
    }
}
