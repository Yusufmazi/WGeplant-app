package com.wgeplant.model.datasource.firebase

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.wgeplant.model.datasource.AuthFirebaseDataSourceImpl
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import java.io.IOException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthDataSourceImplTest {
    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Mock
    private lateinit var mockAuthResult: AuthResult

    @Mock
    private lateinit var mockGetTokenResultTask: Task<GetTokenResult>

    @Mock
    private lateinit var mockGetTokenResult: GetTokenResult

    @Mock
    private lateinit var mockAuthCredential: AuthCredential

    @Mock
    private lateinit var mockCreateUserTask: Task<AuthResult>

    @Mock
    private lateinit var mockLoginTask: Task<AuthResult>

    @Mock
    private lateinit var mockLogoutTask: Task<Unit>

    @Mock
    private lateinit var mockUpdateProfileTask: Task<Void?>

    @Mock
    private lateinit var mockDeleteUserTask: Task<Void>

    @Mock
    private lateinit var mockSuccessVoid: Void

    private lateinit var authStateListenerCaptor: ArgumentCaptor<FirebaseAuth.AuthStateListener>

    private lateinit var dataSource: AuthFirebaseDataSourceImpl

    private val email = "test@test.com"
    private val password = "TellMeWhy58-"
    private val idToken = "token123"
    private val uid = "myUid1234"

    private val dispatcher = StandardTestDispatcher()
    private lateinit var scope: TestScope

    @Before
    fun setup() {
        mockFirebaseAuth = mock()
        mockFirebaseUser = mock()
        mockAuthResult = mock()
        mockAuthCredential = mock()
        mockGetTokenResultTask = mock()
        mockGetTokenResult = mock()

        mockCreateUserTask = mock()
        mockUpdateProfileTask = mock()
        mockLoginTask = mock()
        mockLogoutTask = mock()
        mockDeleteUserTask = mock()
        mockSuccessVoid = mock()

        authStateListenerCaptor = ArgumentCaptor.forClass(FirebaseAuth.AuthStateListener::class.java)

        dataSource = AuthFirebaseDataSourceImpl(mockFirebaseAuth)
        scope = TestScope(dispatcher)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun <TResult> mockTaskWithException(exception: Exception): Task<TResult> {
        val mockTask = mock<Task<TResult>>()
        whenever(mockTask.isComplete).thenReturn(true)
        whenever(mockTask.isSuccessful).thenReturn(false)
        whenever(mockTask.exception).thenReturn(exception)
        return mockTask
    }

    private fun <TResult> successfulTaskWith(result: TResult): Task<TResult> {
        val mockTask = mock<Task<TResult>>()
        `when`(mockTask.isComplete).thenReturn(true)
        `when`(mockTask.isSuccessful).thenReturn(true)
        `when`(mockTask.isCanceled).thenReturn(false)
        `when`(mockTask.result).thenReturn(result)
        `when`(mockTask.exception).thenReturn(null)
        return mockTask
    }

    @Test
    fun `registerNewUser success with valid user and then return Success(AuthCredential)`() = runTest {
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockCreateUserTask)

        whenever(mockCreateUserTask.isSuccessful).thenReturn(true)
        whenever(mockCreateUserTask.isComplete).thenReturn(true)
        whenever(mockCreateUserTask.exception).thenReturn(null)
        whenever(mockCreateUserTask.result).thenReturn(mockAuthResult)

        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.getIdToken(true)).thenReturn(mockGetTokenResultTask)

        whenever(mockGetTokenResultTask.isComplete).thenReturn(true)
        whenever(mockGetTokenResultTask.isSuccessful).thenReturn(true)
        whenever(mockGetTokenResultTask.exception).thenReturn(null)
        whenever(mockGetTokenResultTask.result).thenReturn(mockGetTokenResult)

        whenever(mockGetTokenResult.token).thenReturn(idToken)
        val result = dataSource.registerNewUser(email, password)

        assertTrue(result is Result.Success)
        assertEquals(idToken, result.data)

        verify(mockFirebaseAuth).createUserWithEmailAndPassword(email, password)
        verify(mockFirebaseUser).getIdToken(true)
    }

    @Test
    fun `registerNewUser handles failure when idToken is null`() = runTest {
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockCreateUserTask)

        whenever(mockCreateUserTask.isSuccessful).thenReturn(true)
        whenever(mockCreateUserTask.isComplete).thenReturn(true)
        whenever(mockCreateUserTask.exception).thenReturn(null)
        whenever(mockCreateUserTask.result).thenReturn(mockAuthResult)

        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)

        whenever(mockFirebaseUser.getIdToken(true)).thenReturn(mockGetTokenResultTask)
        whenever(mockGetTokenResultTask.isComplete).thenReturn(true)
        whenever(mockGetTokenResultTask.isSuccessful).thenReturn(true)
        whenever(mockGetTokenResultTask.exception).thenReturn(null)
        whenever(mockGetTokenResultTask.result).thenReturn(mockGetTokenResult)

        whenever(mockGetTokenResult.token).thenReturn(null)

        val result = dataSource.registerNewUser(email, password)

        assertTrue(result is Result.Error)
        val error = result.error
        assertTrue(error is DomainError.FirebaseError.UnknownFirebaseError)
    }

    @Test
    fun `registerNewUser handles failure when user is null`() = runTest {
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockCreateUserTask)

        whenever(mockCreateUserTask.isSuccessful).thenReturn(true)
        whenever(mockCreateUserTask.isComplete).thenReturn(true)
        whenever(mockCreateUserTask.exception).thenReturn(null)
        whenever(mockCreateUserTask.result).thenReturn(mockAuthResult)

        whenever(mockAuthResult.user).thenReturn(null)

        val expectedError = DomainError.FirebaseError.UserNotFound

        val result = dataSource.registerNewUser(email, password)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockFirebaseAuth).createUserWithEmailAndPassword(email, password)
        verify(mockAuthResult).user
    }

    @Test
    fun `registerNewUser handles UnknownHostException`() = runTest {
        val unknownHostException = UnknownHostException("Simulated error")

        val mockFailedAuthTask = mockTaskWithException<AuthResult>(unknownHostException)
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockFailedAuthTask)

        val result = dataSource.registerNewUser(email, password)

        assertTrue(result is Result.Error)
        assertEquals(DomainError.NetworkError, result.error)
    }

    @Test
    fun `registerNewUser handles IOException`() = runTest {
        val ioException = IOException("Simulated error")

        val mockFailedAuthTask = mockTaskWithException<AuthResult>(ioException)
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockFailedAuthTask)

        val result = dataSource.registerNewUser(email, password)

        assertTrue(result is Result.Error)
        assertEquals(DomainError.NetworkError, result.error)
    }

    @Test
    fun `registerNewUser handles generic Exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        val mockFailedAuthTask = mockTaskWithException<AuthResult>(genericException)
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockFailedAuthTask)

        val result = dataSource.registerNewUser(email, password)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
        assertEquals(genericException.message, result.error.message)
    }

    @Test
    fun `login success with valid user and  idToken then return Success(AuthCredential)`() = runTest {
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockLoginTask)
        whenever(mockLoginTask.isSuccessful).thenReturn(true)
        whenever(mockLoginTask.isComplete).thenReturn(true)
        whenever(mockLoginTask.exception).thenReturn(null)

        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.getIdToken(true)).thenReturn(mockGetTokenResultTask)
        whenever(mockGetTokenResultTask.isComplete).thenReturn(true)
        whenever(mockGetTokenResultTask.isSuccessful).thenReturn(true)
        whenever(mockGetTokenResultTask.exception).thenReturn(null)
        whenever(mockGetTokenResultTask.result).thenReturn(mockGetTokenResult)
        whenever(mockGetTokenResult.token).thenReturn(idToken)

        val result = dataSource.loginWithEmailAndPassword(email, password)

        assertTrue(result is Result.Success)
        assertEquals(idToken, result.data)

        verify(mockFirebaseAuth).signInWithEmailAndPassword(email, password)
    }

    @Test
    fun `loginWithEmailAndPassword returns error after successful login idToken is null`() = runTest {
        val mockSuccessfulSignInTask = successfulTaskWith(mockAuthResult)

        whenever(mockFirebaseAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockSuccessfulSignInTask)
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        val mockSuccessfulGetTokenTask = successfulTaskWith(mockGetTokenResult)
        whenever(mockFirebaseUser.getIdToken(true)).thenReturn(mockSuccessfulGetTokenTask)

        whenever(mockGetTokenResult.token).thenReturn(null)

        val result = dataSource.loginWithEmailAndPassword(email, password)

        val expectedError = DomainError.FirebaseError.UnknownFirebaseError("Es gab Probleme bei der Anmeldung.")
        assertTrue(result is Result.Error)
        val error = result.error
        assertEquals(expectedError, error)
    }

    @Test
    fun `login returns error after user is null`() = runTest {
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockLoginTask)

        whenever(mockLoginTask.isSuccessful).thenReturn(true)
        whenever(mockLoginTask.isComplete).thenReturn(true)
        whenever(mockLoginTask.exception).thenReturn(null)
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        val expectedError = DomainError.FirebaseError.UserNotFound

        val result = dataSource.loginWithEmailAndPassword(email, password)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `login handles UnknownHostException`() = runTest {
        val unknownHostException = UnknownHostException("Simulated error")

        val mockFailedAuthTask = mockTaskWithException<AuthResult>(unknownHostException)
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockFailedAuthTask)

        val result = dataSource.loginWithEmailAndPassword(email, password)

        assertTrue(result is Result.Error)
        assertEquals(DomainError.NetworkError, result.error)
    }

    @Test
    fun `login handles IOException`() = runTest {
        val ioException = IOException("Simulated error")

        val mockFailedAuthTask = mockTaskWithException<AuthResult>(ioException)
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockFailedAuthTask)

        val result = dataSource.loginWithEmailAndPassword(email, password)

        assertTrue(result is Result.Error)
        assertEquals(DomainError.NetworkError, result.error)
    }

    @Test
    fun `login handles generic Exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        val mockFailedAuthTask = mockTaskWithException<AuthResult>(genericException)
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockFailedAuthTask)

        val result = dataSource.loginWithEmailAndPassword(email, password)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `logout success with valid user and then return Success(Unit)`() = runTest {
        doNothing().`when`(mockFirebaseAuth).signOut()

        val result = dataSource.logout()

        assertTrue(result is Result.Success)
        assertEquals(Unit, result.data)
        verify(mockFirebaseAuth).signOut()
    }

    @Test
    fun `logout returns Network error on UnknownHostException`() = runTest {
        val simulatedException = UnknownHostException("Simulated error")
        val expectedError = DomainError.NetworkError

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).signOut()

        val result = dataSource.logout()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `logout returns Network error on IOException`() = runTest {
        val simulatedException = IOException("Simulated error")
        val expectedError = DomainError.NetworkError

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).signOut()

        val result = dataSource.logout()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `logout returns Unknown error on generic Exception`() = runTest {
        val simulatedRuntimeException = RuntimeException("Simulated network error")
        val expectedException = DomainError.Unknown(simulatedRuntimeException)

        doThrow(simulatedRuntimeException).`when`(mockFirebaseAuth).signOut()
        val result = dataSource.logout()

        assertTrue(result is Result.Error)
        assertEquals(expectedException, result.error)
    }

    @Test
    fun `deleteAccount with valid user and then return Success(Unit)`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.delete()).thenReturn(mockDeleteUserTask)

        whenever(mockDeleteUserTask.isSuccessful).thenReturn(true)
        whenever(mockDeleteUserTask.isComplete).thenReturn(true)
        whenever(mockDeleteUserTask.exception).thenReturn(null)
        whenever(mockDeleteUserTask.result).thenReturn(mockSuccessVoid)

        val result = dataSource.deleteAccount()

        assertTrue(result is Result.Success)
        assertEquals(Unit, result.data)
        verify(mockFirebaseAuth).currentUser
    }

    @Test
    fun `deleteAccount returns error after user is null`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        val expectedError = DomainError.FirebaseError.UserNotFound

        val result = dataSource.deleteAccount()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `deleteAccount returns Network error after UnknownHostException`() = runTest {
        val simulatedException = UnknownHostException("Simulated error")
        val expectedError = DomainError.NetworkError

        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseUser).delete()

        val result = dataSource.deleteAccount()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `deleteAccount returns Network error after IOException`() = runTest {
        val simulatedException = IOException("Simulated error")
        val expectedError = DomainError.NetworkError

        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseUser).delete()

        val result = dataSource.deleteAccount()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `deleteAccount returns Unknown Error after RuntimeException`() = runTest {
        val simulatedRuntimeException = RuntimeException("Simulated error")
        val expectedError = DomainError.Unknown(simulatedRuntimeException)

        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        doThrow(simulatedRuntimeException).`when`(mockFirebaseUser).delete()

        val result = dataSource.deleteAccount()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getCurrentUserId with valid user and then return Success(String)`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(uid)

        val result = dataSource.getCurrentUserId()

        assertTrue(result is Result.Success)
        assertEquals(uid, result.data)
        verify(mockFirebaseUser).uid
    }

    @Test
    fun `getCurrentUserId with user is null`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        val expectedError = DomainError.FirebaseError.UserNotFound

        val result = dataSource.getCurrentUserId()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getCurrentUserId with userId is null`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(null)

        val expectedError = DomainError.FirebaseError.UserNotFound

        val result = dataSource.getCurrentUserId()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getCurrentUserId handles UnknownHostException`() = runTest {
        val simulatedException = UnknownHostException("Simulated error")
        val expectedError = DomainError.NetworkError

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).currentUser

        val result = dataSource.getCurrentUserId()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getCurrentUserId handles IOException`() = runTest {
        val simulatedException = IOException("Simulated error")
        val expectedError = DomainError.NetworkError

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).currentUser

        val result = dataSource.getCurrentUserId()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getCurrentUserId handles generic Exception`() = runTest {
        val simulatedRuntimeException = RuntimeException("Simulated error")
        val expectedError = DomainError.Unknown(simulatedRuntimeException)

        doThrow(simulatedRuntimeException).`when`(mockFirebaseAuth).currentUser

        val result = dataSource.getCurrentUserId()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `isLoggedIn returns a Success(Unit) if the current user is not null`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.getIdToken(true)).thenReturn(mockGetTokenResultTask)
        whenever(mockGetTokenResultTask.isComplete).thenReturn(true)
        whenever(mockGetTokenResultTask.isSuccessful).thenReturn(true)
        whenever(mockGetTokenResultTask.exception).thenReturn(null)
        whenever(mockGetTokenResultTask.result).thenReturn(mockGetTokenResult)

        whenever(mockGetTokenResult.token).thenReturn(idToken)

        val result = dataSource.isLoggedIn()

        assertTrue(result is Result.Success)
        assertEquals(idToken, result.data)
        verify(mockFirebaseAuth).currentUser
        verify(mockFirebaseUser).getIdToken(true)
    }

    @Test
    fun `isLoggedIn with user is null`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        val result = dataSource.isLoggedIn()

        val expectedError = DomainError.FirebaseError.UserNotFound

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `isLoggedIn handles error with idToken is null`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.getIdToken(true)).thenReturn(mockGetTokenResultTask)
        whenever(mockGetTokenResultTask.isComplete).thenReturn(true)
        whenever(mockGetTokenResultTask.isSuccessful).thenReturn(true)
        whenever(mockGetTokenResultTask.exception).thenReturn(null)
        whenever(mockGetTokenResultTask.result).thenReturn(mockGetTokenResult)

        whenever(mockGetTokenResult.token).thenReturn(null)

        val result = dataSource.isLoggedIn()

        val expectedError = DomainError.FirebaseError
            .UnknownFirebaseError("Es gab Probleme bei der Anmeldung.")

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `isLoggedIn handles UnknownHostException`() = runTest {
        val simulatedException = UnknownHostException("Simulated error")
        val expectedError = DomainError.NetworkError

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).currentUser

        val result = dataSource.isLoggedIn()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `isLoggedIn handles IOException`() = runTest {
        val simulatedException = IOException("Simulated error")
        val expectedError = DomainError.NetworkError

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).currentUser

        val result = dataSource.isLoggedIn()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `isLoggedIn handles generic exception`() = runTest {
        val simulatedRuntimeException = RuntimeException("Simulated failure")
        val expectedError = DomainError.Unknown(simulatedRuntimeException)

        doThrow(simulatedRuntimeException).`when`(mockFirebaseAuth).currentUser

        val result = dataSource.isLoggedIn()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getAuthStateFlow should emit true when user is logged in`() = scope.runTest {
        val emissions = mutableListOf<Result<Boolean, DomainError>>()
        val job = dataSource.getAuthStateFlow()
            .onEach { emissions.add(it) }
            .launchIn(this)

        runCurrent()

        verify(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())
        val capturedListener = authStateListenerCaptor.value
        assertNotNull(capturedListener)

        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        capturedListener.onAuthStateChanged(mockFirebaseAuth)
        runCurrent()

        assertTrue(emissions.isNotEmpty())
        val result = emissions.last()
        assertTrue(result is Result.Success)

        job.cancel()
        runCurrent()
        verify(mockFirebaseAuth).removeAuthStateListener(capturedListener)
    }

    @Test
    fun `getAuthStateFlow should emit false when user is logged out`() = scope.runTest {
        val emissions = mutableListOf<Result<Boolean, DomainError>>()
        val job = dataSource.getAuthStateFlow()
            .onEach { emissions.add(it) }
            .launchIn(this)

        runCurrent()

        verify(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())
        val capturedListener = authStateListenerCaptor.value
        assertNotNull(capturedListener)

        whenever(mockFirebaseAuth.currentUser).thenReturn(null)
        capturedListener.onAuthStateChanged(mockFirebaseAuth)
        runCurrent()

        assertTrue(emissions.isNotEmpty())
        val result = emissions.last()

        assertTrue(result is Result.Success)
        assertEquals(false, result.data)

        job.cancel()
        runCurrent()
        verify(mockFirebaseAuth).removeAuthStateListener(capturedListener)
    }

    @Test
    fun `getAuthStateFlow should emit values when authState changes multiple times`() = scope.runTest {
        val emissions = mutableListOf<Result<Boolean, DomainError>>()
        val job = dataSource.getAuthStateFlow()
            .onEach { emissions.add(it) }
            .launchIn(this)

        runCurrent()

        verify(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())
        val capturedListener = authStateListenerCaptor.value
        assertNotNull(capturedListener)

        `when`(mockFirebaseAuth.currentUser).thenReturn(null)
        capturedListener.onAuthStateChanged(mockFirebaseAuth)
        runCurrent()

        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        capturedListener.onAuthStateChanged(mockFirebaseAuth)
        runCurrent()

        `when`(mockFirebaseAuth.currentUser).thenReturn(null)
        capturedListener.onAuthStateChanged(mockFirebaseAuth)
        runCurrent()

        assertEquals(3, emissions.size)

        val result1 = emissions[0]
        assertTrue(result1 is Result.Success)
        assertEquals(false, result1.data)

        val result2 = emissions[1]
        assertTrue(result2 is Result.Success)
        assertEquals(true, result2.data)

        val result3 = emissions[2]
        assertTrue(result3 is Result.Success)
        assertEquals(false, result3.data)

        job.cancel()
        runCurrent()
        verify(mockFirebaseAuth).removeAuthStateListener(capturedListener)
    }

    @Test
    fun `getAuthStateFlow when addListener throws UnknownHostException`() = scope.runTest {
        val emissions = mutableListOf<Result<Boolean, DomainError>>()
        val simulatedException = UnknownHostException("Simulated error")
        val expectedError = Result.Error(DomainError.NetworkError)

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())

        val job = dataSource.getAuthStateFlow()
            .onEach { emissions.add(it) }
            .launchIn(this)
        job.join()
        runCurrent()

        assertEquals(1, emissions.size)
        assertEquals(expectedError, emissions.first())
    }

    @Test
    fun `getAuthStateFlow when addListener throws IOException`() = scope.runTest {
        val emissions = mutableListOf<Result<Boolean, DomainError>>()
        val simulatedException = IOException("Simulated error")
        val expectedError = Result.Error(DomainError.NetworkError)

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())

        val job = dataSource.getAuthStateFlow()
            .onEach { emissions.add(it) }
            .launchIn(this)
        job.join()
        runCurrent()

        assertEquals(1, emissions.size)
        assertEquals(expectedError, emissions.first())
    }

    @Test
    fun `getAuthStateFlow when addListener throws generic Exception`() = scope.runTest {
        val emissions = mutableListOf<Result<Boolean, DomainError>>()
        val simulatedException = RuntimeException("Simulated error")
        val expectedError = Result.Error(DomainError.Unknown(simulatedException))

        doAnswer { invocation: InvocationOnMock ->
            throw simulatedException
        }.`when`(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())

        val job = dataSource.getAuthStateFlow()
            .onEach { emissions.add(it) }
            .launchIn(this)
        job.join()
        runCurrent()

        assertEquals(1, emissions.size)
        val result = emissions.first()
        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
        assertEquals(expectedError, result)
    }

    @Test
    fun `reloadCurrentUser when user exists and reload succeeds`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        val mockSuccessfulTask: Task<Void> = Tasks.forResult(null)
        whenever(mockFirebaseUser.reload()).thenReturn(mockSuccessfulTask)

        val expectedResult = Result.Success(Unit)

        val result = dataSource.reloadCurrentUser()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `reloadCurrentUser when currentUser is null returns UserNotFound`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)
        val expectedError = DomainError.FirebaseError.UserNotFound
        val result = dataSource.reloadCurrentUser()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `reloadCurrentUser when reload throws UnknownHostException`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        val simulatedException = UnknownHostException("Simulated error")
        val expectedError = DomainError.NetworkError

        val mockFailedTask: Task<Void> = Tasks.forException(simulatedException)
        whenever(mockFirebaseUser.reload()).thenReturn(mockFailedTask)

        val result = dataSource.reloadCurrentUser()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `reloadCurrentUser when reload throws IOException`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        val simulatedException = IOException("Simulated error")
        val expectedError = DomainError.NetworkError

        val mockFailedTask: Task<Void> = Tasks.forException(simulatedException)
        whenever(mockFirebaseUser.reload()).thenReturn(mockFailedTask)

        val result = dataSource.reloadCurrentUser()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `reloadCurrentUser when reload throws generic Exception`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        val simulatedException = RuntimeException("Simulated error")
        val expectedError = DomainError.Unknown(simulatedException)

        val mockFailedTask: Task<Void> = Tasks.forException(simulatedException)
        whenever(mockFirebaseUser.reload()).thenReturn(mockFailedTask)

        val result = dataSource.reloadCurrentUser()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }
}
