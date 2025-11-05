package com.wgeplant.ui.calendar.entry

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.icu.util.Calendar
import android.widget.DatePicker
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
import java.time.format.DateTimeFormatter

object TaskScreenConstants {
    const val REMOVE_DATE = "Datum entfernen"
    const val SELECT_COLOR = "Farbe auswählen"
    const val DELETE_TASK = "Aufgabe löschen?"
    const val CANNOT_UNDO = "Diese Aktion kann nicht rückgängig gemacht werden!"
    const val DELETE = "Löschen"
    const val CANCEL = "Abbrechen"
    const val SAVE = "Speichern"
    const val BACK = "Zurück"
    const val EDIT = "Bearbeiten"
    const val TITLE = "Titel"
    const val NO_DATE = "Kein Datum"
    const val PARTICIPANTS = "Teilnehmer"
    const val COLOR = "Farbe"
    const val DESCRIPTION = "Beschreibung"
    const val DESCRIPTION_OPTIONAL = "Beschreibung (optional)"
    const val DATE = "Datum"
    const val MAX_TITLE_LENGTH = 50
    const val MAX_DESCRIPTION_LENGTH = 250
    const val DATE_PATTERN = "dd.MM.yyyy"
    const val TITLE_FIELD_TEST_TAG = "title_field"
    const val DESCRIPTION_FIELD_TEST_TAG = "description_field"
}

/**
 * Entry point composable for the task screen.
 *
 * Collects state from the [ITaskViewModel] and passes it to [TaskScreenContent].
 *
 * @param navController The [NavController] used for navigation.
 * @param taskViewModel The [ITaskViewModel] instance, injected by Hilt.
 */
@Composable
fun TaskScreen(
    navController: NavController,
    taskViewModel: ITaskViewModel = hiltViewModel()
) {
    val uiState by taskViewModel.uiState.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()

    TaskScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        goBack = taskViewModel::navigateBack,
        onTitleChanged = taskViewModel::onTitleChanged,
        onDateChanged = taskViewModel::onDateChanged,
        onAssignmentChanged = taskViewModel::onAssignmentChanged,
        onColorChanged = taskViewModel::onColorChanged,
        onDescriptionChanged = taskViewModel::onDescriptionChanged,
        saveEntry = taskViewModel::saveEntry,
        undoEdits = taskViewModel::undoEdits,
        deleteEntry = taskViewModel::delete,
        setEditMode = taskViewModel::setEditMode
    )
}

