package com.wgeplant.ui.calendar

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * ViewModel responsible for managing the calendar view state, handling user interaction, and processing domain data.
 *
 * @property model The interactor to fetch appointments and tasks
 * @property manageWGProfileInteractor The interactor for WG profile data
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    model: GetCalendarDataInteractor,
    private val manageWGProfileInteractor: ManageWGProfileInteractor
) : BaseViewModel<CalendarUiState, GetCalendarDataInteractor>(CalendarUiState(), model), ICalendarViewModel {

    companion object {
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
    }

    private var monthDataObservationJob: Job? = null
    private var weekDataObservationJob: Job? = null
    private var profilePictureObservationJob: Job? = null

    init {
        observeWgProfilePicture()
        observeMonthData(uiState.value.currentlyDisplayedMonth)
    }

    private fun observeWgProfilePicture() {
        profilePictureObservationJob?.cancel()

        profilePictureObservationJob = viewModelScope.launch {
            setLoading(true)

            manageWGProfileInteractor.getWGData()
                .distinctUntilChanged()
                .catch {
                    showError(UNEXPECTED_ERROR)
                    updateUiState { it.copy(wgProfileImageUrl = null) }
                    setLoading(false)
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val wg = result.data
                            if (wg != null) {
                                updateUiState { it.copy(wgProfileImageUrl = wg.profilePicture) }
                            } else {
                                updateUiState { it.copy(wgProfileImageUrl = null) }
                            }
                        }
                        is Result.Error -> {
                            handleDomainError(result.error)
                            updateUiState { it.copy(wgProfileImageUrl = null) }
                        }
                    }
                    setLoading(false)
                }
        }
    }

    private fun observeMonthData(month: YearMonth) {
        monthDataObservationJob?.cancel()

        val firstDayOfMonth = month.atDay(1)
        val lastDayOfMonth = month.atDay(month.lengthOfMonth())

        monthDataObservationJob = viewModelScope.launch {
            setLoading(true)

            combine(
                model.getAppointmentsForMonth(month),
                model.getTasksForMonth(month)
            ) {
                    appointmentsResult, tasksResult ->
                val appointments = when (appointmentsResult) {
                    is Result.Success -> appointmentsResult.data
                    is Result.Error -> {
                        handleDomainError(appointmentsResult.error)
                        emptyList()
                    }
                }
                val tasks = when (tasksResult) {
                    is Result.Success -> tasksResult.data
                    is Result.Error -> {
                        handleDomainError(tasksResult.error)
                        emptyList()
                    }
                }

                val appointmentSegments = mutableListOf<AppointmentDisplaySegment>()

                appointments.sortedBy { it.startDate }.forEach { appointment ->
                    appointmentSegments.addAll(
                        processSegmentsForMultiDayUi(
                            getAppointmentDisplaySegments(appointment, firstDayOfMonth, lastDayOfMonth)
                        )
                    )
                }

                val processedAppointmentSegments = calculateAppointmentLanes(appointmentSegments)

                calculateDaysForGrid(month, processedAppointmentSegments, tasks)
            }
                .distinctUntilChanged()
                .catch {
                    showError(UNEXPECTED_ERROR)
                    updateUiState {
                        it.copy(
                            daysForCalendarGrid = calculateDaysForGrid(month, emptyList(), emptyList()),
                            currentlyDisplayedMonth = month
                        )
                    }
                    if (isLoading.value) {
                        setLoading(false)
                    }
                }
                .collect { monthDays ->
                    updateUiState { it.copy(daysForCalendarGrid = monthDays, currentlyDisplayedMonth = month) }
                    if (isLoading.value) {
                        setLoading(false)
                    }
                }
        }
    }

    private fun calculateDaysForGrid(
        month: YearMonth,
        appointmentSegments: List<AppointmentDisplaySegment>,
        tasks: List<Task>
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val todayDate = LocalDate.now()

        // Calculate days in month and empty cells
        val daysInMonth = month.lengthOfMonth()
        val firstDayOfMonth = month.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val startEmptyCells = if (firstDayOfWeek == 0) 6 else firstDayOfWeek - 1

        val totalCells = daysInMonth + startEmptyCells
        val numRows = (totalCells + 6) / 7

        // Days of previous month
        val prevMonth = month.minusMonths(1)
        for (i in (prevMonth.lengthOfMonth() - startEmptyCells + 1)..prevMonth.lengthOfMonth()) {
            days.add(
                CalendarDay(
                    LocalDate.of(prevMonth.year, prevMonth.month, i),
                    isCurrentMonth = false,
                    hasEntries = false,
                    emptyList(),
                    emptyList(),
                    false
                )
            )
        }

        // Days of current month
        for (i in 1..daysInMonth) {
            val date = month.atDay(i)
            val isToday = date.isEqual(todayDate)
            val appointmentSegmentsOfDay = appointmentSegments.filter { appointment ->
                appointment.date == date
            }.sortedBy { it.laneIndex ?: Int.MAX_VALUE }
            val tasksOfDay = tasks.filter { task ->
                task.date == date
            }
            val hasEntries = appointmentSegmentsOfDay.isNotEmpty() || tasksOfDay.isNotEmpty()
            days.add(CalendarDay(date, true, hasEntries, appointmentSegmentsOfDay, tasksOfDay, isToday))
        }

        // Days of following month
        val remainingCells = (numRows * 7) - days.size
        val nextMonth = month.plusMonths(1)
        for (i in 1..remainingCells) {
            days.add(
                CalendarDay(
                    LocalDate.of(nextMonth.year, nextMonth.month, i),
                    isCurrentMonth = false,
                    hasEntries = false,
                    emptyList(),
                    emptyList(),
                    false
                )
            )
        }
        return days
    }

    override fun showPreviousMonth() {
        val previousMonth = uiState.value.currentlyDisplayedMonth.minusMonths(1)
        observeMonthData(previousMonth)
    }

    override fun showNextMonth() {
        val nextMonth = uiState.value.currentlyDisplayedMonth.plusMonths(1)
        observeMonthData(nextMonth)
    }

    override fun navigateToDailyView(selectedDay: LocalDate, navController: NavController) {
        updateUiState {
            it.copy(
                currentlyDisplayedDay = selectedDay,
                currentlyDisplayedMonth = YearMonth.from(selectedDay)
            )
        }
        observeWeekData(selectedDay)
        navController.navigate(Routes.CALENDAR_DAY) {
            launchSingleTop = true
        }
    }

    override fun navigateToMonthlyView(navController: NavController) {
        observeMonthData(uiState.value.currentlyDisplayedMonth)
        navController.navigate(Routes.CALENDAR_MONTH) {
            launchSingleTop = true
        }
    }

    // Just in same week!
    override fun viewSelectedDay(selectedDay: LocalDate) {
        updateUiState {
            it.copy(
                currentlyDisplayedDay = selectedDay,
                currentlyDisplayedMonth = YearMonth.from(selectedDay)
            )
        }
    }

    private fun observeWeekData(selectedDay: LocalDate) {
        weekDataObservationJob?.cancel()

        weekDataObservationJob = viewModelScope.launch {
            setLoading(true)

            combine(
                model.getAppointmentsForMonth(YearMonth.from(selectedDay)),
                model.getTasksForMonth(YearMonth.from(selectedDay))
            ) {
                    appointmentsResult, tasksResult ->
                val appointments = when (appointmentsResult) {
                    is Result.Success -> appointmentsResult.data
                    is Result.Error -> {
                        handleDomainError(appointmentsResult.error)
                        emptyList()
                    }
                }
                val tasks = when (tasksResult) {
                    is Result.Success -> tasksResult.data
                    is Result.Error -> {
                        handleDomainError(tasksResult.error)
                        emptyList()
                    }
                }
                getDaysForWeek(selectedDay, appointments.sortedBy { it.startDate }, tasks)
            }
                .distinctUntilChanged()
                .catch {
                    showError(UNEXPECTED_ERROR)
                    updateUiState {
                        it.copy(
                            daysForDisplayedWeek = getDaysForWeek(selectedDay, emptyList(), emptyList())
                        )
                    }
                    if (isLoading.value) {
                        setLoading(false)
                    }
                }
                .collect { weekDays ->
                    updateUiState { it.copy(daysForDisplayedWeek = weekDays) }
                    if (isLoading.value) {
                        setLoading(false)
                    }
                }
        }
    }

    private fun getDaysForWeek(
        selectedDay: LocalDate,
        appointments: List<Appointment>,
        tasks: List<Task>
    ): List<CalendarDay> {
        val weekDays = mutableListOf<CalendarDay>()

        val mondayOfWeek = selectedDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sundayOfWeek = mondayOfWeek.plusDays(6)

        val appointmentSegments = mutableListOf<AppointmentDisplaySegment>()
        appointments.forEach { appointment ->
            appointmentSegments.addAll(getAppointmentDisplaySegments(appointment, mondayOfWeek, sundayOfWeek))
        }

        for (i in 0..6) {
            val dayDate = mondayOfWeek.plusDays(i.toLong())
            val appointmentSegmentsForDay = appointmentSegments.filter { appointmentSegment ->
                appointmentSegment.date == dayDate
            }.distinct()

            val tasksForDay = tasks.filter { task ->
                task.date?.isEqual(dayDate) ?: false
            }.distinct()

            weekDays.add(
                CalendarDay(
                    date = dayDate,
                    isCurrentMonth = YearMonth.from(dayDate) == YearMonth.from(selectedDay),
                    hasEntries = appointmentSegmentsForDay.isNotEmpty() || tasksForDay.isNotEmpty(),
                    appointmentSegments = appointmentSegmentsForDay,
                    tasks = tasksForDay,
                    isToday = dayDate.isEqual(LocalDate.now())
                )
            )
        }
        return weekDays
    }

    override fun showPreviousWeek() {
        val newSelectedDay = uiState.value.currentlyDisplayedDay.minusWeeks(1)
        updateUiState {
            it.copy(
                currentlyDisplayedDay = newSelectedDay,
                currentlyDisplayedMonth = YearMonth.from(newSelectedDay)
            )
        }
        observeWeekData(newSelectedDay)
    }

    override fun showNextWeek() {
        val newSelectedDay = uiState.value.currentlyDisplayedDay.plusWeeks(1)
        updateUiState {
            it.copy(
                currentlyDisplayedDay = newSelectedDay,
                currentlyDisplayedMonth = YearMonth.from(newSelectedDay)
            )
        }
        observeWeekData(newSelectedDay)
    }

    override fun getSelectedDayDetails(): CalendarDay {
        val selectedDay = uiState.value.currentlyDisplayedDay
        val weekDays = uiState.value.daysForDisplayedWeek

        return weekDays.first { it.date == selectedDay }
    }

    override fun changeTaskState(task: Task) {
        viewModelScope.launch {
            val result = task.taskId?.let { model.changeTaskState(it) }
            if (result is Result.Error) {
                handleDomainError(result.error)
            }
        }
    }

    override fun navigateToAppointmentCreation(navController: NavController) {
        navController.navigate(Routes.CREATE_APPOINTMENT)
    }

    override fun navigateToAppointment(appointment: Appointment, navController: NavController) {
        navController.navigate(Routes.getAppointmentRoute(appointment.appointmentId!!))
    }

    override fun navigateToTask(task: Task, navController: NavController) {
        navController.navigate(Routes.getTaskRoute(task.taskId!!))
    }

    override fun navigateToToDo(navController: NavController) {
        navController.navigate(Routes.TODO)
    }

    override fun navigateToWGProfile(navController: NavController) {
        navController.navigate(Routes.PROFILE_WG)
    }

    private fun getAppointmentDisplaySegments(
        appointment: Appointment,
        viewStartDate: LocalDate,
        viewEndDate: LocalDate
    ): List<AppointmentDisplaySegment> {
        val segments = mutableListOf<AppointmentDisplaySegment>()

        val appointmentActualStartDate = appointment.startDate.toLocalDate()

        // Check if the appointment ends exactly at midnight
        val appointmentActualEndDate = if (appointment.endDate.toLocalTime() == LocalTime.MIDNIGHT) {
            appointment.endDate.toLocalDate().minusDays(1)
        } else {
            appointment.endDate.toLocalDate()
        }

        // Determine start and end day for iteration
        val iterationStartDay = if (appointmentActualStartDate.isBefore(viewStartDate)) {
            viewStartDate
        } else {
            appointmentActualStartDate
        }
        val iterationEndDay = if (appointmentActualEndDate.isAfter(viewEndDate)) {
            viewEndDate
        } else {
            appointmentActualEndDate
        }

        var currentDay = iterationStartDay
        val appointmentStartDateTime = appointment.startDate
        val appointmentEndDateTime = appointment.endDate

        while (currentDay <= iterationEndDay) {
            val dayStartDateTime = currentDay.atStartOfDay()
            val dayEndDateTime = currentDay.plusDays(1).atStartOfDay()

            val segmentStartDateTime = if (appointmentStartDateTime.isAfter(dayStartDateTime)) {
                appointmentStartDateTime
            } else {
                dayStartDateTime
            }

            val segmentEndDateTime = if (appointmentEndDateTime.isBefore(dayEndDateTime)) {
                appointmentEndDateTime
            } else {
                dayEndDateTime
            }

            if (segmentStartDateTime.isBefore(segmentEndDateTime)) {
                segments.add(
                    AppointmentDisplaySegment(
                        originalAppointment = appointment,
                        segmentStartDate = segmentStartDateTime,
                        segmentEndDate = segmentEndDateTime,
                        date = currentDay,
                        startsOnThisDay = appointment.startDate.toLocalDate() == currentDay,
                        endsOnThisDay = (appointmentActualEndDate == currentDay)
                    )
                )
            }
            currentDay = currentDay.plusDays(1)
        }
        return segments
    }

    private fun processSegmentsForMultiDayUi(
        segmentsOfAppointment: List<AppointmentDisplaySegment>
    ): List<AppointmentDisplaySegment> {
        val processedSegments = mutableListOf<AppointmentDisplaySegment>()

        segmentsOfAppointment.forEach { segment ->
            val isMultiDayStart = segment.startsOnThisDay && !segment.endsOnThisDay
            val isMultiDayEnd = segment.endsOnThisDay && !segment.startsOnThisDay
            val isMultiDayMiddle = !segment.startsOnThisDay && !segment.endsOnThisDay

            processedSegments.add(
                segment.copy(
                    isMultiDayStart = isMultiDayStart,
                    isMultiDayEnd = isMultiDayEnd,
                    isMultiDayMiddle = isMultiDayMiddle
                )
            )
        }
        return processedSegments
    }

    private fun calculateAppointmentLanes(monthAppointmentSegments: List<AppointmentDisplaySegment>):
        List<AppointmentDisplaySegment> {
        if (monthAppointmentSegments.isEmpty()) return emptyList()

        val segmentsWithLanes = mutableListOf<AppointmentDisplaySegment>()

        val multiDaySegments = monthAppointmentSegments.filter { it.isPartOfMultiDayAppointment() }
        val singleDaySegments =
            monthAppointmentSegments.filter { !it.isPartOfMultiDayAppointment() }

        val multiDaySegmentsByOriginalAppointment =
            multiDaySegments.groupBy { it.originalAppointment.appointmentId }

        val sortedMultiDayAppointments = multiDaySegmentsByOriginalAppointment.keys
            .filterNotNull()
            .sortedWith(
                compareBy<String> { appointmentId ->
                    multiDaySegmentsByOriginalAppointment[appointmentId]!!.first().originalAppointment.startDate
                }.thenByDescending { appointmentId ->
                    val originalAppointment = multiDaySegmentsByOriginalAppointment[appointmentId]!!
                        .first().originalAppointment
                    ChronoUnit.DAYS.between(
                        originalAppointment.startDate.toLocalDate(),
                        originalAppointment.endDate.toLocalDate()
                    )
                }
            )
        val assignedLanesForOriginalAppointments = mutableMapOf<String, Int>()
        val laneOccupancyByDay = mutableMapOf<LocalDate, MutableMap<Int, String>>()

        // Iterate over all sorted multi-day appointments to assign lanes
        for (appointmentId in sortedMultiDayAppointments) {
            val originalAppointment =
                multiDaySegmentsByOriginalAppointment[appointmentId]?.first()?.originalAppointment ?: continue

            val effectiveStartDay = originalAppointment.startDate.toLocalDate()
            val effectiveEndDay = if (originalAppointment.endDate.toLocalTime() == LocalTime.MIDNIGHT) {
                originalAppointment.endDate.toLocalDate().minusDays(1)
            } else {
                originalAppointment.endDate.toLocalDate()
            }

            var assignedLane = assignedLanesForOriginalAppointments[appointmentId]

            if (assignedLane == null) {
                // Find the first free lane that is not occupied on any day of the appointment
                var potentialLane = 0
                while (true) {
                    var laneIsFree = true
                    var currentDayInTerm = effectiveStartDay
                    // Check all days in the appointment's range for lane occupancy
                    while (currentDayInTerm <= effectiveEndDay) {
                        val occupiedLaneId = laneOccupancyByDay[currentDayInTerm]?.get(potentialLane)
                        if (occupiedLaneId != null) {
                            laneIsFree = false
                            break
                        }
                        currentDayInTerm = currentDayInTerm.plusDays(1)
                    }

                    if (laneIsFree) {
                        assignedLane = potentialLane
                        break
                    }
                    potentialLane++
                }
                assignedLanesForOriginalAppointments[appointmentId] = assignedLane!!
            }

            // Mark the assigned lane as occupied for every day of the appointment
            var currentDayToBlock = effectiveStartDay
            while (currentDayToBlock <= effectiveEndDay) {
                laneOccupancyByDay.getOrPut(currentDayToBlock) { mutableMapOf() }[assignedLane] = appointmentId
                currentDayToBlock = currentDayToBlock.plusDays(1)
            }
        }

        multiDaySegments.forEach { segment ->
            val lane = assignedLanesForOriginalAppointments[segment.originalAppointment.appointmentId]
            segmentsWithLanes.add(segment.copy(laneIndex = lane))
        }
        segmentsWithLanes.addAll(singleDaySegments.map { it.copy(laneIndex = null) })

        return segmentsWithLanes.sortedWith(
            compareBy(
                { it.date },
                { it.laneIndex ?: Int.MAX_VALUE }
            )
        )
    }
}
