package com.wgeplant.model.persistence

import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.WG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

/**
 * This object saves all data that is needed to use the app locally.
 */
object Persistence {
    private const val NOT_FOUND = -1

    /**
     * The local user id.
     */
    private var _localUserId: String? = null

    /**
     * All users in the WG.
     */
    private val _usersInWG = MutableStateFlow<List<User>>(emptyList())
    val usersInWG: StateFlow<List<User>> = _usersInWG

    /**
     * The WG of the local user.
     */
    private val _wgOfLocalUser = MutableStateFlow<WG?>(null)
    val wgOfLocalUser: StateFlow<WG?> = _wgOfLocalUser

    /**
     * The appointments of the local user.
     */
    private val _userAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val userAppointments: StateFlow<List<Appointment>> = _userAppointments

    /**
     * The tasks of the local user.
     */
    private val _userTasks = MutableStateFlow<List<Task>>(emptyList())
    private val userTasks: StateFlow<List<Task>> = _userTasks

    /**
     * All Absences all user in the WG.
     */
    private val _userAbsences = MutableStateFlow<List<Absence>>(emptyList())
    private val userAbsence: StateFlow<List<Absence>> = _userAbsences

    fun setLocalUserId(userId: String) {
        _localUserId = userId
    }

    fun saveUsersInWG(newUsers: List<User>) {
        _usersInWG.value = newUsers
    }

    /**
     * This method adds a new user to the list,
     * if the userId can´t be found in the current list.
     * Otherwise the existing user is replaced by the updated user object.
     * @param newUser the updated or new user
     */
    fun updateUserInWG(newUser: User) {
        val currentUsers = _usersInWG.value
        if (currentUsers.any { user -> user.userId == newUser.userId }) {
            val updatedUsers = currentUsers.map { existingUser ->
                if (existingUser.userId == newUser.userId) {
                    newUser
                } else {
                    existingUser
                }
            }
            saveUsersInWG(updatedUsers)
        } else {
            val updatedList = currentUsers + newUser
            saveUsersInWG(updatedList)
        }
    }

    fun saveWGOfLocalUser(newWG: WG?) {
        _wgOfLocalUser.value = newWG
    }

    fun saveUserAppointments(newAppointments: List<Appointment>) {
        _userAppointments.value = newAppointments
    }

