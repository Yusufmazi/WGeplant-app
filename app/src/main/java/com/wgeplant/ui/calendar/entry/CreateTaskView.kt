package com.wgeplant.ui.calendar.entry

import android.app.DatePickerDialog
import android.content.DialogInterface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.time.format.DateTimeFormatter
import java.util.Calendar

object CreateTaskScreenConstants {
    const val REMOVE_DATE = "Datum entfernen"
    const val SELECT_COLOR = "Farbe auswählen"
    const val TASK = "Aufgabe"
    const val APPOINTMENT = "Termin"
    const val SAVE = "Speichern"
    const val BACK = "Zurück"
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
    const val DROP_DOWN_MENU_TEST_TAG = "drop_down_menu"
}

/**
 * Composable function that sets up the task creation screen and connects it to the [TaskViewModel].
 *
 * Observes UI state and error messages from the ViewModel and passes them into the actual screen content.
 *
 * @param navController The [NavController] used to handle navigation actions.
 * @param createTaskViewModel The [TaskViewModel] instance responsible for task creation logic.
 */
@Composable
fun CreateTaskScreen(
    navController: NavController,
    createTaskViewModel: ITaskViewModel = hiltViewModel()
) {
    val uiState by createTaskViewModel.uiState.collectAsState()
    val errorMessage by createTaskViewModel.errorMessage.collectAsState()

    CreateTaskScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        navController = navController,
        goBack = createTaskViewModel::navigateBack,
        onTitleChanged = createTaskViewModel::onTitleChanged,
        onDateChanged = createTaskViewModel::onDateChanged,
        onAssignmentChanged = createTaskViewModel::onAssignmentChanged,
        onColorChanged = createTaskViewModel::onColorChanged,
        onDescriptionChanged = createTaskViewModel::onDescriptionChanged,
        saveEntry = createTaskViewModel::saveEntry,
        navigateToOtherEntryCreation = createTaskViewModel::navigateToOtherEntryCreation
    )
}

/**
 * Composable function that displays the content of the task creation/editing screen.
 *
 * Includes input fields for title, date, WG member assignment, color selection, and description.
 * Also handles editing state, error messages, bottom sheet interactions, and navigation actions.
 *
 * @param uiState The current UI state of the task form.
 * @param errorMessage An optional error message to display at the bottom of the screen.
 * @param navController The [NavController] used for navigation operations.
 * @param goBack Lambda that navigates back to the previous screen using the provided [NavController].
 * @param onTitleChanged Lambda that is invoked when the task title is modified.
 * @param onDateChanged Lambda that is invoked when a date is selected or removed.
 * @param onAssignmentChanged Lambda that is invoked when a WG member is (de)selected by ID and selection state.
 * @param onColorChanged Lambda that is invoked when a new color is selected.
 * @param onDescriptionChanged Lambda that is invoked when the description is modified.
 * @param saveEntry Lambda that saves the task and performs a navigation action using the provided [NavController].
 * @param navigateToOtherEntryCreation Lambda that navigates to the appointment creation screen using the provided [NavController].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTaskScreenContent(
    uiState: TaskUiState,
    errorMessage: String?,
    navController: NavController,
    goBack: (navController: NavController) -> Unit,
    onTitleChanged: (String) -> Unit,
    onDateChanged: (LocalDate?) -> Unit,
    onAssignmentChanged: (String, Boolean) -> Unit,
    onColorChanged: (Color) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    saveEntry: (navController: NavController) -> Unit,
    navigateToOtherEntryCreation: (navController: NavController) -> Unit
) {
    val context = LocalContext.current

    val showDatePicker = remember { mutableStateOf(false) }

    if (showDatePicker.value) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                onDateChanged(LocalDate.of(selectedYear, selectedMonth + 1, selectedDay))
                showDatePicker.value = false
            },
            year,
            month,
            day
        )

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, CreateTaskScreenConstants.REMOVE_DATE) { _, _ ->
            onDateChanged(null)
            showDatePicker.value = false
        }

        datePickerDialog.setOnDismissListener {
            showDatePicker.value = false
        }

        datePickerDialog.show()
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
                    text = CreateTaskScreenConstants.SELECT_COLOR,
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
                        modifier = Modifier.fillMaxWidth().testTag(CreateTaskScreenConstants.DROP_DOWN_MENU_TEST_TAG)
                    ) {
                        OutlinedTextField(
                            value = CreateTaskScreenConstants.TASK,
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
                                        CreateTaskScreenConstants.TASK,
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
                                        CreateTaskScreenConstants.APPOINTMENT,
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
                            contentDescription = CreateTaskScreenConstants.BACK,
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
                            text = CreateTaskScreenConstants.SAVE,
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
                        if (newValue.length <= CreateTaskScreenConstants.MAX_TITLE_LENGTH) onTitleChanged(newValue)
                    },
                    label = { Text(CreateTaskScreenConstants.TITLE, style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag(CreateTaskScreenConstants.TITLE_FIELD_TEST_TAG),
                    isError = uiState.titleError != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiState.titleError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                            Text(
                                text = "${uiState.title.length} / ${CreateTaskScreenConstants.MAX_TITLE_LENGTH}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.title.length
                                        == CreateTaskScreenConstants.MAX_TITLE_LENGTH
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
                icon = { Icon(Icons.Default.DateRange, contentDescription = CreateTaskScreenConstants.DATE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker.value = true }
            ) {
                val dateText = if (uiState.date != null) {
                    uiState.date.format(DateTimeFormatter.ofPattern(CreateTaskScreenConstants.DATE_PATTERN))
                } else {
                    CreateTaskScreenConstants.NO_DATE
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
                icon = { Icon(Icons.Default.People, contentDescription = CreateTaskScreenConstants.PARTICIPANTS) },
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
                        contentDescription = CreateTaskScreenConstants.COLOR,
                        tint = uiState.color,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showColorPickerBottomSheet.value = true }
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
                icon = { Icon(Icons.Default.Description, contentDescription = CreateTaskScreenConstants.DESCRIPTION) }
            ) {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { newValue ->
                        if (newValue.length <= CreateTaskScreenConstants.MAX_DESCRIPTION_LENGTH) {
                            onDescriptionChanged(
                                newValue
                            )
                        }
                    },
                    label = {
                        Text(
                            CreateTaskScreenConstants.DESCRIPTION_OPTIONAL,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp).testTag(CreateTaskScreenConstants.DESCRIPTION_FIELD_TEST_TAG),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    supportingText = {
                        Text(
                            text = "${uiState.description.length} / " +
                                "${CreateTaskScreenConstants.MAX_DESCRIPTION_LENGTH}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.description.length
                                    == CreateTaskScreenConstants.MAX_DESCRIPTION_LENGTH
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

@Preview(showBackground = true, showSystemUi = true, name = "Erstellung Aufgabe")
@Composable
fun CreateTaskScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        CreateTaskScreenContent(
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
            navController = previewNavController,
            goBack = {},
            onTitleChanged = {},
            onDateChanged = {},
            onAssignmentChanged = { _, _ -> },
            onColorChanged = {},
            onDescriptionChanged = {},
            saveEntry = {},
            navigateToOtherEntryCreation = {}
        )
    }
}
