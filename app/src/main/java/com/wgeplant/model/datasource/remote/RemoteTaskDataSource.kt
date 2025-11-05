package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.TaskRequestDTO
import com.wgeplant.common.dto.response.TaskResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for communicating with the server about task data.
 */
interface RemoteTaskDataSource {
    /**
     * This method creates a new task on the server.
     * @param task the new task
     */
    suspend fun createTask(task: TaskRequestDTO): Result<TaskResponseDTO, DomainError>

    /**
     * This method updates an existing task on the server.
     * @param task the updated data
     */
    suspend fun updateTask(task: TaskRequestDTO): Result<TaskResponseDTO, DomainError>

    /**
     * This method gets a specific task from the server.
     * @param taskId of the task
     */
    suspend fun getTaskById(taskId: String): Result<TaskResponseDTO, DomainError>

    /**
     * This method deletes a task on the server.
     * @param taskId of the task
     */
    suspend fun deleteTask(taskId: String): Result<Unit, DomainError>
}
