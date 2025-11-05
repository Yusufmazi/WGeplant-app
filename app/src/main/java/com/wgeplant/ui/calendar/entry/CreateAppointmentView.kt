package com.wgeplant.ui.calendar.entry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wgeplant.ui.theme.EventColors
import com.wgeplant.ui.theme.WGeplantTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

object CreateAppointmentScreenConstants {
    const val APPOINTMENT = "Termin"
    const val TASK = "Aufgabe"
    const val BACK = "Zurück"
    const val SAVE = "Speichern"
    const val TITLE = "Titel"
    const val START_TIME = "Startzeit"
    const val END_TIME = "Endzeit"
    const val DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm"
    const val SELECT_COLOR = "Farbe auswählen"
    const val DESCRIPTION = "Beschreibung"
    const val DESCRIPTION_OPTIONAL = "Beschreibung (optional)"
    const val DATE = "Datum"
    const val PARTICIPANTS = "Teilnehmer"
    const val COLOR = "Farbe"
    const val SELECTED = "Ausgewählt"
    const val MAX_TITLE_LENGTH = 50
    const val MAX_DESCRIPTION_LENGTH = 250
    const val TITLE_FIELD_TEST_TAG = "title_field"
    const val DESCRIPTION_FIELD_TEST_TAG = "description_field"
    const val DROP_DOWN_MENU_TEST_TAG = "dropdown_menu"
}

/**
 * Screen composable to create a new appointment.
 *
 * @param navController Controller for navigation between screens.
 * @param appointmentViewModel ViewModel managing the UI state and business logic.
 */
@Composable
fun CreateAppointmentScreen(
    navController: NavController,
    appointmentViewModel: IAppointmentViewModel = hiltViewModel()
) {
    val uiState by appointmentViewModel.uiState.collectAsState()
    val errorMessage by appointmentViewModel.errorMessage.collectAsState()

    CreateAppointmentScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        navController = navController,
        goBack = appointmentViewModel::navigateBack,
        onTitleChanged = appointmentViewModel::onTitleChanged,
        onStartDateChanged = appointmentViewModel::onStartDateChanged,
        onEndDateChanged = appointmentViewModel::onEndDateChanged,
        onAssignmentChanged = appointmentViewModel::onAssignmentChanged,
        onColorChanged = appointmentViewModel::onColorChanged,
        onDescriptionChanged = appointmentViewModel::onDescriptionChanged,
        saveEntry = appointmentViewModel::saveEntry,
        navigateToOtherEntryCreation = appointmentViewModel::navigateToOtherEntryCreation
    )
}

