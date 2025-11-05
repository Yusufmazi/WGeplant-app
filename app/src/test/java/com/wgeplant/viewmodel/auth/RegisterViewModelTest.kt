package com.wgeplant.viewmodel.auth

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.userManagement.AuthInteractor
import com.wgeplant.ui.auth.RegisterViewModel
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RegisterViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockAuthInteractor: AuthInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        viewModel = RegisterViewModel(mockAuthInteractor)
    }

    // input changes tests

    @Test
    fun `onEmailChanged updates email in uiState and clears error`() = runTest {
        viewModel.onEmailChanged("invalidEmail")
        viewModel.register(mockNavController)
        assertNotNull(viewModel.uiState.value.emailError)

        viewModel.onEmailChanged("test@email.com")

        assertEquals("test@email.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `onPasswordChanged updates password in uiState and clears error`() = runTest {
        viewModel.onPasswordChanged("short")
        viewModel.register(mockNavController)
        assertNotNull(viewModel.uiState.value.passwordError)

        viewModel.onPasswordChanged("testPassword123!")

        assertEquals("testPassword123!", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `onPasswordVisibilityChanged toggles isPasswordVisible in uiState`() = runTest {
        assertFalse(viewModel.uiState.first().isPasswordVisible)
        viewModel.onPasswordVisibilityChanged()
        assertTrue(viewModel.uiState.first().isPasswordVisible)
        viewModel.onPasswordVisibilityChanged()
        assertFalse(viewModel.uiState.first().isPasswordVisible)
    }

    @Test
    fun `onDisplayNameChanged updates displayName in uiState and clears error`() = runTest {
        viewModel.onDisplayNameChanged("")
        viewModel.register(mockNavController)
        assertNotNull(viewModel.uiState.value.displayNameError)

        viewModel.onDisplayNameChanged("TestName")

        assertEquals("TestName", viewModel.uiState.value.displayName)
        assertNull(viewModel.uiState.value.displayNameError)
    }

    // input validation tests

    @Test
    fun `error for blank input fields when register is called`() = runTest {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("")
        viewModel.onDisplayNameChanged("")

        viewModel.register(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.FIELD_EMPTY, state.emailError)
        assertEquals(RegisterViewModel.FIELD_EMPTY, state.passwordError)
        assertEquals(RegisterViewModel.FIELD_EMPTY, state.displayNameError)
    }

    @Test
    fun `error for invalid email format when register is called`() = runTest {
        viewModel.onEmailChanged("invalidEmail")

        viewModel.register(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.EMAIL_INVALID, state.emailError)
    }

    @Test
    fun `error for invalid password when register is called`() = runTest {
        viewModel.onPasswordChanged("short")

        viewModel.register(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.PASSWORD_TOO_SHORT, state.value.passwordError)

        viewModel.onPasswordChanged("password123")

        viewModel.register(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.PASSWORD_WEAK, state.value.passwordError)

        viewModel.onPasswordChanged("PASSWORD!?")
        viewModel.register(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.PASSWORD_WEAK, state.value.passwordError)
    }

    @Test
    fun `error for invalid display name when register is called`() = runTest {
        viewModel.onDisplayNameChanged("ThisDisplayNameIsWayTooLongForThisField")

        viewModel.register(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.DISPLAY_NAME_TOO_LONG, state.value.displayNameError)

        viewModel.onDisplayNameChanged("Name#")

        viewModel.register(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.DISPLAY_NAME_INVALID_CHARS, state.value.displayNameError)

        viewModel.onDisplayNameChanged("123")

        viewModel.register(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(RegisterViewModel.DISPLAY_NAME_NO_LETTER, state.value.displayNameError)
    }

    // register() Tests

    @Test
    fun `register shows error and does not call interactor if validation fails`() = runTest {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("testPassword123")
        viewModel.onDisplayNameChanged("TestName")
        viewModel.register(mockNavController)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertEquals(RegisterViewModel.FIX_INPUTS, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verifyNoInteractions(mockAuthInteractor)
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `register calls interactor and navigates on successful registration`() = runTest {
        viewModel.onEmailChanged("test@email.com")
        viewModel.onPasswordChanged("testPassword123")
        viewModel.onDisplayNameChanged("TestName")

        whenever(mockAuthInteractor.executeRegistration(any(), any(), any()))
            .thenReturn(Result.Success(Unit))

        viewModel.register(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockAuthInteractor).executeRegistration("test@email.com", "testPassword123", "TestName")
        verify(mockNavController).navigate(eq(Routes.CHOOSE_WG), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `register handles interactor error and shows error message`() = runTest {
        viewModel.onEmailChanged("test@email.com")
        viewModel.onPasswordChanged("testPassword123")
        viewModel.onDisplayNameChanged("TestName")

        val domainError = DomainError.NetworkError
        whenever(mockAuthInteractor.executeRegistration(any(), any(), any()))
            .thenReturn(Result.Error(domainError))

        viewModel.register(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(domainError.message, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockAuthInteractor).executeRegistration("test@email.com", "testPassword123", "TestName")
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `register handles unexpected exception during registration`() = runTest {
        viewModel.onEmailChanged("test@email.com")
        viewModel.onPasswordChanged("testPassword123")
        viewModel.onDisplayNameChanged("TestName")

        whenever(mockAuthInteractor.executeRegistration(any(), any(), any()))
            .thenThrow(RuntimeException("Netzwerkfehler"))

        viewModel.register(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isValid)
        assertEquals(RegisterViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockAuthInteractor).executeRegistration(any(), any(), any())
        verifyNoInteractions(mockNavController)
    }

    // navigation tests

    @Test
    fun `navigateBack calls popBackStack on navController`() = runTest {
        viewModel.navigateBack(mockNavController)

        verify(mockNavController).popBackStack()
    }
}
