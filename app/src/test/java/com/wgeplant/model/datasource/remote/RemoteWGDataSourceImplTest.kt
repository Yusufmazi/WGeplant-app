package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.WGRequestDTO
import com.wgeplant.common.dto.response.WGResponseDTO
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

class RemoteWGDataSourceImplTest {
    @Mock
    private lateinit var mockApiService: ApiService

    @Mock
    private lateinit var mockResponseHandler: ResponseHandler

    @Mock
    private lateinit var mockWGResponse: Response<WGResponseDTO>

    @Mock
    private lateinit var mockErrorResponse: ResponseBody

    private lateinit var dataSource: RemoteWGDataSourceImpl

    private val wgId = "1239"
    private val requestWG = WGRequestDTO(
        wgId = null,
        displayName = "WG1",
        invitationCode = null,
        profilePicture = null
    )
    private val responseWG = WGResponseDTO(
        wgId = wgId,
        displayName = "WG1",
        invitationCode = "helloBye",
        profilePicture = null
    )
    private val requestUpdateWG = WGRequestDTO(
        wgId = wgId,
        displayName = "WG5",
        invitationCode = "helloBye",
        profilePicture = null
    )
    private val responseUpdateWG = WGResponseDTO(
        wgId = wgId,
        displayName = "WG5",
        invitationCode = "helloBye",
        profilePicture = null
    )

    @Before
    fun setUp() {
        mockApiService = mock()
        mockResponseHandler = mock()
        mockWGResponse = mock()
        mockErrorResponse = mock()
        dataSource = RemoteWGDataSourceImpl(mockApiService, mockResponseHandler)
    }

    @Test
    fun `createWGRemote should return WGResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.createWGRemote(requestWG)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(true)
        whenever(mockWGResponse.code()).thenReturn(200)
        whenever(mockWGResponse.body()).thenReturn(responseWG)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Success(responseWG))

        val result = dataSource.createWGRemote(requestWG)

        assertTrue(result is Result.Success)
        assertEquals(responseWG, result.data)

        verify(mockApiService).createWGRemote(requestWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `createWGRemote should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.createWGRemote(requestWG)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(false)
        whenever(mockWGResponse.code()).thenReturn(400)
        whenever(mockWGResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.createWGRemote(requestWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).createWGRemote(requestWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `createWGRemote should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.createWGRemote(requestWG)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(true)
        whenever(mockWGResponse.code()).thenReturn(200)
        whenever(mockWGResponse.body()).thenReturn(responseWG)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.createWGRemote(requestWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).createWGRemote(requestWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `createWGRemote when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.createWGRemote(requestWG)).thenReturn(mockWGResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.createWGRemote(requestWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).createWGRemote(requestWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `createWGRemote when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.createWGRemote(requestWG)).thenReturn(mockWGResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.createWGRemote(requestWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).createWGRemote(requestWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `createWGRemote should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.createWGRemote(requestWG)).thenThrow(genericException)

        val result = dataSource.createWGRemote(requestWG)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `createWGRemote when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.createWGRemote(requestWG)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.createWGRemote(requestWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getWGById should return WGResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.getWGById(wgId)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(true)
        whenever(mockWGResponse.code()).thenReturn(200)
        whenever(mockWGResponse.body()).thenReturn(responseWG)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Success(responseWG))

        val result = dataSource.getWGById(wgId)

        assertTrue(result is Result.Success)
        assertEquals(responseWG, result.data)

        verify(mockApiService).getWGById(wgId)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `getWGById should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.getWGById(wgId)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(false)
        whenever(mockWGResponse.code()).thenReturn(400)
        whenever(mockWGResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.getWGById(wgId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).getWGById(wgId)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `getWGById should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.getWGById(wgId)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(true)
        whenever(mockWGResponse.code()).thenReturn(200)
        whenever(mockWGResponse.body()).thenReturn(responseWG)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.getWGById(wgId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).getWGById(wgId)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `getWGById when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.getWGById(wgId)).thenReturn(mockWGResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.getWGById(wgId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).getWGById(wgId)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `getWGById when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.getWGById(wgId)).thenReturn(mockWGResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.getWGById(wgId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).getWGById(wgId)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `getWGById should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.getWGById(wgId)).thenThrow(genericException)

        val result = dataSource.getWGById(wgId)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `getWGById when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.getWGById(wgId)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.getWGById(wgId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `updateWG should return a WGResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.updateWG(requestUpdateWG)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(true)
        whenever(mockWGResponse.code()).thenReturn(200)
        whenever(mockWGResponse.body()).thenReturn(responseWG)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Success(responseUpdateWG))

        val result = dataSource.updateWG(requestUpdateWG)

        assertTrue(result is Result.Success)
        assertEquals(responseUpdateWG, result.data)

        verify(mockApiService).updateWG(requestUpdateWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `updateWG should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.updateWG(requestUpdateWG)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(false)
        whenever(mockWGResponse.code()).thenReturn(400)
        whenever(mockWGResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.updateWG(requestUpdateWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).updateWG(requestUpdateWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `updateWG should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.updateWG(requestUpdateWG)).thenReturn(mockWGResponse)
        whenever(mockWGResponse.isSuccessful).thenReturn(true)
        whenever(mockWGResponse.code()).thenReturn(200)
        whenever(mockWGResponse.body()).thenReturn(responseWG)
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.updateWG(requestUpdateWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).updateWG(requestUpdateWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `updateWG when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.updateWG(requestUpdateWG)).thenReturn(mockWGResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.updateWG(requestUpdateWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).updateWG(requestUpdateWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `updateWG when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.updateWG(requestUpdateWG)).thenReturn(mockWGResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockWGResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.updateWG(requestUpdateWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).updateWG(requestUpdateWG)
        verify(mockResponseHandler).handleResponse(mockWGResponse)
    }

    @Test
    fun `updateWG should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.updateWG(requestUpdateWG)).thenThrow(genericException)

        val result = dataSource.updateWG(requestUpdateWG)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `updateWG when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.updateWG(requestUpdateWG)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.updateWG(requestUpdateWG)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }
}
