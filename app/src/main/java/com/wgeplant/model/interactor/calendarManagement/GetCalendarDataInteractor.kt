package com.wgeplant.model.interactor.calendarManagement

import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * This interface is responsible for requesting calendar data.
 */
interface GetCalendarDataInteractor {
    /**
     * This method gets the appointments for the given month
     * and sends all changes of the saved appointment list to the UI.
     * @param month: The month for which the appointments that are requested.
     */
    fun getAppointmentsForMonth(month: YearMonth): Flow<Result<List<Appointment>, DomainError>>

    /**
     * This method gets all relevant tasks of the local user
     * and sends all changes of the saved task list to the UI. The relevant tasks are all tasks,
     * that aren't checked off in the past.
     */
    fun getTaskList(): Flow<Result<List<Task>, DomainError>>

    /**
     * This method gets the tasks for the given month
     * and sends all changes of the saved task list to the UI.
     * @param month: The month for which the tasks that are requested.
     */
    fun getTasksForMonth(month: YearMonth): Flow<Result<List<Task>, DomainError>>

    /**
     * This method gets the appointment with the given ID
     * and sends all changes to the UI.
     * @param appointmentId: The ID of the appointment that got requested.
     */
    fun getAppointment(appointmentId: String): Flow<Result<Appointment, DomainError>>

    /**
     * This method gets the task with the given ID
     * and sends all changes to the UI.
     * @param taskId: The ID of the task that got requested.
     */
    fun getTask(taskId: String): Flow<Result<Task, DomainError>>
    suspend fun changeTaskState(taskId: String): Result<Unit, DomainError>
}
