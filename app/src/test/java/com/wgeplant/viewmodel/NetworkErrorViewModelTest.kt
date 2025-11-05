package com.wgeplant.viewmodel

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.ui.navigation.NetworkErrorViewModel
import com.wgeplant.util.MainCoroutineRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class NetworkErrorViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockGetInitialDataInteractor: GetInitialDataInteractor

    private lateinit var viewModel: NetworkErrorViewModel

    @Before
    fun setUp() {
        viewModel = NetworkErrorViewModel(mockGetInitialDataInteractor)
    }

    @Test
    fun `synchronizeData succeeds when user is logged in`() = runTest {
        given(mockGetInitialDataInteractor.isUserLoggedIn()).willReturn(flowOf(Result.Success(true)))
        given(mockGetInitialDataInteractor.execute()).willReturn(Result.Success(Unit))

        viewModel.synchronizeData()
        advanceUntilIdle()

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor).execute()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `synchronizeData fails with error during data load`() = runTest {
        given(mockGetInitialDataInteractor.isUserLoggedIn()).willReturn(flowOf(Result.Success(true)))
        given(mockGetInitialDataInteractor.execute()).willReturn(Result.Error(DomainError.PersistenceError))

        viewModel.synchronizeData()
        advanceUntilIdle()

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor).execute()

        assertEquals(NetworkErrorViewModel.ERROR_CLOSE_THE_APP, viewModel.errorMessage.value)
    }

    @Test
    fun `synchronizeData handles unexpected exception`() = runTest {
        given(mockGetInitialDataInteractor.isUserLoggedIn()).willAnswer { throw RuntimeException() }

        viewModel.synchronizeData()
        advanceUntilIdle()

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor, never()).execute()

        assertEquals(NetworkErrorViewModel.ERROR_CLOSE_THE_APP, viewModel.errorMessage.value)
    }

    @Test
    fun `synchronizeData succeeds when user is not logged in`() = runTest {
        given(mockGetInitialDataInteractor.isUserLoggedIn()).willReturn(flowOf(Result.Success(false)))

        viewModel.synchronizeData()
        advanceUntilIdle()

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor, never()).execute()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `synchronizeData fails with error during login check`() = runTest {
        given(mockGetInitialDataInteractor.isUserLoggedIn()).willReturn(flowOf(Result.Error(DomainError.NotFoundError)))

        viewModel.synchronizeData()
        advanceUntilIdle()

        verify(mockGetInitialDataInteractor).isUserLoggedIn()
        verify(mockGetInitialDataInteractor, never()).execute()

        assertEquals(NetworkErrorViewModel.ERROR_CLOSE_THE_APP, viewModel.errorMessage.value)
    }
}
