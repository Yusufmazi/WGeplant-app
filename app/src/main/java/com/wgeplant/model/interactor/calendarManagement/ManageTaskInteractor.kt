package com.wgeplant.model.interactor.calendarManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for starting the task related use cases.
 */
interface ManageTaskInteractor {
    /**
     * This method creates a new task.
     * @param input: The input for the task creation.
     */
    suspend fun executeCreation(input: CreateTaskInput): Result<Unit, DomainError>

    /**
     * This method edits an existing task.
     * @param taskId: The ID of the task that got edited.
     * @param edit: The input for the edited task.
     */
    suspend fun executeEditing(taskId: String, edit: CreateTaskInput): Result<Unit, DomainError>

    /**
     * This method deletes an existing task.
     * @param taskId: The ID of the task that got deleted.
     */
    suspend fun executeDeletion(taskId: String): Result<Unit, DomainError>
}
