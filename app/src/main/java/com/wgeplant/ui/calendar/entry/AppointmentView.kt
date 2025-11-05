package com.wgeplant.ui.calendar.entry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.widget.DatePicker
import android.widget.TimePicker
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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

object AppointmentScreenConstants {
    const val TITLE = "Titel"
    const val MAX_TITLE_LENGTH = 50
    const val START_TIME = "Startzeit"
    const val END_TIME = "Endzeit"
    const val DATE_PATTERN = "dd.MM.yyyy, HH:mm"
    const val DATE = "Datum"
    const val DELETE_APPOINTMENT = "Termin löschen?"
    const val CANNOT_UNDO = "Diese Aktion kann nicht rückgängig gemacht werden!"
    const val DELETE = "Löschen"
    const val CANCEL = "Abbrechen"
    const val SAVE = "Speichern"
    const val EDIT = "Bearbeiten"
    const val BACK = "Zurück"
    const val SELECT_COLOR = "Farbe auswählen"
    const val COLOR = "Farbe"
    const val PARTICIPANTS = "Teilnehmer"
    const val DESCRIPTION = "Beschreibung (optional)"
    const val MAX_DESCRIPTION_LENGTH = 250
    const val TITLE_FIELD_TEST_TAG = "title_field"
    const val DESCRIPTION_FIELD_TEST_TAG = "description_field"
}

/**
 * Composable screen displaying appointment details and allowing user interaction.
 *
 * This screen shows appointment information, allows editing, date picking,
 * color selection, and handles save, undo, and delete operations.
 *
 * @param navController Navigation controller to handle navigation actions.
 * @param appointmentViewModel ViewModel providing UI state and actions for the appointment.
 */
@Composable
fun AppointmentScreen(
    navController: NavController,
    appointmentViewModel: IAppointmentViewModel = hiltViewModel()
) {
    val uiState by appointmentViewModel.uiState.collectAsState()
    val errorMessage by appointmentViewModel.errorMessage.collectAsState()
    val isLoading by appointmentViewModel.isLoading.collectAsState()

    AppointmentScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        goBack = appointmentViewModel::navigateBack,
        onTitleChanged = appointmentViewModel::onTitleChanged,
        onStartDateChanged = appointmentViewModel::onStartDateChanged,
        onEndDateChanged = appointmentViewModel::onEndDateChanged,
        onAssignmentChanged = appointmentViewModel::onAssignmentChanged,
        onColorChanged = appointmentViewModel::onColorChanged,
        onDescriptionChanged = appointmentViewModel::onDescriptionChanged,
        saveEntry = appointmentViewModel::saveEntry,
        undoEdits = appointmentViewModel::undoEdits,
        deleteEntry = appointmentViewModel::delete,
        setEditMode = appointmentViewModel::setEditMode
    )
}

