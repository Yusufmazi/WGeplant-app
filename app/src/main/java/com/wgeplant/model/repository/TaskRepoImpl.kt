package com.wgeplant.model.repository

import com.wgeplant.common.dto.response.toDomain
import com.wgeplant.model.datasource.local.LocalTaskDataSource
import com.wgeplant.model.datasource.local.LocalUserDataSource
import com.wgeplant.model.datasource.remote.RemoteTaskDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.domain.toRequestDto
import com.wgeplant.model.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

/**
 * This class is managing the task data with the server and persistence.
 * @param remoteTaskData to communicate with the server about the task data
 * @param localTaskData to manage the task data locally
 * @param localUserData to manage the affected user of the task
 */
class TaskRepoImpl @Inject constructor(
    private val remoteTaskData: RemoteTaskDataSource,
    private val localTaskData: LocalTaskDataSource,
    private val localUserData: LocalUserDataSource
) : TaskRepo {
    /**
     * This method creates a new task in the server.
     * It is saved in persistence, if the current user is part of that task.
     * @param task the new task
     */
    override suspend fun createTask(task: Task): Result<Unit, DomainError> {
        return when (val remoteTask = remoteTaskData.createTask(task.toRequestDto())) {
            is Result.Success -> {
                val remoteTaskData = remoteTask.data
                if (remoteTaskData.affectedUsers.contains(Persistence.getLocalUserId())) {
                    localTaskData.saveTask(remoteTaskData.toDomain())
                } else {
                    Result.Success(Unit)
                }
            }
            is Result.Error -> {
                remoteTask
            }
        }
    }

    /**
     * This method gets the task of the id from persistence.
     * @param taskId of the task
     */
    override fun getTaskById(taskId: String): Flow<Result<Task, DomainError>> {
        return localTaskData.getTaskById(taskId)
    }

    /**
     * This method gets all tasks of a user from persistence.
     */
    override fun getTaskList(): Flow<Result<List<Task>, DomainError>> {
        return localTaskData.getTaskList()
    }

    /**
     * This method returns the tasks of the current user from persistence that are in the month.
     * @param month the given month
     */
    override fun getMonthlyTasks(month: YearMonth): Flow<Result<List<Task>, DomainError>> {
        return localTaskData.getMonthlyTasks(month)
    }

    /**
     * This method deletes a task from the server and afterwards from persistence.
     * @param taskId of the task
     */
    override suspend fun deleteTask(taskId: String): Result<Unit, DomainError> {
        return when (val remoteDeletion = remoteTaskData.deleteTask(taskId)) {
            is Result.Success -> {
                localTaskData.deleteTask(taskId)
            }
            is Result.Error -> {
                remoteDeletion
            }
        }
    }

    /**
     * This method updates the data of a task on the server.
     * If the updated task still contains the current user, it is saved in persistence.
     * Otherwise it is deleted locally.
     * @param task the updated task
     */
    override suspend fun updateTask(task: Task): Result<Unit, DomainError> {
        return when (val remoteUpdate = remoteTaskData.updateTask(task.toRequestDto())) {
            is Result.Success -> {
                val updatedTask = remoteUpdate.data
                if (updatedTask.affectedUsers.contains(Persistence.getLocalUserId())) {
                    localTaskData.saveTask(updatedTask.toDomain())
                } else {
                    localTaskData.deleteTask(updatedTask.taskId)
                }
            }
            is Result.Error -> {
                remoteUpdate
            }
        }
    }

    /**
     * This method fetches the task of the id from the server and saves it in persistence.
     * @param taskId of the task
     */
    override suspend fun fetchAndSafe(taskId: String): Result<Unit, DomainError> {
        when (val remoteTask = remoteTaskData.getTaskById(taskId)) {
            is Result.Success -> {
                val foundTask = remoteTask.data.toDomain()
                val updatedUserIds = foundTask.affectedUsers
                return when (val userId = localUserData.getLocalUserId()) {
                    is Result.Success -> {
                        if (updatedUserIds.contains(userId.data)) {
                            localTaskData.saveTask(foundTask)
                        } else {
                            localTaskData.deleteTask(taskId)
                        }
                    }
                    is Result.Error -> {
                        userId
                    }
                }
            }
            is Result.Error -> {
                return remoteTask
            }
        }
    }

    /**
     * This method deletes a task from persistence.
     * It is used, when fcm updates the client about the deletion of the task.
     * @param taskId of the deleted task
     */
    override suspend fun deleteLocalTask(taskId: String): Result<Unit, DomainError> {
        return localTaskData.deleteTask(taskId)
    }
}
