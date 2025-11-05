package com.wgeplant.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.wgeplant.R
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.Task
import com.wgeplant.ui.theme.WGeplantTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

object MonthlyCalendarScreenConstants {
    const val APP_LOGO = "WGeplant Logo"
    const val APP_TITLE_PART1 = "WG"
    const val APP_TITLE_PART2 = "eplant"
    const val CALENDAR = "Kalender"
    const val MONTH = "Monat"
    const val DAY = "Tag"
    const val ADD_APPOINTMENT = "Termin hinzufügen"
    const val TODO = "To-Do"
    const val WG_PROFILE_PICTURE = "WG Profilbild"
    val DAY_NAMES = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    const val CALENDAR_GRID_TEST_TAG = "calendar_grid"
}

/**
 * Displays the monthly calendar screen with navigation and data from the provided ViewModel.
 *
 * @param navController Controller for navigation actions.
 * @param calendarViewModel ViewModel providing calendar state and navigation event handlers.
 */
@Composable
fun MonthlyCalendarScreen(
    navController: NavController,
    calendarViewModel: ICalendarViewModel = hiltViewModel()
) {
    val uiState by calendarViewModel.uiState.collectAsState()
    val errorMessage by calendarViewModel.errorMessage.collectAsState()
    val isLoading by calendarViewModel.isLoading.collectAsState()

    MonthlyCalendarScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        onPreviousMonth = calendarViewModel::showPreviousMonth,
        onNextMonth = calendarViewModel::showNextMonth,
        onWGProfileClicked = calendarViewModel::navigateToWGProfile,
        onAddClicked = calendarViewModel::navigateToAppointmentCreation,
        onDailyViewClicked = calendarViewModel::navigateToDailyView,
        onDayCellClicked = calendarViewModel::navigateToDailyView,
        onToDoListClicked = calendarViewModel::navigateToToDo
    )
}

/**
 * Renders the content of the monthly calendar screen, including the top app bar,
 * calendar grid, loading indicator, error messages, and bottom navigation bar.
 *
 * @param uiState The current UI state of the calendar screen.
 * @param errorMessage Optional error message to be displayed.
 * @param isLoading Flag indicating if the data is currently loading.
 * @param navController Controller for navigation actions.
 * @param onWGProfileClicked Lambda to invoke when the WG profile icon is clicked.
 * @param onAddClicked Lambda to invoke when the add button is clicked.
 * @param onDailyViewClicked Lambda to invoke when switching to daily view for a specific day.
 * @param onPreviousMonth Lambda to invoke when navigating to the previous month.
 * @param onNextMonth Lambda to invoke when navigating to the next month.
 * @param onDayCellClicked Lambda to invoke when a specific day cell is clicked. Defaults to no-op.
 * @param onToDoListClicked Lambda to invoke when the To-Do list is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthlyCalendarScreenContent(
    uiState: CalendarUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    onWGProfileClicked: (navController: NavController) -> Unit,
    onAddClicked: (navController: NavController) -> Unit,
    onDailyViewClicked: (selectedDay: LocalDate, navController: NavController) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayCellClicked: (day: LocalDate, navController: NavController) -> Unit = { _, _ -> },
    onToDoListClicked: (navController: NavController) -> Unit
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
                                contentDescription = MonthlyCalendarScreenConstants.APP_LOGO,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                        append(MonthlyCalendarScreenConstants.APP_TITLE_PART1)
                                    }
                                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                        append(MonthlyCalendarScreenConstants.APP_TITLE_PART2)
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
            CalendarHeaderMonth(
                onAddClicked = { onAddClicked(navController) },
                onDailyViewClicked = { onDailyViewClicked(LocalDate.now(), navController) }
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            var dragAmount = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { dragAmount = 0f },
                                onHorizontalDrag = { change, drag ->
                                    dragAmount += drag
                                    change.consume()
                                },
                                onDragEnd = {
                                    val swipeThreshold = size.width / 4
                                    if (dragAmount > swipeThreshold) {
                                        onPreviousMonth()
                                    } else if (dragAmount < -swipeThreshold) {
                                        onNextMonth()
                                    }
                                }
                            )
                        }.testTag(MonthlyCalendarScreenConstants.CALENDAR_GRID_TEST_TAG)
                ) {
                    CalendarGrid(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        onDayCellClicked = onDayCellClicked,
                        uiState = uiState
                    )
                }
            }

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

/**
 * Shows the WG profile icon with an optional profile image.
 * Displays a placeholder color when no image URL is provided or loading fails.
 *
 * @param profileImageUrl URL string for the WG profile image. Nullable.
 * @param modifier Modifier to apply to the icon container.
 * @param iconSize Size of the profile icon.
 * @param placeholderColor Background color used as a placeholder during loading or error.
 */