/**
 * Private composable function that renders the content of the appointment screen.
 *
 * Displays the appointment form including title, start and end dates, participants selection,
 * color picker, description, error messages, loading states, and handles user interactions
 * such as editing, saving, deleting, and undoing changes.
 *
 * The screen uses dialogs for date and time picking, a modal bottom sheet for color selection,
 * and confirmation dialogs for deleting appointments.
 *
 * @param uiState The current UI state representing the appointment details and validation errors.
 * @param errorMessage An optional error message to display at the bottom of the screen.
 * @param isLoading Boolean flag indicating whether a save or delete operation is in progress.
 * @param navController NavController used for navigation actions.
 * @param goBack Lambda function to navigate back to the previous screen.
 * @param onTitleChanged Lambda called when the appointment title changes.
 * @param onStartDateChanged Lambda called when the appointment start date/time changes.
 * @param onEndDateChanged Lambda called when the appointment end date/time changes.
 * @param onAssignmentChanged Lambda called when a participant's assignment state changes.
 * @param onColorChanged Lambda called when the appointment color changes.
 * @param onDescriptionChanged Lambda called when the appointment description changes.
 * @param saveEntry Lambda function to save the appointment, triggered on save action.
 * @param undoEdits Lambda function to revert any unsaved changes.
 * @param deleteEntry Lambda function to delete the appointment.
 * @param setEditMode Lambda function to toggle the edit mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentScreenContent(
    uiState: AppointmentUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    goBack: (navController: NavController) -> Unit,
    onTitleChanged: (String) -> Unit,
    onStartDateChanged: (LocalDateTime) -> Unit,
    onEndDateChanged: (LocalDateTime) -> Unit,
    onAssignmentChanged: (String, Boolean) -> Unit,
    onColorChanged: (Color) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    saveEntry: (navController: NavController) -> Unit,
    undoEdits: () -> Unit,
    deleteEntry: (navController: NavController) -> Unit,
    setEditMode: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isEditing = uiState.isEditing
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    var tempSelectedDateForStartTime by remember { mutableStateOf<LocalDate?>(null) }
    var tempSelectedDateForEndTime by remember { mutableStateOf<LocalDate?>(null) }

    val showColorPickerBottomSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    text = AppointmentScreenConstants.SELECT_COLOR,
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
                            modifier = Modifier
                                .padding(4.dp)
                                .testTag(EventColors.getEventColorName(color))
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(AppointmentScreenConstants.DELETE_APPOINTMENT) },
            text = { Text(AppointmentScreenConstants.CANNOT_UNDO, style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                Button(
                    onClick = {
                        deleteEntry(navController)
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text(AppointmentScreenConstants.DELETE)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmationDialog = false }
                ) {
                    Text(AppointmentScreenConstants.CANCEL)
                }
            }
        )
    }

    if (!uiState.isExisting) {
        goBack(navController)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    if (isEditing) {
                        TextButton(
                            onClick = {
                                undoEdits()
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = AppointmentScreenConstants.CANCEL,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 20.sp,
                                    lineHeight = 29.sp
                                )
                            )
                        }
                    } else {
                        IconButton(onClick = { goBack(navController) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = AppointmentScreenConstants.BACK,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(
                            onClick = {
                                saveEntry(navController)
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = AppointmentScreenConstants.SAVE,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 20.sp,
                                    lineHeight = 29.sp
                                )
                            )
                        }
                    } else {
                        IconButton(onClick = { setEditMode() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = AppointmentScreenConstants.EDIT,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = AppointmentScreenConstants.DELETE,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        if (isEditing && newValue.length <= AppointmentScreenConstants.MAX_TITLE_LENGTH) {
                            onTitleChanged(
                                newValue
                            )
                        }
                    },
                    label = { Text(AppointmentScreenConstants.TITLE, style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AppointmentScreenConstants.TITLE_FIELD_TEST_TAG),
                    isError = uiState.titleError != null,
                    readOnly = !isEditing,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiState.titleError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (isEditing) {
                                Text(
                                    text = "${uiState.title.length} / ${AppointmentScreenConstants.MAX_TITLE_LENGTH}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.title.length
                                            == AppointmentScreenConstants.MAX_TITLE_LENGTH
                                        ) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.size(8.dp))

            IconRow(
                icon = { Icon(Icons.Default.DateRange, contentDescription = AppointmentScreenConstants.DATE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEditing) { showStartDatePicker.value = true }
            ) {
                val startDateText = if (uiState.startDate != null) {
                    uiState.startDate.format(DateTimeFormatter.ofPattern(AppointmentScreenConstants.DATE_PATTERN))
                } else {
                    AppointmentScreenConstants.START_TIME
                }

                Column(modifier = Modifier.clickable(enabled = isEditing) { showStartDatePicker.value = true }) {
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
                icon = { Icon(Icons.Default.DateRange, contentDescription = AppointmentScreenConstants.DATE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEditing) { showEndDatePicker.value = true }
            ) {
                val endDateText = if (uiState.endDate != null) {
                    uiState.endDate.format(DateTimeFormatter.ofPattern(AppointmentScreenConstants.DATE_PATTERN))
                } else {
                    AppointmentScreenConstants.END_TIME
                }

                Column(modifier = Modifier.clickable(enabled = isEditing) { showEndDatePicker.value = true }) {
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
                icon = { Icon(Icons.Default.People, contentDescription = AppointmentScreenConstants.PARTICIPANTS) },
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
                                    if (isEditing) onAssignmentChanged(member.id, isChecked)
                                },
                                enabled = isEditing,
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
                        contentDescription = AppointmentScreenConstants.COLOR,
                        tint = uiState.color,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEditing) { showColorPickerBottomSheet.value = true }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isEditing) { showColorPickerBottomSheet.value = true }
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
                icon = { Icon(Icons.Default.Description, contentDescription = AppointmentScreenConstants.DESCRIPTION) }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { newValue ->
                            if (isEditing && newValue.length <= AppointmentScreenConstants.MAX_DESCRIPTION_LENGTH) {
                                onDescriptionChanged(
                                    newValue
                                )
                            }
                        },
                        label = {
                            Text(
                                AppointmentScreenConstants.DESCRIPTION,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag(AppointmentScreenConstants.DESCRIPTION_FIELD_TEST_TAG),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        readOnly = !isEditing,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        supportingText = {
                            if (isEditing) {
                                Text(
                                    text = "${uiState.description.length} / " +
                                        "${AppointmentScreenConstants.MAX_DESCRIPTION_LENGTH}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.description.length
                                            == AppointmentScreenConstants.MAX_DESCRIPTION_LENGTH
                                        ) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontSize = 12.sp
                                    )
                                )
                            }
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

@Preview(showBackground = true, showSystemUi = true, name = "Anzeige Termin")
@Composable
fun AppointmentScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        AppointmentScreenContent(
            uiState = AppointmentUiState(
                title = "Filmeabend",
                startDate = LocalDateTime.of(2025, 4, 20, 20, 0),
                endDate = LocalDateTime.of(2025, 4, 20, 23, 0),
                wgMembers = listOf(
                    WGMemberSelection("max", "Max", true),
                    WGMemberSelection("lena", "Lena", false),
                    WGMemberSelection("tobi", "Tobi", true)
                ),
                color = EventColors.defaultEventColor,
                description = "Gemütlicher Filmeabend mit Snacks und guter Gesellschaft. Denkt an Popcorn!",
                isValid = true
            ),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            goBack = { },
            onTitleChanged = { },
            onStartDateChanged = { },
            onEndDateChanged = { },
            onAssignmentChanged = { _, _ -> },
            onColorChanged = { },
            onDescriptionChanged = { },
            saveEntry = { },
            undoEdits = { },
            deleteEntry = { },
            setEditMode = { }
        )
    }
}
