package com.wgeplant.ui.auth

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.model.interactor.userManagement.AuthInteractor
import com.wgeplant.ui.navigation.Routes
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
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock lateinit var mockAuthInteractor: AuthInteractor

    @Mock lateinit var mockInitialDataInteractor: GetInitialDataInteractor

    @Mock lateinit var mockNavController: NavController

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(mockAuthInteractor, mockInitialDataInteractor)
    }

    // --- Your original tests ---

    @Test
    fun `login with valid inputs sets no validation errors`() = runTest {
        val email = "test@example.com"
        val password = "password"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))

        viewModel.login(mockNavController)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `isLoading is false after login process completes`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))

        viewModel.login(mockNavController)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `new login attempt clears previous error message`() = runTest {
        val email = "test@example.com"
        val password = "password"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        whenever(mockAuthInteractor.executeLogin(email, password)).thenReturn(Result.Error(DomainError.NetworkError))

        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)

        whenever(mockAuthInteractor.executeLogin(email, password)).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))
        viewModel.login(mockNavController)
        advanceUntilIdle()

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `onEmailChanged updates email and clears error`() {
        viewModel.onEmailChanged("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `onPasswordChanged updates password and clears error`() {
        viewModel.onPasswordChanged("password123")
        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `onPasswordVisibilityChanged toggles visibility flag`() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onPasswordVisibilityChanged()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun `error for blank input fields when login is called`() = runTest {
        viewModel.login(mockNavController)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("Fülle dieses Feld aus.", state.emailError)
        assertEquals("Fülle dieses Feld aus.", state.passwordError)
    }

    @Test
    fun `error for invalid email format when login is called`() = runTest {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("somePassword")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertEquals("Ungültiges E-Mail-Format.", viewModel.uiState.value.emailError)
    }

    @Test
    fun `login with invalid fields shows generic error message`() = runTest {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("somePassword")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertEquals("Ändere bitte deine Eingaben.", viewModel.errorMessage.value)
    }

    @Test
    fun `successful login for user in WG navigates to calendar`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        whenever(mockAuthInteractor.executeLogin(email, password)).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))

        viewModel.login(mockNavController)
        advanceUntilIdle()

        verify(mockNavController).navigate(eq(Routes.CALENDAR_GRAPH), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `successful login for user not in WG navigates to choose WG`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        whenever(mockAuthInteractor.executeLogin(email, password)).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(false)))

        viewModel.login(mockNavController)
        advanceUntilIdle()

        verify(mockNavController).navigate(eq(Routes.CHOOSE_WG), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `successful login but isUserInWG check fails navigates to choose WG`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        whenever(mockAuthInteractor.executeLogin(email, password)).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Error(DomainError.NetworkError)))

        viewModel.login(mockNavController)
        advanceUntilIdle()

        verify(mockNavController).navigate(eq(Routes.CHOOSE_WG), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `login failure shows error message`() = runTest {
        val email = "test@example.com"
        val password = "wrong-password"
        val error = DomainError.NetworkError
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        whenever(mockAuthInteractor.executeLogin(email, password)).thenReturn(Result.Error(error))

        viewModel.login(mockNavController)
        advanceUntilIdle()

        assertEquals(error.message, viewModel.errorMessage.value)
    }

    // --- Added tests (missing scenarios) ---

    @Test
    fun `invalid form prevents calling executeLogin`() = runTest {
        viewModel.onEmailChanged("bad-email")
        viewModel.onPasswordChanged("")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        verify(mockAuthInteractor, never()).executeLogin(any(), any())
        assertFalse(viewModel.uiState.value.isValid)
        assertEquals("Ändere bitte deine Eingaben.", viewModel.errorMessage.value)
    }

    @Test
    fun `uiState isInWG updated to false`() = runTest {
        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(false)))
        viewModel.onEmailChanged("a@b.com")
        viewModel.onPasswordChanged("p")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isInWG)
    }

    @Test
    fun `uiState isInWG updated to true`() = runTest {
        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))
        viewModel.onEmailChanged("a@b.com")
        viewModel.onPasswordChanged("p")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isInWG)
    }

    @Test
    fun `loading resets to false on error`() = runTest {
        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Error(DomainError.NetworkError))
        viewModel.onEmailChanged("a@b.com")
        viewModel.onPasswordChanged("p")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertEquals(DomainError.NetworkError.message, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `invalid then fix clears errors and succeeds`() = runTest {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.emailError)
        assertNotNull(viewModel.uiState.value.passwordError)
        assertEquals("Ändere bitte deine Eingaben.", viewModel.errorMessage.value)

        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))
        viewModel.onEmailChanged("ok+tag@sub.example.co")
        viewModel.onPasswordChanged("pass")
        viewModel.login(mockNavController)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
        assertNull(viewModel.errorMessage.value)
        assertTrue(viewModel.uiState.value.isValid)
        verify(mockNavController).navigate(eq(Routes.CALENDAR_GRAPH), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `email with plus and subdomain passes validation`() = runTest {
        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))
        viewModel.onEmailChanged("john.doe+tag@sub.example.co")
        viewModel.onPasswordChanged("p")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `navigation lambda provided on success`() = runTest {
        whenever(mockAuthInteractor.executeLogin(any(), any())).thenReturn(Result.Success(Unit))
        whenever(mockInitialDataInteractor.isUserInWG()).thenReturn(flowOf(Result.Success(true)))
        val navBlockCaptor = argumentCaptor<NavOptionsBuilder.() -> Unit>()
        viewModel.onEmailChanged("a@b.com")
        viewModel.onPasswordChanged("p")
        viewModel.login(mockNavController)
        advanceUntilIdle()
        verify(mockNavController).navigate(eq(Routes.CALENDAR_GRAPH), navBlockCaptor.capture())
        assertNotNull(navBlockCaptor.firstValue)
    }
}