/**
 * Content composable for the appointment creation screen.
 *
 * @param uiState The current UI state holding appointment data.
 * @param errorMessage An optional error message to display.
 * @param navController Controller for navigation.
 * @param goBack Lambda to navigate back.
 * @param onTitleChanged Lambda called when title changes.
 * @param onStartDateChanged Lambda called when start date/time changes.
 * @param onEndDateChanged Lambda called when end date/time changes.
 * @param onAssignmentChanged Lambda called when assignment selection changes.
 * @param onColorChanged Lambda called when the color selection changes.
 * @param onDescriptionChanged Lambda called when the description changes.
 * @param saveEntry Lambda to save the appointment entry.
 * @param navigateToOtherEntryCreation Lambda to navigate to other entry creation screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAppointmentScreenContent(
    uiState: AppointmentUiState,
    errorMessage: String?,
    navController: NavController,
    goBack: (navController: NavController) -> Unit,
    onTitleChanged: (String) -> Unit,
    onStartDateChanged: (LocalDateTime) -> Unit,
    onEndDateChanged: (LocalDateTime) -> Unit,
    onAssignmentChanged: (String, Boolean) -> Unit,
    onColorChanged: (Color) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    saveEntry: (navController: NavController) -> Unit,
    navigateToOtherEntryCreation: (navController: NavController) -> Unit
) {
    val context = LocalContext.current

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    var tempSelectedDateForStartTime by remember { mutableStateOf<LocalDate?>(null) }
    var tempSelectedDateForEndTime by remember { mutableStateOf<LocalDate?>(null) }

    if (showStartDatePicker.value) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val startDatePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                tempSelectedDateForStartTime = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                showStartDatePicker.value = false
            },
            year,
            month,
            day
        )
        startDatePickerDialog.setOnDismissListener {
            showStartDatePicker.value = false
        }

        startDatePickerDialog.show()
    }

    if (showEndDatePicker.value) {
        val year: Int
        val month: Int
        val day: Int

        if (uiState.startDate != null) {
            year = uiState.startDate.year
            month = uiState.startDate.monthValue - 1
            day = uiState.startDate.dayOfMonth
        } else {
            val calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
        }

        val endDatePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                tempSelectedDateForEndTime = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                showEndDatePicker.value = false
            },
            year,
            month,
            day
        )

        endDatePickerDialog.setOnDismissListener {
            showEndDatePicker.value = false
        }

        endDatePickerDialog.show()
    }

    if (tempSelectedDateForStartTime != null) {
        val calendar = Calendar.getInstance()

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val finalStartDateTime = LocalDateTime.of(
                    tempSelectedDateForStartTime!!,
                    LocalTime.of(selectedHour, selectedMinute)
                )
                onStartDateChanged(finalStartDateTime)
                tempSelectedDateForStartTime = null
            },
            hour,
            minute,
            true
        ).show()
    }

    if (tempSelectedDateForEndTime != null) {
        val calendar = Calendar.getInstance()

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val finalEndDateTime = LocalDateTime.of(
                    tempSelectedDateForEndTime!!,
                    LocalTime.of(selectedHour, selectedMinute)
                )
                onEndDateChanged(finalEndDateTime)
                tempSelectedDateForEndTime = null
            },
            hour,
            minute,
            true
        ).show()
    }

    val showColorPickerBottomSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showColorPickerBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                showColorPickerBottomSheet.value = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = CreateAppointmentScreenConstants.SELECT_COLOR,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventColors.allEventColors.forEach { color ->
                        ColorCircle(
                            color = color,
                            isSelected = uiState.color == color,
                            onClick = {
                                onColorChanged(color)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showColorPickerBottomSheet.value = false
                                    }
                                }
                            },
                            modifier = Modifier.padding(4.dp).testTag(EventColors.getEventColorName(color))
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    val expanded = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    ExposedDropdownMenuBox(
                        expanded = expanded.value,
                        onExpandedChange = { expanded.value = !expanded.value },
                        modifier = Modifier.fillMaxWidth().testTag(
                            CreateAppointmentScreenConstants.DROP_DOWN_MENU_TEST_TAG
                        )
                    ) {
                        OutlinedTextField(
                            value = CreateAppointmentScreenConstants.APPOINTMENT,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                focusedTrailingIconColor = MaterialTheme.colorScheme.tertiary
                            )

                        )
                        ExposedDropdownMenu(
                            expanded = expanded.value,
                            onDismissRequest = { expanded.value = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        CreateAppointmentScreenConstants.APPOINTMENT,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                },
                                onClick = { expanded.value = false }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        CreateAppointmentScreenConstants.TASK,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                },
                                onClick = {
                                    expanded.value = false
                                    navigateToOtherEntryCreation(navController)
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { goBack(navController) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = CreateAppointmentScreenConstants.BACK,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { saveEntry(navController) }
                    ) {
                        Text(
                            text = CreateAppointmentScreenConstants.SAVE,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 20.sp,
                                lineHeight = 29.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.size(8.dp))

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { newValue ->
                        if (newValue.length <= CreateAppointmentScreenConstants.MAX_TITLE_LENGTH) {
                            onTitleChanged(
                                newValue
                            )
                        }
                    },
                    label = {
                        Text(
                            text = CreateAppointmentScreenConstants.TITLE,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag(CreateAppointmentScreenConstants.TITLE_FIELD_TEST_TAG),
                    isError = uiState.titleError != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiState.titleError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                            Text(
                                text = "${uiState.title.length} / ${CreateAppointmentScreenConstants.MAX_TITLE_LENGTH}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.title.length
                                        == CreateAppointmentScreenConstants.MAX_TITLE_LENGTH
                                    ) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontSize = 12.sp
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            IconRow(
                icon = { Icon(Icons.Default.DateRange, contentDescription = CreateAppointmentScreenConstants.DATE) },
                modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker.value = true }
            ) {
                val startDateText = if (uiState.startDate != null) {
                    uiState.startDate.format(
                        DateTimeFormatter.ofPattern(CreateAppointmentScreenConstants.DATE_TIME_FORMAT)
                    )
                } else {
                    CreateAppointmentScreenConstants.START_TIME
                }

                Column(modifier = Modifier.clickable { showStartDatePicker.value = true }) {
                    Text(
                        text = startDateText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    if (uiState.startDateError != null) {
                        Text(
                            text = uiState.startDateError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))

            IconRow(
                icon = { Icon(Icons.Default.DateRange, contentDescription = CreateAppointmentScreenConstants.DATE) },
                modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker.value = true }
            ) {
                val endDateText = if (uiState.endDate != null) {
                    uiState.endDate.format(
                        DateTimeFormatter.ofPattern(CreateAppointmentScreenConstants.DATE_TIME_FORMAT)
                    )
                } else {
                    CreateAppointmentScreenConstants.END_TIME
                }

                Column(modifier = Modifier.clickable { showEndDatePicker.value = true }) {
                    Text(
                        text = endDateText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    if (uiState.endDateError != null) {
                        Text(
                            text = uiState.endDateError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))

            IconRow(
                icon = {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = CreateAppointmentScreenConstants.PARTICIPANTS
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.wgMembers) { member ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = member.isSelected,
                                onCheckedChange = { isChecked ->
                                    onAssignmentChanged(member.id, isChecked)
                                },
                                modifier = Modifier.testTag(member.name)
                            )
                            Text(
                                member.name,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            if (uiState.affectedUsersError != null) {
                Text(
                    text = uiState.affectedUsersError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp, end = 16.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }

            IconRow(
                icon = {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = CreateAppointmentScreenConstants.COLOR,
                        tint = uiState.color,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth().clickable { showColorPickerBottomSheet.value = true }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showColorPickerBottomSheet.value = true }
                ) {
                    Text(
                        text = EventColors.getEventColorName(uiState.color),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))

            IconRow(
                icon = {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = CreateAppointmentScreenConstants.DESCRIPTION
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { newValue ->
                            if (newValue.length <= CreateAppointmentScreenConstants.MAX_DESCRIPTION_LENGTH) {
                                onDescriptionChanged(
                                    newValue
                                )
                            }
                        },
                        label = {
                            Text(
                                CreateAppointmentScreenConstants.DESCRIPTION_OPTIONAL,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp).testTag(CreateAppointmentScreenConstants.DESCRIPTION_FIELD_TEST_TAG),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        supportingText = {
                            Text(
                                text = "${uiState.description.length} / " +
                                    "${CreateAppointmentScreenConstants.MAX_DESCRIPTION_LENGTH}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.description.length
                                        == CreateAppointmentScreenConstants.MAX_DESCRIPTION_LENGTH
                                    ) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontSize = 12.sp
                                )
                            )
                        }
                    )
                }
            }
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Circle button showing a color and whether it is selected.
 *
 * @param color Color to display.
 * @param isSelected Whether this color is currently selected.
 * @param onClick Callback invoked when the circle is clicked.
 * @param modifier Modifier to apply to the circle.
 */
