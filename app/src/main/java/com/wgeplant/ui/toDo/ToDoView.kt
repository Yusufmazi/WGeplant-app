package com.wgeplant.ui.toDo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wgeplant.R
import com.wgeplant.model.domain.Task
import com.wgeplant.ui.calendar.WGProfileIcon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val LOGO_SIZE_DP = 40
private const val TITLE_FONT_SIZE_SP = 16
private const val HEADER_FONT_SIZE_SP = 24
private const val TASK_SECTION_FONT_SIZE_SP = 18
private const val TASK_ICON_SIZE_DP = 20
private const val TASK_SPACING_DP = 12
private const val LIST_HORIZONTAL_PADDING_DP = 16
private const val TASK_VERTICAL_PADDING_DP = 6
private const val ERROR_FONT_SIZE_SP = 15
private const val FLOATING_BUTTON_SIZE_DP = 40
private const val TASK_TEXT_FONT_SIZE_SP = 16
private const val TO_DO_PADDING_DP = 16
private const val SECTION_SPACER_HEIGHT_DP = 16
private const val ERROR_TEXT_TOP_PADDING_DP = 12
private const val DATE_FORMAT_PATTERN = "dd.MM.yyyy"
private const val NO_TIME = "Kein Datum"
private const val TO_DO = "To-Do"
private const val WG = "WG"
private const val EPLANT = "eplant"
private const val CALENDER = "Kalender"
private const val LOGO = "WGeplant Logo"
private const val ADD_TASK = "Aufgabe hinzufügen"
private const val TEST_TAG_WG_PROFILE_ICON = "WG Profile Icon"

/**
 * Entry point for the To-Do screen.
 * Collects state and delegates UI rendering to [ToDoScreenContent].
 *
 */
@Composable
fun ToDoScreen(
    navController: NavController,
    toDoViewModel: IToDoViewModel = hiltViewModel()
) {
    val uiState by toDoViewModel.uiState.collectAsState()
    val errorMessage by toDoViewModel.errorMessage.collectAsState()
    val isLoading by toDoViewModel.isLoading.collectAsState()

    ToDoScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        onWGProfileClicked = toDoViewModel::navigateToWGProfile,
        onCalendarClicked = toDoViewModel::navigateToCalendar,
        onAddClicked = toDoViewModel::navigateToTaskCreation,
        onTaskStateChange = toDoViewModel::changeTaskState,
        onTaskClicked = toDoViewModel::navigateToTask
    )
}

fun groupTasksByDate(tasks: List<Task>): Map<LocalDate?, List<Task>> {
    return tasks.groupBy { it.date }
}

/**
 * Main UI content of the To-Do screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreenContent(
    uiState: ToDoUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    onWGProfileClicked: (NavController) -> Unit,
    onCalendarClicked: (NavController) -> Unit,
    onAddClicked: (NavController) -> Unit,
    onTaskStateChange: (Task) -> Unit,
    onTaskClicked: (Task, NavController) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.wgeplant_logo),
                                contentDescription = LOGO,
                                modifier = Modifier.size(LOGO_SIZE_DP.dp)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color =
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        append(WG)
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color =
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        append(EPLANT)
                                    }
                                },
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize =
                                    TITLE_FONT_SIZE_SP.sp
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
                            .testTag(TEST_TAG_WG_PROFILE_ICON) // for e2e tests
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBarToDo(
                onCalendarClicked = { onCalendarClicked(navController) },
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Bottom)
                )
            )
        }
    ) { innerPadding ->
        val groupedTasks = groupTasksByDate(uiState.tasks)
        val today = remember { LocalDate.now() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ToDoHeader(onAddClicked = { onAddClicked(navController) })

            Spacer(modifier = Modifier.height(SECTION_SPACER_HEIGHT_DP.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = LIST_HORIZONTAL_PADDING_DP.dp)
            ) {
                groupedTasks.toSortedMap(compareBy { it ?: LocalDate.MAX })
                    .forEach { (date, tasksForDate) ->
                        item {
                            val formattedDateText = date?.let {
                                val dayOfWeek = it.dayOfWeek.getDisplayName(
                                    TextStyle.SHORT_STANDALONE,
                                    Locale.GERMANY
                                )
                                val dateFormatted = it.format(
                                    DateTimeFormatter.ofPattern(
                                        DATE_FORMAT_PATTERN
                                    ).withLocale(Locale.GERMANY)
                                )
                                "$dayOfWeek, $dateFormatted"
                            } ?: NO_TIME

                            val isOverdue = tasksForDate.any { task ->
                                task.date != null &&
                                    task.date.isBefore(today)
                            }

                            Text(
                                text = formattedDateText,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = TASK_SECTION_FONT_SIZE_SP.sp
                                ),
                                color = if (isOverdue) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(vertical = TO_DO_PADDING_DP.dp)
                            )
                        }

                        items(tasksForDate) { task ->
                            ToDoItem(
                                task = task,
                                onTaskStateChange = { onTaskStateChange(task) },
                                onTaskClicked = { onTaskClicked(task, navController) }
                            )
                        }
                    }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = ERROR_FONT_SIZE_SP.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ERROR_TEXT_TOP_PADDING_DP.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Header section with title and add button.
 */
@Composable
fun ToDoHeader(onAddClicked: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = TO_DO,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = HEADER_FONT_SIZE_SP.sp,
                color = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier.padding(horizontal = TO_DO_PADDING_DP.dp)
        )

        FloatingActionButton(
            onClick = onAddClicked,
            modifier = Modifier.size(FLOATING_BUTTON_SIZE_DP.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = ADD_TASK)
        }

        Spacer(modifier = Modifier.width(TO_DO_PADDING_DP.dp))
    }
}

/**
 * Bottom navigation bar for switching between To-Do and Calendar.
 */
@Composable
fun BottomNavigationBarToDo(onCalendarClicked: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.secondary)
                .clickable { onCalendarClicked() }
                .padding(vertical = TO_DO_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = CALENDER, tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = CALENDER,
                    fontSize = ERROR_FONT_SIZE_SP.sp,
                    color = Color.Black
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = TO_DO_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = TO_DO,
                    tint = MaterialTheme
                        .colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = TO_DO,
                    fontSize = ERROR_FONT_SIZE_SP.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Individual task item shown in the list.
 */
@Composable
fun ToDoItem(
    task: Task,
    onTaskStateChange: (Task) -> Unit,
    onTaskClicked: (Task) -> Unit
) {
    val today = remember { LocalDate.now() }
    val isOverdue = task.date != null && task.date.isBefore(today)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TASK_VERTICAL_PADDING_DP.dp)
            .combinedClickable(
                onClick = { onTaskStateChange(task) },
                onLongClick = { onTaskClicked(task) }
            ),
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
            modifier = Modifier.size(TASK_ICON_SIZE_DP.dp)
        )

        Spacer(modifier = Modifier.width(TASK_SPACING_DP.dp))

        val textStyle = if (task.stateOfTask) {
            MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough)
        } else {
            MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
        }

        val textColor = if (isOverdue) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Text(
            text = task.title,
            style = textStyle,
            fontSize = TASK_TEXT_FONT_SIZE_SP.sp,
            color = textColor
        )
    }
}
