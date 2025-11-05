package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.UserRequestDTO
import com.wgeplant.common.dto.response.UserResponseDTO
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteUserDataSourceImplTest {
    @Mock
    private lateinit var mockApiService: ApiService

    @Mock
    private lateinit var mockResponseHandler: ResponseHandler

    @Mock
    private lateinit var mockUserResponse: Response<UserResponseDTO>

    @Mock
    private lateinit var mockUnitResponse: Response<Unit>

    @Mock
    private lateinit var mockErrorResponse: ResponseBody

    private lateinit var dataSource: RemoteUserDataSourceImpl

    private val userId = "huhu234"
    private val invitationCode = "pleaseLeave90!"
    private val requestUser = UserRequestDTO(
        userId = userId,
        displayName = "Kaot",
        profilePicture = null
    )
    private val responseUser = UserResponseDTO(
        userId = userId,
        displayName = "Kaot",
        profilePicture = null
    )
    private val updateUser = UserRequestDTO(
        userId = userId,
        displayName = "Upsi",
        profilePicture = null
    )
    private val updatedUser = UserResponseDTO(
        userId = userId,
        displayName = "Upsi",
        profilePicture = null
    )

    @Before
    fun setUp() {
        mockApiService = mock()
        mockResponseHandler = mock()
        mockUserResponse = mock()
        mockUnitResponse = mock()
        mockErrorResponse = mock()
        dataSource = RemoteUserDataSourceImpl(mockApiService, mockResponseHandler)
    }

    @Test
    fun `createUserRemote should return a UserResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.createUserRemote(requestUser)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(true)
        whenever(mockUserResponse.code()).thenReturn(200)
        whenever(mockUserResponse.body()).thenReturn(responseUser)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Success(responseUser))

        val result = dataSource.createUserRemote(requestUser)

        assertTrue(result is Result.Success)
        assertEquals(responseUser, result.data)

        verify(mockApiService).createUserRemote(requestUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `createUserRemote should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.createUserRemote(requestUser)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(false)
        whenever(mockUserResponse.code()).thenReturn(400)
        whenever(mockUserResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.createUserRemote(requestUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).createUserRemote(requestUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `createUserRemote should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.createUserRemote(requestUser)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(true)
        whenever(mockUserResponse.code()).thenReturn(200)
        whenever(mockUserResponse.body()).thenReturn(responseUser)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.createUserRemote(requestUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).createUserRemote(requestUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `createUserRemote when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.createUserRemote(requestUser)).thenReturn(mockUserResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.createUserRemote(requestUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).createUserRemote(requestUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `createTask when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.createUserRemote(requestUser)).thenReturn(mockUserResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.createUserRemote(requestUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).createUserRemote(requestUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `createUserRemote should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.createUserRemote(requestUser)).thenThrow(genericException)

        val result = dataSource.createUserRemote(requestUser)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `createUserRemote when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.createUserRemote(requestUser)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.createUserRemote(requestUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getUserById should return a UserResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.getUserById(userId)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(true)
        whenever(mockUserResponse.code()).thenReturn(200)
        whenever(mockUserResponse.body()).thenReturn(responseUser)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Success(responseUser))

        val result = dataSource.getUserById(userId)

        assertTrue(result is Result.Success)
        assertEquals(responseUser, result.data)

        verify(mockApiService).getUserById(userId)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `getUserById should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.getUserById(userId)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(false)
        whenever(mockUserResponse.code()).thenReturn(400)
        whenever(mockUserResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.getUserById(userId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).getUserById(userId)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `getUserById should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.getUserById(userId)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(true)
        whenever(mockUserResponse.code()).thenReturn(200)
        whenever(mockUserResponse.body()).thenReturn(responseUser)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.getUserById(userId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).getUserById(userId)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `getUserById when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.getUserById(userId)).thenReturn(mockUserResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.getUserById(userId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).getUserById(userId)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `getUserById when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.getUserById(userId)).thenReturn(mockUserResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.getUserById(userId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).getUserById(userId)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `getUserById should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.getUserById(userId)).thenThrow(genericException)

        val result = dataSource.getUserById(userId)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `getUserById when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.getUserById(userId)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.getUserById(userId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `deleteUserData should return a Unit when API call is successful`() = runTest {
        whenever(mockApiService.deleteUserData()).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Success(Unit))

        val result = dataSource.deleteUserData()

        assertTrue(result is Result.Success)

        verify(mockApiService).deleteUserData()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteUserData should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.deleteUserData()).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(false)
        whenever(mockUnitResponse.code()).thenReturn(400)
        whenever(mockUnitResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.deleteUserData()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).deleteUserData()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteUserData should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.deleteUserData()).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.deleteUserData()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).deleteUserData()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteUserData when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.deleteUserData()).thenReturn(mockUnitResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.deleteUserData()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).deleteUserData()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteUserData when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.deleteUserData()).thenReturn(mockUnitResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.deleteUserData()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).deleteUserData()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteUserData should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.deleteUserData()).thenThrow(genericException)

        val result = dataSource.deleteUserData()

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `deleteUserData when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.deleteUserData()).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.deleteUserData()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `joinWG should return a Unit when API call is successful`() = runTest {
        whenever(mockApiService.joinWG(invitationCode)).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Success(Unit))

        val result = dataSource.joinWG(invitationCode)

        assertTrue(result is Result.Success)

        verify(mockApiService).joinWG(invitationCode)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `joinWG should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.joinWG(invitationCode)).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(false)
        whenever(mockUnitResponse.code()).thenReturn(400)
        whenever(mockUnitResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.joinWG(invitationCode)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).joinWG(invitationCode)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `joinWG should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.joinWG(invitationCode)).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.joinWG(invitationCode)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).joinWG(invitationCode)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `joinWG when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.joinWG(invitationCode)).thenReturn(mockUnitResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.joinWG(invitationCode)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).joinWG(invitationCode)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `joinWG when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.joinWG(invitationCode)).thenReturn(mockUnitResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.joinWG(invitationCode)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).joinWG(invitationCode)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `joinWG should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.joinWG(invitationCode)).thenThrow(genericException)

        val result = dataSource.joinWG(invitationCode)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `joinWG when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.joinWG(invitationCode)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.joinWG(invitationCode)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `leaveWG should return Unit when API call is successful`() = runTest {
        whenever(mockApiService.leaveWG()).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Success(Unit))

        val result = dataSource.leaveWG()

        assertTrue(result is Result.Success)

        verify(mockApiService).leaveWG()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `leaveWG should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.leaveWG()).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(false)
        whenever(mockUnitResponse.code()).thenReturn(400)
        whenever(mockUnitResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.leaveWG()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).leaveWG()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `leaveWG should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.leaveWG()).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.leaveWG()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).leaveWG()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `leaveWG when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.leaveWG()).thenReturn(mockUnitResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.leaveWG()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).leaveWG()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `leaveWG when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.leaveWG()).thenReturn(mockUnitResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.leaveWG()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).leaveWG()
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `leaveWG should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.leaveWG()).thenThrow(genericException)

        val result = dataSource.leaveWG()

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `leaveWG when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.leaveWG()).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.leaveWG()

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `updateUser should return a UserResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.updateUser(updateUser)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(true)
        whenever(mockUserResponse.code()).thenReturn(200)
        whenever(mockUserResponse.body()).thenReturn(updatedUser)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Success(updatedUser))

        val result = dataSource.updateUser(updateUser)

        assertTrue(result is Result.Success)
        assertEquals(updatedUser, result.data)

        verify(mockApiService).updateUser(updateUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `updateUser should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.updateUser(updateUser)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(false)
        whenever(mockUserResponse.code()).thenReturn(400)
        whenever(mockUserResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.updateUser(updateUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).updateUser(updateUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `updateUser should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.updateUser(updateUser)).thenReturn(mockUserResponse)
        whenever(mockUserResponse.isSuccessful).thenReturn(true)
        whenever(mockUserResponse.code()).thenReturn(200)
        whenever(mockUserResponse.body()).thenReturn(updatedUser)
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.updateUser(updateUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).updateUser(updateUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `updateUser when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.updateUser(updateUser)).thenReturn(mockUserResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.updateUser(updateUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).updateUser(updateUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `updateUser when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.updateUser(updateUser)).thenReturn(mockUserResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockUserResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.updateUser(updateUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).updateUser(updateUser)
        verify(mockResponseHandler).handleResponse(mockUserResponse)
    }

    @Test
    fun `updateUser should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.updateUser(requestUser)).thenThrow(genericException)

        val result = dataSource.updateUser(requestUser)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `updateUser when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.updateUser(updateUser)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.updateUser(updateUser)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }
}
