package com.wgeplant.model.datasource.remote

import androidx.compose.ui.graphics.Color
import com.wgeplant.common.dto.requests.TaskRequestDTO
import com.wgeplant.common.dto.response.TaskResponseDTO
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
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteTaskDataSourceImplTest {
    @Mock
    private lateinit var mockApiService: ApiService

    @Mock
    private lateinit var mockResponseHandler: ResponseHandler

    @Mock
    private lateinit var mockTaskResponse: Response<TaskResponseDTO>

    @Mock
    private lateinit var mockUnitResponse: Response<Unit>

    @Mock
    private lateinit var mockErrorResponse: ResponseBody

    private lateinit var dataSource: RemoteTaskDataSourceImpl

    private val userId1 = "user2"
    private val userId2 = "user1"
    private val taskId = "task1234"
    private val requestCreateTask = TaskRequestDTO(
        taskId = null,
        title = "Müll rausbringen",
        date = null,
        affectedUsers = listOf(userId1, userId2),
        description = null,
        color = Color.Yellow,
        stateOfTask = false
    )
    private val responseTask = TaskResponseDTO(
        taskId = taskId,
        title = "Müll rausbringen",
        date = null,
        affectedUsers = listOf(userId1, userId2),
        color = Color.Yellow,
        description = null,
        stateOfTask = false
    )
    private val requestUpdateTask = TaskRequestDTO(
        taskId = taskId,
        title = "Müll rausbringen",
        date = LocalDate.now(),
        affectedUsers = listOf(userId2),
        description = null,
        color = Color.Yellow,
        stateOfTask = false
    )
    private val updatedTask = TaskResponseDTO(
        taskId = taskId,
        title = "Müll rausbringen",
        date = LocalDate.now(),
        affectedUsers = listOf(userId2),
        color = Color.Yellow,
        description = null,
        stateOfTask = false
    )

    @Before
    fun setUp() {
        mockApiService = mock()
        mockResponseHandler = mock()
        mockTaskResponse = mock()
        mockUnitResponse = mock()
        mockErrorResponse = mock()
        dataSource = RemoteTaskDataSourceImpl(mockApiService, mockResponseHandler)
    }

    @Test
    fun `createTask should return a TaskResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.createTask(requestCreateTask)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(true)
        whenever(mockTaskResponse.code()).thenReturn(200)
        whenever(mockTaskResponse.body()).thenReturn(responseTask)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Success(responseTask))

        val result = dataSource.createTask(requestCreateTask)

        assertTrue(result is Result.Success)
        assertEquals(responseTask, result.data)

        verify(mockApiService).createTask(requestCreateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `createTask should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.createTask(requestCreateTask)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(false)
        whenever(mockTaskResponse.code()).thenReturn(400)
        whenever(mockTaskResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.createTask(requestCreateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).createTask(requestCreateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `createTask should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.createTask(requestCreateTask)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(true)
        whenever(mockTaskResponse.code()).thenReturn(200)
        whenever(mockTaskResponse.body()).thenReturn(responseTask)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.createTask(requestCreateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).createTask(requestCreateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `createTask when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.createTask(requestCreateTask)).thenReturn(mockTaskResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.createTask(requestCreateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).createTask(requestCreateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `createTask when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.createTask(requestCreateTask)).thenReturn(mockTaskResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.createTask(requestCreateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).createTask(requestCreateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `createTask when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.createTask(requestCreateTask)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.createTask(requestCreateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `createTask should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.createTask(requestCreateTask)).thenThrow(genericException)

        val result = dataSource.createTask(requestCreateTask)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `updateTask should return a TaskResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.updateTask(requestUpdateTask)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(true)
        whenever(mockTaskResponse.code()).thenReturn(200)
        whenever(mockTaskResponse.body()).thenReturn(updatedTask)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Success(updatedTask))

        val result = dataSource.updateTask(requestUpdateTask)

        assertTrue(result is Result.Success)
        assertEquals(updatedTask, result.data)

        verify(mockApiService).updateTask(requestUpdateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `updateTask should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.updateTask(requestUpdateTask)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(false)
        whenever(mockTaskResponse.code()).thenReturn(400)
        whenever(mockTaskResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.updateTask(requestUpdateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).updateTask(requestUpdateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `updateTask should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.updateTask(requestUpdateTask)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(true)
        whenever(mockTaskResponse.code()).thenReturn(200)
        whenever(mockTaskResponse.body()).thenReturn(updatedTask)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.updateTask(requestUpdateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).updateTask(requestUpdateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `updateTask when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.updateTask(requestUpdateTask)).thenReturn(mockTaskResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.updateTask(requestUpdateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).updateTask(requestUpdateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `updateTask when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.updateTask(requestUpdateTask)).thenReturn(mockTaskResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.updateTask(requestUpdateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).updateTask(requestUpdateTask)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `updateTask should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.updateTask(requestUpdateTask)).thenThrow(genericException)

        val result = dataSource.updateTask(requestUpdateTask)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `updateTask when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.updateTask(requestUpdateTask)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.updateTask(requestUpdateTask)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `getTaskById should return a TaskResponseDTO when API call is successful`() = runTest {
        whenever(mockApiService.getTaskById(taskId)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(true)
        whenever(mockTaskResponse.code()).thenReturn(200)
        whenever(mockTaskResponse.body()).thenReturn(responseTask)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Success(responseTask))

        val result = dataSource.getTaskById(taskId)

        assertTrue(result is Result.Success)
        assertEquals(responseTask, result.data)

        verify(mockApiService).getTaskById(taskId)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `getTaskById should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.getTaskById(taskId)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(false)
        whenever(mockTaskResponse.code()).thenReturn(400)
        whenever(mockTaskResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.getTaskById(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).getTaskById(taskId)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `getTaskById should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.getTaskById(taskId)).thenReturn(mockTaskResponse)
        whenever(mockTaskResponse.isSuccessful).thenReturn(true)
        whenever(mockTaskResponse.code()).thenReturn(200)
        whenever(mockTaskResponse.body()).thenReturn(responseTask)
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.getTaskById(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).getTaskById(taskId)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `getTaskById when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.getTaskById(taskId)).thenReturn(mockTaskResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.getTaskById(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).getTaskById(taskId)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `getTaskById when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.getTaskById(taskId)).thenReturn(mockTaskResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleResponse(mockTaskResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.getTaskById(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).getTaskById(taskId)
        verify(mockResponseHandler).handleResponse(mockTaskResponse)
    }

    @Test
    fun `getTaskById should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.getTaskById(taskId)).thenThrow(genericException)

        val result = dataSource.getTaskById(taskId)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `getTaskById when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.getTaskById(taskId)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.getTaskById(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }

    @Test
    fun `deleteTask should return a Unit when API call is successful`() = runTest {
        whenever(mockApiService.deleteTask(taskId)).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Success(Unit))

        val result = dataSource.deleteTask(taskId)

        assertTrue(result is Result.Success)

        verify(mockApiService).deleteTask(taskId)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteTask should return error when API call is successful but response is error`() = runTest {
        val expectedError = DomainError.ServerError.BadRequest("ungültige Anfrage")

        whenever(mockApiService.deleteTask(taskId)).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(false)
        whenever(mockUnitResponse.code()).thenReturn(400)
        whenever(mockUnitResponse.errorBody()).thenReturn(mockErrorResponse)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.deleteTask(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).deleteTask(taskId)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteTask should return error when API call is successful but client has error`() = runTest {
        val expectedError = DomainError.NetworkError

        whenever(mockApiService.deleteTask(taskId)).thenReturn(mockUnitResponse)
        whenever(mockUnitResponse.isSuccessful).thenReturn(true)
        whenever(mockUnitResponse.code()).thenReturn(204)
        whenever(mockUnitResponse.body()).thenReturn(Unit)
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenReturn(Result.Error(expectedError))

        val result = dataSource.deleteTask(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockApiService).deleteTask(taskId)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteTask when responseHandler throws IOException`() = runTest {
        whenever(mockApiService.deleteTask(taskId)).thenReturn(mockUnitResponse)

        val simulatedIOExceptionOfHandler = IOException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedIOExceptionOfHandler
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.deleteTask(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).deleteTask(taskId)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteTask when responseHandler throws generic exception`() = runTest {
        whenever(mockApiService.deleteTask(taskId)).thenReturn(mockUnitResponse)

        val simulatedRuntimeExceptionOfHandler = RuntimeException("Simulated error")
        whenever(mockResponseHandler.handleUnitResponse(mockUnitResponse)).thenAnswer {
            throw simulatedRuntimeExceptionOfHandler
        }

        val expectedError = DomainError.Unknown(simulatedRuntimeExceptionOfHandler)

        val result = dataSource.deleteTask(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockApiService).deleteTask(taskId)
        verify(mockResponseHandler).handleUnitResponse(mockUnitResponse)
    }

    @Test
    fun `deleteTask should return error when handling general exception`() = runTest {
        val genericException = RuntimeException("Simulated error")

        whenever(mockApiService.deleteTask(taskId)).thenThrow(genericException)

        val result = dataSource.deleteTask(taskId)

        assertTrue(result is Result.Error)
        assertTrue(result.error is DomainError.Unknown)
    }

    @Test
    fun `deleteTask when apiService throws IOException returns error`() = runTest {
        val simulatedIOException = IOException("Simulated error")

        whenever(mockApiService.deleteTask(taskId)).thenAnswer {
            throw simulatedIOException
        }

        val expectedError = DomainError.NetworkError

        val result = dataSource.deleteTask(taskId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
    }
}