@Composable
fun WGProfileIcon(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    placeholderColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .size(iconSize)
            .clip(CircleShape).testTag(MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE),
        contentAlignment = Alignment.Center
    ) {
        if (!profileImageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = MonthlyCalendarScreenConstants.WG_PROFILE_PICTURE,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(placeholderColor)
                        )
                    }

                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(placeholderColor)
                        )
                    }

                    is AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent()
                    }

                    AsyncImagePainter.State.Empty -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(placeholderColor)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderColor)
            )
        }
    }
}

/**
 * Displays the header section of the monthly calendar, including the title,
 * mode selection between month and day views, and an add button.
 *
 * @param onAddClicked Lambda to invoke when the add button is clicked.
 * @param onDailyViewClicked Lambda to invoke when switching to daily view.
 */
@Composable
fun CalendarHeaderMonth(
    onAddClicked: () -> Unit,
    onDailyViewClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = MonthlyCalendarScreenConstants.CALENDAR,
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
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = MonthlyCalendarScreenConstants.MONTH,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            topEnd = 15.dp,
                            bottomEnd = 15.dp
                        )
                    )
                    .clickable { onDailyViewClicked() }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = MonthlyCalendarScreenConstants.DAY,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 20.sp,
                        color = Color.Black
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
                Icon(Icons.Default.Add, contentDescription = MonthlyCalendarScreenConstants.ADD_APPOINTMENT)
            }

            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

/**
 * Renders the grid of calendar days for the currently displayed month.
 *
 * @param modifier Modifier to apply to the grid container.
 * @param navController Controller for navigation actions.
 * @param onDayCellClicked Lambda invoked when a day cell is clicked; passes the clicked date and navController.
 * @param uiState Current calendar UI state containing the days and month data.
 */
@Composable
fun CalendarGrid(
    modifier: Modifier = Modifier,
    navController: NavController,
    onDayCellClicked: (LocalDate, navController: NavController) -> Unit,
    uiState: CalendarUiState = CalendarUiState()
) {
    val currentMonth = uiState.currentlyDisplayedMonth.month
    val currentYear = uiState.currentlyDisplayedMonth.year

    val dayNames = MonthlyCalendarScreenConstants.DAY_NAMES

    Column(modifier = modifier) {
        Text(
            text = "${currentMonth.getDisplayName(TextStyle.FULL, Locale.GERMAN)} $currentYear",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp, start = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dayNames.forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.daysForCalendarGrid) { day ->
                DayCell(day = day, navController = navController, onDayCellClicked = onDayCellClicked)
            }
        }
    }
}

/**
 * Displays a single day cell within the calendar grid.
 * Shows the day number, highlights if today, and displays indicators for appointments and tasks.
 *
 * @param day The calendar day data to display.
 * @param navController Controller for navigation actions.
 * @param onDayCellClicked Lambda to invoke when the day cell is clicked; provides the date and navController.
 */
