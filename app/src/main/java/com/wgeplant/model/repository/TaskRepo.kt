package com.wgeplant.model.repository

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * This interface is responsible to administrate the task data.
 */
interface TaskRepo {

    /**
     * This method creates a new task in the server.
     * It is saved in persistence, if the current user is part of that task.
     * @param task the new task
     */
    suspend fun createTask(task: Task): Result<Unit, DomainError>

    /**
     * This method gets the task of the id from persistence.
     * @param taskId of the task
     */
    fun getTaskById(taskId: String): Flow<Result<Task, DomainError>>

    /**
     * This method gets all tasks of a user from persistence.
     */
    fun getTaskList(): Flow<Result<List<Task>, DomainError>>

    /**
     * This method returns the tasks of the current user from persistence that are in the month.
     * @param month the given month
     */
    fun getMonthlyTasks(month: YearMonth): Flow<Result<List<Task>, DomainError>>

    /**
     * This method deletes a task from the server and afterwards from persistence.
     * @param taskId of the task
     */
    suspend fun deleteTask(taskId: String): Result<Unit, DomainError>

    /**
     * This method updates the data of a task on the server.
     * If the updated task still contains the current user, it is saved in persistence.
     * Otherwise it is deleted locally.
     * @param task the updated task
     */
    suspend fun updateTask(task: Task): Result<Unit, DomainError>

    /**
     * This method fetches the task of the id from the server and saves it in persistence.
     * @param taskId of the task
     */
    suspend fun fetchAndSafe(taskId: String): Result<Unit, DomainError>

    /**
     * This method deletes a task from persistence.
     * It is used, when fcm updates the client about the deletion of the task.
     * @param taskId of the deleted task
     */
    suspend fun deleteLocalTask(taskId: String): Result<Unit, DomainError>
}
