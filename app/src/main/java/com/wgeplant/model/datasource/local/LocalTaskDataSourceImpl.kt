package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import java.time.YearMonth
import javax.inject.Inject

/**
 * This class manages the data of tasks in persistence concerning the user.
 */
class LocalTaskDataSourceImpl @Inject constructor() : LocalTaskDataSource {
    /**
     * This method saves or updates a task in persistence.
     * @param task saving task
     */
    override suspend fun saveTask(task: Task): Result<Unit, DomainError> {
        return try {
            Persistence.saveTask(task)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    /**
     * This method returns a specific Task from persistence.
     * @param taskId of the task
     */
    override fun getTaskById(taskId: String): Flow<Result<Task, DomainError>> {
        return Persistence.getTask(taskId).map { foundTask ->
            if (foundTask != null) {
                Result.Success(foundTask)
            } else {
                Result.Error(DomainError.NotFoundError)
            }
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method returns all tasks of the current user from persistence.
     */
    override fun getTaskList(): Flow<Result<List<Task>, DomainError>> {
        return Persistence.getUserTasks().map { taskList ->
            Result.Success(taskList)
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method returns the tasks of the current user in a certain month.
     * @param month the month
     */
    override fun getMonthlyTasks(month: YearMonth): Flow<Result<List<Task>, DomainError>> {
        return Persistence.getMonthlyTask(month).map { taskList ->
            Result.Success(taskList)
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method deletes a specific task in persistence.
     * @param taskId of the task
     */
    override suspend fun deleteTask(taskId: String): Result<Unit, DomainError> {
        return Persistence.deleteTask(taskId)
    }
}