@Composable
fun DayCell(
    day: CalendarDay,
    navController: NavController,
    onDayCellClicked: (LocalDate, navController: NavController) -> Unit
) {
    val backgroundColor = if (day.isToday) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
    val textColor = if (day.isCurrentMonth) Color.Unspecified else Color.Gray
    val indicatorSize = 6.dp
    val indicatorPadding = 1.5.dp
    val barHeight = 4.dp
    val barVerticalSpacing = 2.dp

    Box(
        modifier = Modifier
            .aspectRatio(0.6f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = day.isCurrentMonth) { onDayCellClicked(day.date, navController) },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = textColor
                )
            )

            if (day.hasEntries) {
                val allDisplayableEntries = mutableListOf<Any>()
                allDisplayableEntries.addAll(day.appointmentSegments.sortedBy { it.laneIndex })
                allDisplayableEntries.addAll(day.tasks)

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 1.5.dp)
                ) {
                    val availableHeightForEntries = maxHeight
                    var currentYOffsetForIndicators = 0.dp
                    var barsCount = 0
                    var indicatorsCount = 0

                    val multiDaySegmentsForDisplay = day.appointmentSegments.filter { it.isPartOfMultiDayAppointment() }
                    val singleDayAppointmentSegmentsForDisplay = day.appointmentSegments.filter {
                        !it.isPartOfMultiDayAppointment()
                    }

                    val distinctMultiDayLanes = multiDaySegmentsForDisplay.mapNotNull { it.laneIndex }.distinct()
                        .sorted()

                    for (lane in distinctMultiDayLanes) {
                        val segmentToDisplayInLane = multiDaySegmentsForDisplay.firstOrNull { it.laneIndex == lane }

                        if (segmentToDisplayInLane != null) {
                            val barOccupiedHeight = barHeight + barVerticalSpacing
                            val calculatedYOffset = (lane.dp * barOccupiedHeight.value)

                            if (calculatedYOffset + barOccupiedHeight <= availableHeightForEntries) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barHeight)
                                        .offset(y = calculatedYOffset)
                                ) {
                                    AppointmentSegmentBar(
                                        segment = segmentToDisplayInLane,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                barsCount++
                                currentYOffsetForIndicators = maxOf(
                                    currentYOffsetForIndicators,
                                    calculatedYOffset + barOccupiedHeight
                                )
                            } else {
                                break
                            }
                        }
                    }

                    val combinedSingleDayEntries = singleDayAppointmentSegmentsForDisplay + day.tasks

                    if (combinedSingleDayEntries.isNotEmpty() &&
                        currentYOffsetForIndicators < availableHeightForEntries
                    ) {
                        val remainingHeightForIndicators = availableHeightForEntries - currentYOffsetForIndicators
                        val estimatedRows = (remainingHeightForIndicators / (indicatorSize + indicatorPadding)).toInt()
                        val maxIndicatorsPerCell = (
                            estimatedRows * (maxWidth / (indicatorSize + indicatorPadding)).toInt()
                            ).coerceAtLeast(
                            0
                        )

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = currentYOffsetForIndicators)
                                .heightIn(max = remainingHeightForIndicators),
                            horizontalArrangement = Arrangement
                                .spacedBy(indicatorPadding, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(indicatorPadding)
                        ) {
                            combinedSingleDayEntries.forEach { entry ->
                                if (indicatorsCount < maxIndicatorsPerCell) {
                                    when (entry) {
                                        is AppointmentDisplaySegment -> {
                                            IndicatorCircle(
                                                color = entry.originalAppointment.color,
                                                size = indicatorSize,
                                                isFilled = true
                                            )
                                        }

                                        is Task -> {
                                            IndicatorCircle(
                                                color = entry.color,
                                                size = indicatorSize,
                                                isFilled = false
                                            )
                                        }
                                    }
                                    indicatorsCount++
                                } else {
                                    return@FlowRow
                                }
                            }
                        }
                    }
                    val totalEntries = day.appointmentSegments.size + day.tasks.size
                    val displayedEntries = barsCount + indicatorsCount
                    val remainingEntries = totalEntries - displayedEntries

                    if (remainingEntries > 0) {
                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = "+$remainingEntries",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 9.sp,
                                color = textColor.copy(alpha = 0.7f)
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 1.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws a circular indicator dot, either filled or stroked.
 *
 * @param color Color of the indicator.
 * @param size Diameter of the indicator circle.
 * @param isFilled Whether the circle is filled or just outlined.
 * @param strokeWidth Stroke width used if not filled.
 */
@Composable
fun IndicatorCircle(color: Color, size: Dp, isFilled: Boolean, strokeWidth: Dp = 1.dp) {
    Canvas(modifier = Modifier.size(size)) {
        if (isFilled) {
            drawCircle(color = color)
        } else {
            drawCircle(color = color, style = Stroke(width = strokeWidth.toPx()))
        }
    }
}

/**
 * Draws a horizontal bar representing a segment of a multi-day appointment.
 * Rounded corners are applied at start and/or end depending on segment position.
 *
 * @param segment The appointment segment to display.
 * @param modifier Modifier to apply to the bar container.
 */
@Composable
fun AppointmentSegmentBar(
    segment: AppointmentDisplaySegment,
    modifier: Modifier = Modifier
) {
    val startCornerRadius = if (segment.isMultiDayStart) 4.dp else 0.dp
    val endCornerRadius = if (segment.isMultiDayEnd) 4.dp else 0.dp

    Box(
        modifier = modifier
            .background(
                color = segment.originalAppointment.color,
                shape = RoundedCornerShape(
                    topStart = startCornerRadius,
                    bottomStart = startCornerRadius,
                    topEnd = endCornerRadius,
                    bottomEnd = endCornerRadius
                )
            )
            .padding(horizontal = 2.dp)
    )
}

/**
 * Bottom navigation bar for the calendar screen with options to switch to calendar view and to-do list.
 *
 * @param onToDoListClicked Lambda to invoke when the To-Do tab is clicked.
 * @param modifier Modifier to apply to the bottom bar container.
 */
@Composable
fun BottomNavigationBarCalendar(onToDoListClicked: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = MonthlyCalendarScreenConstants.CALENDAR,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = MonthlyCalendarScreenConstants.CALENDAR,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.secondary
                )
                .clickable { onToDoListClicked() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = MonthlyCalendarScreenConstants.TODO,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = MonthlyCalendarScreenConstants.TODO,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Monthly Calendar Screen")
@Composable
fun MonthlyCalendarScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        MonthlyCalendarScreenContent(
            uiState = CalendarUiState(),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            onWGProfileClicked = {},
            onAddClicked = {},
            onDailyViewClicked = { _, _ -> },
            onPreviousMonth = {},
            onNextMonth = {},
            onDayCellClicked = { _, _ -> },
            onToDoListClicked = {}
        )
    }
}