/**
 * Composable function that displays the content of the task creation/editing screen.
 *
 * Includes input fields for title, date, WG member assignment, color selection, and description.
 * Also handles editing state, error messages, loading indication, and navigation actions.
 *
 * @param uiState The current UI state of the task form.
 * @param errorMessage An optional error message to display at the bottom of the screen.
 * @param isLoading Whether a save or delete operation is currently in progress.
 * @param navController The [NavController] used for navigation operations.
 * @param goBack Lambda that navigates back to the previous screen.
 * @param onTitleChanged Lambda that is invoked when the task title is modified.
 * @param onDateChanged Lambda that is invoked when the date is selected or removed.
 * @param onAssignmentChanged Lambda that is invoked when a WG member is (de)selected.
 * @param onColorChanged Lambda that is invoked when a new color is selected.
 * @param onDescriptionChanged Lambda that is invoked when the description is changed.
 * @param saveEntry Lambda that saves the current task and performs a navigation action.
 * @param undoEdits Lambda that discards any unsaved changes and exits edit mode.
 * @param deleteEntry Lambda that deletes the current task and performs a navigation action.
 * @param setEditMode Lambda function to toggle the edit mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreenContent(
    uiState: TaskUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    goBack: (navController: NavController) -> Unit,
    onTitleChanged: (String) -> Unit,
    onDateChanged: (LocalDate?) -> Unit,
    onAssignmentChanged: (String, Boolean) -> Unit,
    onColorChanged: (Color) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    saveEntry: (navController: NavController) -> Unit,
    undoEdits: () -> Unit,
    deleteEntry: (navController: NavController) -> Unit,
    setEditMode: () -> Unit
) {
    val context = LocalContext.current

    val isEditing = uiState.isEditing
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val showDatePicker = remember { mutableStateOf(false) }

    if (showDatePicker.value) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                onDateChanged(LocalDate.of(selectedYear, selectedMonth + 1, selectedDay))
                showDatePicker.value = false
            },
            year,
            month,
            day
        )

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, TaskScreenConstants.REMOVE_DATE) { _, _ ->
            onDateChanged(null)
            showDatePicker.value = false
        }

        dialog.setOnDismissListener {
            showDatePicker.value = false
        }

        dialog.show()
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
                    text = TaskScreenConstants.SELECT_COLOR,
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

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(TaskScreenConstants.DELETE_TASK) },
            text = { Text(TaskScreenConstants.CANNOT_UNDO, style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                Button(
                    onClick = {
                        deleteEntry(navController)
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text(TaskScreenConstants.DELETE)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmationDialog = false }
                ) {
                    Text(TaskScreenConstants.CANCEL)
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
                                text = TaskScreenConstants.CANCEL,
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
                                contentDescription = TaskScreenConstants.BACK,
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
                                text = TaskScreenConstants.SAVE,
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
                                contentDescription = TaskScreenConstants.EDIT,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = TaskScreenConstants.DELETE,
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
                        if (isEditing && newValue.length <= TaskScreenConstants.MAX_TITLE_LENGTH) {
                            onTitleChanged(
                                newValue
                            )
                        }
                    },
                    label = { Text(TaskScreenConstants.TITLE, style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag(TaskScreenConstants.TITLE_FIELD_TEST_TAG),
                    isError = uiState.titleError != null,
                    readOnly = !isEditing,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiState.titleError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

                            if (isEditing) {
                                Text(
                                    text = "${uiState.title.length} / ${TaskScreenConstants.MAX_TITLE_LENGTH}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.title.length == TaskScreenConstants.MAX_TITLE_LENGTH) {
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
                icon = { Icon(Icons.Default.DateRange, contentDescription = TaskScreenConstants.DATE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEditing) { showDatePicker.value = true }
            ) {
                val dateText = if (uiState.date != null) {
                    uiState.date.format(DateTimeFormatter.ofPattern(TaskScreenConstants.DATE_PATTERN))
                } else {
                    TaskScreenConstants.NO_DATE
                }
                Column {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )

                    if (uiState.dateError != null) {
                        Text(
                            text = uiState.dateError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))

            IconRow(
                icon = { Icon(Icons.Default.People, contentDescription = TaskScreenConstants.PARTICIPANTS) },
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
                        contentDescription = TaskScreenConstants.COLOR,
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
                icon = { Icon(Icons.Default.Description, contentDescription = TaskScreenConstants.DESCRIPTION) }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { newValue ->
                            if (isEditing && newValue.length <= TaskScreenConstants.MAX_DESCRIPTION_LENGTH) {
                                onDescriptionChanged(
                                    newValue
                                )
                            }
                        },
                        label = {
                            Text(
                                TaskScreenConstants.DESCRIPTION_OPTIONAL,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp).testTag(TaskScreenConstants.DESCRIPTION_FIELD_TEST_TAG),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        readOnly = !isEditing,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        supportingText = {
                            if (isEditing) {
                                Text(
                                    text = "${uiState.description.length} / " +
                                        "${TaskScreenConstants.MAX_DESCRIPTION_LENGTH}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.description.length
                                            == TaskScreenConstants.MAX_DESCRIPTION_LENGTH
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

@Preview(showBackground = true, showSystemUi = true, name = "Anzeige Aufgabe")
@Composable
fun TaskScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        TaskScreenContent(
            uiState = TaskUiState(
                title = "Einkaufen",
                date = LocalDate.now(),
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
            isLoading = false,
            navController = previewNavController,
            goBack = {},
            onTitleChanged = {},
            onDateChanged = {},
            onAssignmentChanged = { _, _ -> },
            onColorChanged = {},
            onDescriptionChanged = {},
            saveEntry = {},
            undoEdits = {},
            deleteEntry = {},
            setEditMode = {}
        )
    }
}
