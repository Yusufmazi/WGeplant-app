package com.wgeplant.model.interactor.calendarManagement

import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.repository.AppointmentRepo
import com.wgeplant.model.repository.TaskRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import javax.inject.Inject

/**
 * This class is responsible for implementing the calendar related use cases.
 * @param appointmentRepo: The repository for appointments.
 * @param taskRepo: The repository for tasks.
 */
class GetCalendarDataInteractorImpl @Inject constructor(
    private val appointmentRepo: AppointmentRepo,
    private val taskRepo: TaskRepo
) : GetCalendarDataInteractor {

    companion object {
        /** The constant is used to confirm that the task is checked off.*/
        private const val TASK_STATE_CHECKED_OFF = true

        /** The constant is used to confirm that the task is not checked off.*/
        private const val TASK_STATE_NOT_CHECKED_OFF = false
    }

    /**
     * This method gets all appointments for a given month and sends all changes of the saved list to the UI.
     * @param month: The month for which the appointments should be retrieved.
     */
    override fun getAppointmentsForMonth(month: YearMonth): Flow<Result<List<Appointment>, DomainError>> {
        return appointmentRepo.getMonthlyAppointments(month)
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }

    /**
     * This method gets all tasks and sends all changes of the saved list to the UI.
     */
    override fun getTaskList(): Flow<Result<List<Task>, DomainError>> {
        return taskRepo.getTaskList()
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }

    /**
     * This method gets all tasks for a given month and sends all changes of the saved list to the UI.
     * @param month: The month for which the tasks should be retrieved.
     */
    override fun getTasksForMonth(month: YearMonth): Flow<Result<List<Task>, DomainError>> {
        return taskRepo.getMonthlyTasks(month)
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }

    /**
     * This method gets an appointment by its ID and sends all changes of the saved appointment to the UI.
     * @param appointmentId: The ID of the appointment to be retrieved.
     */
    override fun getAppointment(appointmentId: String): Flow<Result<Appointment, DomainError>> {
        return appointmentRepo.getAppointmentById(appointmentId)
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }

    /**
     * This method gets a task by its ID and sends all changes of the saved task to the UI.
     * @param taskId: The ID of the task to be retrieved.
     */
    override fun getTask(taskId: String): Flow<Result<Task, DomainError>> {
        return taskRepo.getTaskById(taskId)
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }

    /**
     * This method changes the state of a task. If the task is not checked off, it will be marked as uncompleted
     * and if it's not checked off, it will get checked off. This method is placed in this class
     * since the placement is more accommodating for the ViewModels usage of this method.
     * @param taskId: The ID of the task to be changed.
     */
    override suspend fun changeTaskState(taskId: String): Result<Unit, DomainError> {
        val getTaskResult: Result<Task, DomainError> = try {
            taskRepo.getTaskById(taskId).first()
        } catch (e: Exception) {
            return Result.Error(DomainError.Unknown(e))
        }
        return when (getTaskResult) {
            is Result.Success -> {
                val task = getTaskResult.data
                val newTask: Task = if (!task.stateOfTask) {
                    createTaskObjectForStateChange(task, TASK_STATE_CHECKED_OFF)
                } else {
                    createTaskObjectForStateChange(task, TASK_STATE_NOT_CHECKED_OFF)
                }
                return taskRepo.updateTask(newTask)
            }
            is Result.Error -> Result.Error(getTaskResult.error)
        }
    }

    /**
     * This method creates a new task object.
     * @param task: The task to be changed.
     * @param stateOfTask: The new state of the task.
     */
    private fun createTaskObjectForStateChange(task: Task, stateOfTask: Boolean): Task {
        val checkedOffTask = Task(
            taskId = task.taskId,
            title = task.title,
            date = task.date,
            affectedUsers = task.affectedUsers,
            color = task.color,
            description = task.description,
            stateOfTask = stateOfTask
        )
        return checkedOffTask
    }
}