@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = CreateAppointmentScreenConstants.SELECTED,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * A small composable that arranges an icon and content horizontally with padding.
 *
 * @param icon Composable representing the icon.
 * @param modifier Modifier to be applied to the Row.
 * @param content Content composable displayed next to the icon.
 */
@Composable
fun IconRow(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp)) {
            icon()
        }
        Spacer(Modifier.width(16.dp))
        content()
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Erstellung Termin")
@Composable
fun PreviewCreateAppointmentScreen() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        CreateAppointmentScreenContent(
            uiState = AppointmentUiState(
                title = "Einkauf planen",
                startDate = LocalDateTime.now().plusHours(1),
                endDate = LocalDateTime.now().plusHours(2),
                wgMembers = listOf(
                    WGMemberSelection(id = "1", name = "Max", isSelected = true),
                    WGMemberSelection(id = "2", name = "Lena", isSelected = false),
                    WGMemberSelection(id = "3", name = "Tobi", isSelected = false),
                    WGMemberSelection(id = "4", name = "Julia", isSelected = true),
                    WGMemberSelection(id = "3", name = "Moritz", isSelected = false),
                    WGMemberSelection(id = "4", name = "Isabelle", isSelected = false),
                    WGMemberSelection(id = "5", name = "Lisa", isSelected = false),
                    WGMemberSelection(id = "6", name = "Alexander", isSelected = false)
                ),
                color = Color(0xFF2196F3),
                description = "Obst, Gemüse, Milch und Brot kaufen gehen.",
                isValid = true
            ),
            errorMessage = null,
            navController = previewNavController,
            goBack = {},
            onTitleChanged = {},
            onStartDateChanged = {},
            onEndDateChanged = {},
            onAssignmentChanged = { _, _ -> },
            onColorChanged = {},
            onDescriptionChanged = {},
            saveEntry = {},
            navigateToOtherEntryCreation = {}
        )
    }
}
