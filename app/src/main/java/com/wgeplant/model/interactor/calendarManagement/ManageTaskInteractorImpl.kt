package com.wgeplant.model.interactor.calendarManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.repository.TaskRepo
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * This class is responsible for implementing the task related use cases.
 * @param taskRepo: The repository for tasks.
 */
class ManageTaskInteractorImpl @Inject constructor(
    private val taskRepo: TaskRepo
) : ManageTaskInteractor {

    companion object {
        /** The constant is used to confirm that the task is not checked off.*/
        private const val TASK_STATE_NOT_CHECKED_OFF = false
    }

    /**
     * This method starts the creation of a new task.
     * @param input: The input data for the new task.
     */
    override suspend fun executeCreation(input: CreateTaskInput): Result<Unit, DomainError> {
        val newTask = createTaskObject(input, null, TASK_STATE_NOT_CHECKED_OFF)
        return taskRepo.createTask(newTask)
    }

    /**
     * This method starts the editing of an existing task.
     * @param taskId: The ID of the task to be edited.
     * @param edit: The edited data for the task.
     */
    override suspend fun executeEditing(
        taskId: String,
        edit: CreateTaskInput
    ): Result<Unit, DomainError> {
        val getTaskResult: Result<Task, DomainError> = try {
            taskRepo.getTaskById(taskId).first()
        } catch (e: Exception) {
            return Result.Error(DomainError.Unknown(e))
        }
        return when (getTaskResult) {
            is Result.Success -> {
                val currentTask = getTaskResult.data
                val newTask = createTaskObject(edit, taskId, currentTask.stateOfTask)
                return taskRepo.updateTask(newTask)
            }
            is Result.Error -> Result.Error(getTaskResult.error)
        }
    }

    /**
     * This method starts the deletion of an existing task.
     * @param taskId: The ID of the task to be deleted.
     */
    override suspend fun executeDeletion(taskId: String): Result<Unit, DomainError> {
        return taskRepo.deleteTask(taskId)
    }

    /**
     * This method creates a new task object.
     * @param input: The input data for the new task.
     * @param taskId: The ID of the new task. If it's a new Task the ID is null.
     * @param stateOfTask: The state of the new task.
     */
    private fun createTaskObject(input: CreateTaskInput, taskId: String?, stateOfTask: Boolean): Task {
        val newTask = Task(
            taskId = taskId,
            title = input.title,
            date = input.date,
            affectedUsers = input.affectedUsers,
            color = input.color,
            description = input.description,
            stateOfTask = stateOfTask
        )
        return newTask
    }
}