@Preview(name = "Calendar Grid Preview", showBackground = true)
@Composable
fun CalendarGridPreview() {
    val sampleDays = createSampleCalendarDaysForPreview()
    val sampleMonth = YearMonth.of(2025, 7)

    WGeplantTheme {
        CalendarGrid(
            onDayCellClicked = { _, _ -> },
            modifier = Modifier.fillMaxWidth(),
            navController = rememberNavController(),
            uiState = CalendarUiState(currentlyDisplayedMonth = sampleMonth, daysForCalendarGrid = sampleDays)
        )
    }
}

private fun createSampleCalendarDaysForPreview(): List<CalendarDay> {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    val days = mutableListOf<CalendarDay>()

    val appointmentSegmentsSample = listOf(
        AppointmentDisplaySegment(
            originalAppointment = Appointment(
                appointmentId = "1",
                title = "Meeting",
                startDate = LocalDateTime.of(today.plusDays(1), LocalTime.of(15, 0)),
                endDate = LocalDateTime.of(today.plusDays(1), LocalTime.of(16, 0)),
                affectedUsers = emptyList(),
                color = Color(0xFFFFA500),
                description = ""
            ),
            segmentStartDate = LocalDateTime.of(today.plusDays(1), LocalTime.of(15, 0)),
            segmentEndDate = LocalDateTime.of(today.plusDays(1), LocalTime.of(16, 0)),
            date = today.plusDays(1),
            startsOnThisDay = true,
            endsOnThisDay = true
        ),
        AppointmentDisplaySegment(
            originalAppointment = Appointment(
                appointmentId = "2",
                title = "Lunch",
                startDate = LocalDateTime.of(today.plusDays(3), LocalTime.of(13, 0)),
                endDate = LocalDateTime.of(today.plusDays(3), LocalTime.of(14, 0)),
                affectedUsers = emptyList(),
                color = Color(0xFF00FF00),
                description = ""
            ),
            segmentStartDate = LocalDateTime.of(today.plusDays(3), LocalTime.of(13, 0)),
            segmentEndDate = LocalDateTime.of(today.plusDays(3), LocalTime.of(14, 0)),
            date = today.plusDays(3),
            startsOnThisDay = true,
            endsOnThisDay = true
        )
    )

    val tasksSample = listOf(
        Task(
            taskId = "t1",
            title = "Task 1",
            date = LocalDate.now().plusDays(1),
            color = Color(0xFFFF00FF),
            description = "",
            affectedUsers = emptyList(),
            stateOfTask = false
        ),
        Task(
            taskId = "t2",
            title = "Task 2",
            date = LocalDate.now().plusDays(2),
            color = Color(0xFF0000FF),
            description = "",
            affectedUsers = emptyList(),
            stateOfTask = false
        )
    )

    for (i in -3..10) {
        val date = today.plusDays(i.toLong())
        val appointmentsOnDay = appointmentSegmentsSample.filter { it.date.isEqual(date) }
        val tasksOnDay = tasksSample.filter { task ->
            val taskDate = task.date
            taskDate?.isEqual(date) ?: false
        }

        days.add(
            CalendarDay(
                date = date,
                isCurrentMonth = YearMonth.from(date) == currentMonth,
                isToday = date.isEqual(today),
                appointmentSegments = appointmentsOnDay,
                tasks = tasksOnDay,
                hasEntries = true
            )
        )
    }
    return days
}