    /**
     * This method adds a new appointment to the list,
     * if the appointment ID can't be found in the currently saved list
     * and replaces the saved appointment with an edited one if the ID is found.
     * @param newAppointment the updated or new appointment
     */
    fun saveAppointment(newAppointment: Appointment): Result<Unit, DomainError.PersistenceError> {
        return try {
            val currentAppointments = _userAppointments.value.toMutableList()
            val updateIndex = currentAppointments.indexOfFirst { it.appointmentId == newAppointment.appointmentId }
            if (updateIndex != NOT_FOUND) {
                currentAppointments[updateIndex] = newAppointment
            } else {
                currentAppointments.add(newAppointment)
            }
            _userAppointments.value = currentAppointments.toList()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    fun deleteAppointment(appointmentId: String): Result<Unit, DomainError> {
        return try {
            val currentAppointments = _userAppointments.value.toMutableList()
            val appointmentIndex = currentAppointments.indexOfFirst { it.appointmentId == appointmentId }
            if (appointmentIndex != NOT_FOUND) {
                val deletingAppointment = currentAppointments[appointmentIndex]
                currentAppointments.remove(deletingAppointment)
                _userAppointments.value = currentAppointments.toList()
                Result.Success(Unit)
            } else {
                Result.Error(DomainError.NotFoundError)
            }
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    fun saveUserTasks(newTasks: List<Task>) {
        _userTasks.value = newTasks
    }

    /**
     * This method adds a new task to the list,
     * if the task ID can't be found in the currently saved list
     * and replaces the saved task with an edited one if the ID is found.
     * @param newTask the updated or new task
     */
    fun saveTask(newTask: Task): Result<Unit, DomainError.PersistenceError> {
        return try {
            val currentTasks = _userTasks.value.toMutableList()
            val updateIndex = currentTasks.indexOfFirst { it.taskId == newTask.taskId }
            if (updateIndex != NOT_FOUND) {
                currentTasks[updateIndex] = newTask
            } else {
                currentTasks.add(newTask)
            }
            _userTasks.value = currentTasks.toList()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    fun deleteTask(taskId: String): Result<Unit, DomainError> {
        return try {
            val currentTasks = _userTasks.value.toMutableList()
            val taskIndex = currentTasks.indexOfFirst { it.taskId == taskId }
            if (taskIndex != NOT_FOUND) {
                val deletingTask = currentTasks[taskIndex]
                currentTasks.remove(deletingTask)
                _userTasks.value = currentTasks.toList()
                Result.Success(Unit)
            } else {
                Result.Error(DomainError.NotFoundError)
            }
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    fun saveUserAbsences(newAbsences: List<Absence>) {
        _userAbsences.value = newAbsences
    }

    /**
     * This method adds a new absence to the list,
     * if the absence ID can't be found in the currently saved list
     * and replaces the saved absence with an edited one if the ID is found.
     * @param newAbsence the updated or new absence
     */
    fun saveAbsence(newAbsence: Absence): Result<Unit, DomainError.PersistenceError> {
        return try {
            val currentAbsences = _userAbsences.value.toMutableList()
            val updateIndex = currentAbsences.indexOfFirst { it.absenceId == newAbsence.absenceId }
            if (updateIndex != NOT_FOUND) {
                currentAbsences[updateIndex] = newAbsence
            } else {
                currentAbsences.add(newAbsence)
            }
            _userAbsences.value = currentAbsences.toList()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    fun deleteAbsence(absenceId: String): Result<Unit, DomainError> {
        return try {
            val currentAbsences = _userAbsences.value.toMutableList()
            val absenceIndex = currentAbsences.indexOfFirst { it.absenceId == absenceId }
            if (absenceIndex != NOT_FOUND) {
                val deletingAbsence = currentAbsences[absenceIndex]
                currentAbsences.remove(deletingAbsence)
                _userAbsences.value = currentAbsences.toList()
                Result.Success(Unit)
            } else {
                Result.Error(DomainError.NotFoundError)
            }
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    fun getLocalUserId(): String? {
        return _localUserId
    }

    fun getUsersInWG(): Flow<List<User>> {
        return _usersInWG
    }

    fun getUserInWG(userId: String): Flow<User?> {
        return usersInWG.map { userList ->
            userList.find { it.userId == userId }
        }.distinctUntilChanged()
    }

    fun getWGOfLocalUser(): Flow<WG?> {
        return _wgOfLocalUser
    }

    fun getUserAppointments(): Flow<List<Appointment>> {
        return _userAppointments.map { appointmentList ->
            appointmentList
        }.distinctUntilChanged()
    }

    fun getAppointment(appointmentId: String): Flow<Appointment?> {
        return userAppointments.map { appointmentList ->
            appointmentList.find { it.appointmentId == appointmentId }
        }.distinctUntilChanged()
    }

    /**
     * This method gets all appointments of the local user for the given month.
     * @param month: The month for which the appointments that are requested.
     */
    fun getMonthlyAppointments(month: YearMonth): Flow<List<Appointment>> {
        return userAppointments.map { appointmentList ->
            appointmentList.filter { appointment ->
                val appointmentStart = YearMonth.from(appointment.startDate)
                val appointmentEnd = YearMonth.from(appointment.endDate)
                if (appointmentStart == month || appointmentEnd == month) {
                    true
                } else if (appointmentStart.isBefore(month) && appointmentEnd.isAfter(month)) {
                    true
                } else {
                    false
                }
            }
        }.distinctUntilChanged()
    }

    /**
     * This method gets all tasks of the local user
     * except those that have a date in the past and are additionally checked off.
     */
    fun getUserTasks(): Flow<List<Task>> {
        return _userTasks.map { taskList ->
            taskList.filter { task ->
                val today = LocalDate.now()
                if (task.date == null) {
                    true
                } else if (task.date.isAfter(today) || task.date == today) {
                    true
                } else if (task.date.isBefore(today) && !task.stateOfTask) {
                    true
                } else {
                    false
                }
            }
        }.distinctUntilChanged()
    }

    fun getTask(taskId: String): Flow<Task?> {
        return userTasks.map { taskList ->
            taskList.find { it.taskId == taskId }
        }.distinctUntilChanged()
    }

    /**
     * This method gets all tasks that have a date in the given month of the local user.
     * @param month: The month for which the tasks that are requested.
     */
    fun getMonthlyTask(month: YearMonth): Flow<List<Task>> {
        return userTasks.map { taskList ->
            taskList.filter { task ->
                val taskDate: LocalDate? = task.date
                if (taskDate != null) {
                    YearMonth.from(taskDate) == month
                } else {
                    true
                }
            }
        }.distinctUntilChanged()
    }

    fun getAbsence(absenceId: String): Flow<Absence?> {
        return userAbsence.map { absenceList ->
            absenceList.find { it.absenceId == absenceId }
        }.distinctUntilChanged()
    }

    fun getAbsenceOfUser(userId: String): Flow<List<Absence>> {
        return userAbsence.map { absenceList ->
            absenceList.filter { absence ->
                absence.userId == userId
            }
        }.distinctUntilChanged()
    }

    fun deleteAllData(): Result<Unit, DomainError> {
        return try {
            _localUserId = null
            _usersInWG.value = emptyList()
            _wgOfLocalUser.value = null
            _userAppointments.value = emptyList()
            _userTasks.value = emptyList()
            _userAbsences.value = emptyList()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    /**
     * This method deletes all wg related data.
     */
    fun deleteWGData(): Result<Unit, DomainError> {
        return try {
            val localUser = _usersInWG.value.find { it.userId == _localUserId }
            if (localUser != null) {
                _usersInWG.value = listOf(localUser)
            } else {
                Result.Error(DomainError.NotFoundError)
            }
            _wgOfLocalUser.value = null
            _userAppointments.value = emptyList()
            _userTasks.value = emptyList()
            _userAbsences.value = emptyList()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }
}
