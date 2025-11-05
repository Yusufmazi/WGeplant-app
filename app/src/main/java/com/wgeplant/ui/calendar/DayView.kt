package com.wgeplant.ui.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wgeplant.R
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.Task
import com.wgeplant.ui.theme.WGeplantTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DailyCalendarScreenConstants {
    const val APP_LOGO = "WGeplant Logo"
    const val ADD_APPOINTMENT = "Termin hinzufügen"
    const val PREVIOUS_WEEK = "Vorherige Woche"
    const val NEXT_WEEK = "Nächste Woche"
    const val CALENDAR = "Kalender"
    const val MONTH = "Monat"
    const val DAY = "Tag"
    const val APPOINTMENTS = "Termine"
    const val NO_APPOINTMENTS_FOR_DAY = "Keine Termine für diesen Tag"
    const val TASKS = "Aufgaben"
    const val NO_TASKS_FOR_DAY = "Keine Aufgaben für diesen Tag"
    const val TIME_PATTERN = "HH:mm"
    const val APP_TITLE_PART1 = "WG"
    const val APP_TITLE_PART2 = "eplant"
    const val WEEK_TEST_TAG = "week"
}

/**
 * Displays the daily calendar screen with appointment and task lists.
 *
 * @param navController Navigation controller to handle navigation actions.
 * @param calendarViewModel ViewModel providing the UI state and event handlers.
 */
@Composable
fun DailyCalendarScreen(
    navController: NavController,
    calendarViewModel: ICalendarViewModel = hiltViewModel()
) {
    val uiState by calendarViewModel.uiState.collectAsState()
    val errorMessage by calendarViewModel.errorMessage.collectAsState()
    val isLoading by calendarViewModel.isLoading.collectAsState()

    DailyCalendarScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        onPreviousWeek = calendarViewModel::showPreviousWeek,
        onNextWeek = calendarViewModel::showNextWeek,
        onWGProfileClicked = calendarViewModel::navigateToWGProfile,
        onAddClicked = calendarViewModel::navigateToAppointmentCreation,
        onMonthlyViewClicked = calendarViewModel::navigateToMonthlyView,
        onToDoListClicked = calendarViewModel::navigateToToDo,
        onDayClicked = calendarViewModel::viewSelectedDay,
        onTaskStateChange = calendarViewModel::changeTaskState,
        onTaskClicked = calendarViewModel::navigateToTask,
        onAppointmentClicked = calendarViewModel::navigateToAppointment,
        getSelectedDayDetails = calendarViewModel::getSelectedDayDetails
    )
}

