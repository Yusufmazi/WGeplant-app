package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * This interface is responsible for managing the data of tasks in persistence.
 */
interface LocalTaskDataSource {

    /**
     * This method saves or updates a task in persistence.
     * @param task saving task
     */
    suspend fun saveTask(task: Task): Result<Unit, DomainError>

    /**
     * This method returns a specific Task from persistence.
     * @param taskId of the task
     */
    fun getTaskById(taskId: String): Flow<Result<Task, DomainError>>

    /**
     * This method returns all tasks of the current user from persistence.
     */
    fun getTaskList(): Flow<Result<List<Task>, DomainError>>

    /**
     * This method returns the tasks of the current user in a certain month.
     * @param month the month
     */
    fun getMonthlyTasks(month: YearMonth): Flow<Result<List<Task>, DomainError>>

    /**
     * This method deletes a specific task in persistence.
     * @param taskId of the task
     */
    suspend fun deleteTask(taskId: String): Result<Unit, DomainError>
}
