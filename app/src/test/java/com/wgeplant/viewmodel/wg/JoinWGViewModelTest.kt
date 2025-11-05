package com.wgeplant.viewmodel.wg

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.ui.wg.JoinWGViewModel
import com.wgeplant.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class JoinWGViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockManageWGInteractor: ManageWGInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: JoinWGViewModel

    @Before
    fun setUp() {
        viewModel = JoinWGViewModel(mockManageWGInteractor)
    }

    // input changes tests
    @Test
    fun `onInvitationCodeChanged updates invitationCode and clears error`() = runTest {
        viewModel.onInvitationCodeChanged("")
        viewModel.joinWG(mockNavController)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.invitationCodeError)

        viewModel.onInvitationCodeChanged("123456")
        assertEquals("123456", viewModel.uiState.value.invitationCode)
        assertNull(viewModel.uiState.value.invitationCodeError)
    }

    // input validation tests

    @Test
    fun `error for invalid invitation code when joinWG is called`() = runTest {
        viewModel.onInvitationCodeChanged("")

        viewModel.joinWG(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(JoinWGViewModel.FIELD_EMPTY, state.value.invitationCodeError)

        viewModel.onInvitationCodeChanged("12345")

        viewModel.joinWG(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(JoinWGViewModel.INVALID_CODE_FORMAT, state.value.invitationCodeError)

        viewModel.onInvitationCodeChanged("123abc")

        viewModel.joinWG(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(JoinWGViewModel.INVALID_CODE_FORMAT, state.value.invitationCodeError)
    }

    // joinWG tests

    @Test
    fun `joinWG shows error and does not call interactor if validation fails`() = runTest {
        viewModel.onInvitationCodeChanged("")

        viewModel.joinWG(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertEquals(JoinWGViewModel.FIX_INPUTS, viewModel.errorMessage.value)

        verifyNoInteractions(mockManageWGInteractor)
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `joinWG calls interactor and navigates on success`() = runTest {
        viewModel.onInvitationCodeChanged("123456")

        `when`(mockManageWGInteractor.executeJoining(anyString())).thenReturn(Result.Success(Unit))

        viewModel.joinWG(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockManageWGInteractor).executeJoining("123456")
        verify(mockNavController).navigate(eq(Routes.CALENDAR_GRAPH), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `joinWG handles interactor error and shows error message`() = runTest {
        viewModel.onInvitationCodeChanged("123456")

        val domainError = DomainError.NetworkError
        `when`(mockManageWGInteractor.executeJoining(anyString())).thenReturn(Result.Error(domainError))

        viewModel.joinWG(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(domainError.message, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockManageWGInteractor).executeJoining("123456")
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `joinWG handles unexpected exception during joining`() = runTest {
        viewModel.onInvitationCodeChanged("123456")

        `when`(mockManageWGInteractor.executeJoining(anyString())).thenThrow(RuntimeException("Netzwerkfehler"))

        viewModel.joinWG(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(JoinWGViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockManageWGInteractor).executeJoining("123456")
        verifyNoInteractions(mockNavController)
    }

    // navigation tests

    @Test
    fun `navigateBack calls popBackStack on navController`() = runTest {
        viewModel.navigateBack(mockNavController)

        verify(mockNavController).popBackStack()
    }
}
