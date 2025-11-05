package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.TaskRequestDTO
import com.wgeplant.common.dto.response.TaskResponseDTO
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import okio.IOException
import javax.inject.Inject

/**
 * This class is communicating with the server about task data.
 * @param apiService to send a request to the server
 * @param responseHandler to process the server response
 */
class RemoteTaskDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler
) : RemoteTaskDataSource {

    /**
     * This method creates a new task on the server.
     * @param task the new task
     */
    override suspend fun createTask(task: TaskRequestDTO): Result<TaskResponseDTO, DomainError> {
        return try {
            val response = apiService.createTask(task)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method updates an existing task on the server.
     * @param task the updated data
     */
    override suspend fun updateTask(task: TaskRequestDTO): Result<TaskResponseDTO, DomainError> {
        return try {
            val response = apiService.updateTask(task)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method gets a specific task from the server.
     * @param taskId of the task
     */
    override suspend fun getTaskById(taskId: String): Result<TaskResponseDTO, DomainError> {
        return try {
            val response = apiService.getTaskById(taskId)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method deletes a task on the server.
     * @param taskId of the task
     */
    override suspend fun deleteTask(taskId: String): Result<Unit, DomainError> {
        return try {
            val response = apiService.deleteTask(taskId)
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