/**
 * Core UI content for the daily calendar screen including the app bar, bottom navigation,
 * calendar header, day selector, and lists of appointments and tasks.
 *
 * @param uiState Current calendar UI state containing displayed month, day, appointments, and tasks.
 * @param errorMessage Optional error message to show to the user.
 * @param isLoading Boolean flag indicating if data is currently loading.
 * @param navController Navigation controller used for screen navigation.
 * @param onWGProfileClicked Lambda invoked when the WG profile icon is clicked; receives [NavController].
 * @param onAddClicked Lambda invoked when the add button is clicked; receives [NavController].
 * @param onToDoListClicked Lambda invoked when the to-do list button is clicked; receives [NavController].
 * @param onNextWeek Lambda invoked to navigate to the next week; takes no parameters.
 * @param onPreviousWeek Lambda invoked to navigate to the previous week; takes no parameters.
 * @param onMonthlyViewClicked Lambda invoked when the monthly view button is clicked; receives [NavController].
 * @param onDayClicked Lambda invoked when a day is selected; receives the selected [LocalDate].
 * @param onTaskStateChange Lambda invoked when a task’s state changes; receives the affected [Task].
 * @param onTaskClicked Lambda invoked when a task is long-clicked; receives the [Task] and [NavController].
 * @param onAppointmentClicked Lambda invoked when an appointment is long-clicked; receives the [Appointment] and [NavController].
 * @param getSelectedDayDetails Lambda returning the [CalendarDay] details for the currently selected day.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyCalendarScreenContent(
    uiState: CalendarUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    onWGProfileClicked: (navController: NavController) -> Unit,
    onAddClicked: (navController: NavController) -> Unit,
    onToDoListClicked: (navController: NavController) -> Unit,
    onNextWeek: () -> Unit,
    onPreviousWeek: () -> Unit,
    onMonthlyViewClicked: (navController: NavController) -> Unit,
    onDayClicked: (day: LocalDate) -> Unit,
    onTaskStateChange: (task: Task) -> Unit,
    onTaskClicked: (task: Task, navController: NavController) -> Unit,
    onAppointmentClicked: (appointment: Appointment, navController: NavController) -> Unit,
    getSelectedDayDetails: () -> CalendarDay
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.wgeplant_logo),
                                contentDescription = DailyCalendarScreenConstants.APP_LOGO,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                        append(DailyCalendarScreenConstants.APP_TITLE_PART1)
                                    }
                                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                        append(DailyCalendarScreenConstants.APP_TITLE_PART2)
                                    }
                                },
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    WGProfileIcon(
                        profileImageUrl = uiState.wgProfileImageUrl,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onWGProfileClicked(navController) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BottomNavigationBarCalendar(
                onToDoListClicked = { onToDoListClicked(navController) },
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            CalendarHeaderDay(
                onAddClicked = { onAddClicked(navController) },
                onMonthlyViewClicked = { onMonthlyViewClicked(navController) }
            )

            val currentMonth = uiState.currentlyDisplayedMonth.month
            val currentYear = uiState.currentlyDisplayedMonth.year

            Text(
                text = "${currentMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())} $currentYear",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp, start = 16.dp)
            )

            DayOfWeekSelector(
                selectedDay = uiState.currentlyDisplayedDay,
                onDaySelected = { newDate ->
                    onDayClicked(newDate)
                },
                uiState = uiState,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                val selectedDay = getSelectedDayDetails()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = DailyCalendarScreenConstants.APPOINTMENTS,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (selectedDay.appointmentSegments.isEmpty()) {
                            Text(
                                DailyCalendarScreenConstants.NO_APPOINTMENTS_FOR_DAY,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        } else {
                            selectedDay.appointmentSegments.forEach { appointmentSegment ->
                                AppointmentItem(
                                    appointmentSegment = appointmentSegment,
                                    onLongClick = {
                                        onAppointmentClicked(
                                            appointmentSegment.originalAppointment,
                                            navController
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = DailyCalendarScreenConstants.TASKS,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (selectedDay.tasks.isEmpty()) {
                            Text(
                                DailyCalendarScreenConstants.NO_TASKS_FOR_DAY,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        } else {
                            selectedDay.tasks.forEach { task ->
                                TaskItem(
                                    task = task,
                                    onTaskStateChange = { onTaskStateChange(task) },
                                    onLongClick = { onTaskClicked(task, navController) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CalendarHeaderDay(
    onAddClicked: () -> Unit,
    onMonthlyViewClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = DailyCalendarScreenConstants.CALENDAR,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(vertical = 4.dp)
                    .clickable { onMonthlyViewClicked() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = DailyCalendarScreenConstants.MONTH,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            topEnd = 15.dp,
                            bottomEnd = 15.dp
                        )
                    )
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = DailyCalendarScreenConstants.DAY,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            FloatingActionButton(
                onClick = onAddClicked,
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = DailyCalendarScreenConstants.ADD_APPOINTMENT)
            }

            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDay: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    uiState: CalendarUiState = CalendarUiState(),
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val swipeModifier = Modifier
        .pointerInput(Unit) {
            var dragAmount = 0f
            detectHorizontalDragGestures(
                onDragStart = { dragAmount = 0f },
                onHorizontalDrag = { change, drag ->
                    dragAmount += drag
                    change.consume()
                },
                onDragEnd = {
                    val swipeThreshold = size.width / 10f
                    if (dragAmount > swipeThreshold) {
                        onPreviousWeek()
                    } else if (dragAmount < -swipeThreshold) {
                        onNextWeek()
                    }
                }
            )
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .then(swipeModifier).testTag(DailyCalendarScreenConstants.WEEK_TEST_TAG),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = DailyCalendarScreenConstants.PREVIOUS_WEEK,
            modifier = Modifier
                .size(20.dp)
                .clickable { onPreviousWeek() }
        )
        val daysInWeek = uiState.daysForDisplayedWeek

        daysInWeek.forEach { day ->
            val isSelected = day.date == selectedDay
            DayOfWeekBox(
                day = day.date,
                isSelected = isSelected,
                modifier = Modifier.weight(1f),
                onClick = { onDaySelected(day.date) }
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = DailyCalendarScreenConstants.NEXT_WEEK,
            modifier = Modifier
                .size(20.dp)
                .clickable { onNextWeek() }
        )
    }
}

@Composable
fun DayOfWeekBox(
    day: LocalDate,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier
) {
    val boxColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    val borderColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .padding(4.dp)
            .defaultMinSize(minWidth = 40.dp, minHeight = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(boxColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick(day) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.GERMAN),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 15.sp,
                lineHeight = 18.sp
            ),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = day.dayOfMonth.toString() + "." + day.monthValue.toString() + ".",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 17.sp
            ),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun AppointmentItem(
    appointmentSegment: AppointmentDisplaySegment,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(appointmentSegment.originalAppointment.color)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "${appointmentSegment.segmentStartDate.toLocalTime().format(
                DateTimeFormatter.ofPattern(DailyCalendarScreenConstants.TIME_PATTERN)
            )}-${appointmentSegment.segmentEndDate.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = appointmentSegment.originalAppointment.title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskStateChange: (task: Task) -> Unit,
    onLongClick: (task: Task) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onTaskStateChange(task) },
                onLongClick = { onLongClick(task) }
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, tint) = if (task.stateOfTask) {
            Icons.Default.CheckCircle to task.color
        } else {
            Icons.Default.RadioButtonUnchecked to task.color
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(8.dp))

        val textStyle = if (task.stateOfTask) {
            MaterialTheme.typography.bodySmall.copy(
                textDecoration = TextDecoration.LineThrough
            )
        } else {
            MaterialTheme.typography.bodySmall
        }

        Text(
            text = task.title,
            style = textStyle,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Daily Calendar")
@Composable
fun DailyCalendarScreenPreview() {
    val sampleWeek = createSampleWeekForPreview()
    val sampleMonth = YearMonth.of(2025, 7)

    WGeplantTheme {
        val previewNavController = rememberNavController()

        DailyCalendarScreenContent(
            uiState = CalendarUiState(
                currentlyDisplayedMonth = sampleMonth,
                daysForDisplayedWeek = sampleWeek,
                currentlyDisplayedDay = LocalDate.of(2025, 7, 14)
            ),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            onWGProfileClicked = {},
            onAddClicked = {},
            onToDoListClicked = {},
            onNextWeek = {},
            onPreviousWeek = {},
            onMonthlyViewClicked = {},
            onDayClicked = {},
            onTaskStateChange = {},
            onTaskClicked = { _, _ -> },
            onAppointmentClicked = { _, _ -> },
            getSelectedDayDetails = { createSelectedDayForPreview() }
        )
    }
}

private fun createSampleWeekForPreview(): List<CalendarDay> {
    val selectedDay = LocalDate.of(2025, 7, 14)
    val days = mutableListOf<CalendarDay>()

    for (i in 0..6) {
        val date = selectedDay.plusDays(i.toLong())
        days.add(
            CalendarDay(
                date = date,
                isCurrentMonth = true,
                appointmentSegments = emptyList(),
                tasks = emptyList(),
                isToday = date.isEqual(LocalDate.now()),
                hasEntries = false
            )
        )
    }
    return days
}

private fun createSelectedDayForPreview(): CalendarDay {
    val selectedDay = LocalDate.of(2025, 7, 14)

    val sampleAppointmentSegments = listOf(
        AppointmentDisplaySegment(
            originalAppointment = Appointment(
                appointmentId = "1",
                title = "Team Meeting",
                startDate = LocalDateTime.of(selectedDay, LocalTime.of(9, 0)),
                endDate = LocalDateTime.of(selectedDay, LocalTime.of(10, 0)),
                color = Color.Blue,
                affectedUsers = emptyList(),
                description = ""
            ),
            segmentStartDate = LocalDateTime.of(selectedDay, LocalTime.of(9, 0)),
            segmentEndDate = LocalDateTime.of(selectedDay, LocalTime.of(10, 0)),
            date = selectedDay,
            startsOnThisDay = true,
            endsOnThisDay = true
        ),
        AppointmentDisplaySegment(
            originalAppointment = Appointment(
                appointmentId = "2",
                title = "Lunch mit Alex",
                startDate = LocalDateTime.of(selectedDay, LocalTime.of(11, 0)),
                endDate = LocalDateTime.of(selectedDay, LocalTime.of(13, 0)),
                color = Color.Green,
                affectedUsers = emptyList(),
                description = ""
            ),
            segmentStartDate = LocalDateTime.of(selectedDay, LocalTime.of(11, 0)),
            segmentEndDate = LocalDateTime.of(selectedDay, LocalTime.of(13, 0)),
            date = selectedDay,
            startsOnThisDay = true,
            endsOnThisDay = true
        )
    )

    val sampleTasks = listOf(
        Task(
            taskId = "t1",
            title = "Einkaufen gehen",
            date = selectedDay,
            stateOfTask = false,
            color = Color.Magenta,
            affectedUsers = emptyList(),
            description = ""
        ),
        Task(
            taskId = "t2",
            title = "Fitnessstudio",
            date = selectedDay,
            stateOfTask = true,
            color = Color.Red,
            affectedUsers = emptyList(),
            description = ""
        )
    )

    return CalendarDay(
        date = selectedDay,
        isCurrentMonth = true,
        appointmentSegments = sampleAppointmentSegments,
        tasks = sampleTasks,
        isToday = selectedDay.isEqual(LocalDate.now()),
        hasEntries = true
    )
}
