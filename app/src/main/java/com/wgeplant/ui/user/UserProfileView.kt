package com.wgeplant.ui.user

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.wgeplant.R
import com.wgeplant.model.domain.Absence
import com.wgeplant.ui.navigation.Routes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

private const val CONFIRM_LEAVE_WG = "Möchtest du die WG wirklich verlassen?"
private const val CONFIRM_LOGOUT = "Möchtest du dich wirklich abmelden?"
private const val CONFIRM_DELETE_ACCOUNT = "Möchtest du dein Konto wirklich löschen?"
private const val CONFIRMATION_TITLE = "Bist du dir sicher?"
private const val NAVIGATION_BACK_DESC = "Zurück"
private const val CANCEL_TEXT = "Abbrechen"
private const val EDIT_DESC = "Bearbeiten"
private const val SAVE_DESC = "Speichern"
private const val PROFILE_PIC_DESC = "Profilbild"
private const val USERNAME_LABEL = "Benutzername"
private const val ABSENCES_BUTTON = "Abwesenheiten"
private const val LEAVE_WG_BUTTON = "WG verlassen"
private const val LOGOUT_BUTTON = "Abmelden"
private const val DELETE_ACCOUNT_BUTTON = "Account löschen"
private const val ABSENCE_DIALOG_TITLE = "Abwesenheiten"
private const val ADD_ABSENCE_TEXT = "Abwesenheit hinzufügen"
private const val EDIT_DELETE_ABSENCE_TEXT = "Abwesenheit bearbeiten/löschen"
private const val DELETE_ABSENCE_TITLE = "Abwesenheit löschen"
private const val DELETE_ABSENCE_MESSAGE = "Möchtest du diese Abwesenheit wirklich löschen?"
private const val CONFIRM_YES = "Ja"
private const val CONFIRM_NO = "Nein"
private const val DATE_FORMAT_PATTERN = "dd.MM.yyyy"
private const val DIALOG_TITLE = "Abwesenheit"
private const val START_DATE_LABEL = "Startdatum (TT.MM.JJJJ)"
private const val END_DATE_LABEL = "Enddatum (TT.MM.JJJJ)"
private const val PAST_DATE_ERROR = "Vergangene Daten sind nicht erlaubt"
private const val END_BEFORE_START_ERROR = "Enddatum darf nicht vor dem Startdatum liegen"
private const val SAVE_BUTTON_TEXT = "Speichern"
private const val CANCEL_BUTTON_TEXT = "Abbrechen"
private const val EDIT_ABSENCES_TITLE = "Abwesenheiten bearbeiten"
private const val NO_ABSENCES_TEXT = "Keine Abwesenheiten verfügbar."
private const val DELETE_DESC = "Löschen"
private const val CLOSE_BUTTON_TEXT = "Schließen"
private const val WG_ICON_DESC = "WG"
private const val WG_TEXT = "WG"
private const val PROFILE_ICON_DESC = "Profil"
private const val PROFILE_TEXT = "Profil"
private const val ABSENCE = "Abwesenheit"
private const val WITHOUT_ID = "Versuch, die Abwesenheit mit der ID zu löschen"
private const val ABSENCE_ERROR = "Abwesenheits-ID ist null oder leer, kann nicht gelöscht werden."
private const val YES = "Ja"
private const val NO = "Nein"
private const val NO_CACHE = "nocache"
private const val PROFILE_REMOVE_TEXT = "Profilbild entfernen"
private const val PROFILE_NEW_PICTURE_TEXT = "Neues Bild auswählen"
private const val IMAGE = "image/*"
private const val ACTION_BUTTON_WIDTH = 0.7f
private val PROFILE_FONT_SIZE = 16.sp
private val VERTICAL_PADDING = 6.dp
private val BUTTON_CORNER_RADIUS = 10.dp
private val ACTION_BUTTON_VERTICAL_PADDING = 12.dp
private val ACTION_BUTTON_FONT_SIZE = 16.sp
private val BOTTOM_NAV_VERTICAL_PADDING = 16.dp
private val WG_FONT_SIZE = 15.sp
private val SPACER_WIDTH = 4.dp
private val SPACER_HEIGHT = 8.dp
private val PROFILE_IMAGE_SIZE = 120.dp
private val ICON_BUTTON_SIZE = 24.dp
private val BACK_ICON_BUTTON_SIZE = 40.dp
private val VERTICAL_SPACER_SMALL = 8.dp
private val VERTICAL_SPACER_MEDIUM = 12.dp
private val TEXT_FIELD_WIDTH_FRACTION = 0.7f
private val OUTLINED_TEXT_FIELD_CORNER_RADIUS = 10.dp
private val LINEHEIGHT = 29.sp
private val FONTSIZE = 20.sp
private val VERTICAL_SPACER_LARGE = 16.dp
private val TEXT_FIELD_WIDTH = 0.6f
private val TEXT_SIZE_MINI = 12.sp
private const val MAX_DISPLAY_NAME_LENGTH = 15

enum class BottomTab { WG, PROFILE }

/**
 * UserProfileScreen displays the user's personal profile,
 * including their name, profile picture, and account options.
 * Handles all UI and state logic.
 */
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: IUserProfileViewModel = hiltViewModel()
) {
    // Collect UI state and other state flows from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State for confirm dialog action and message
    var confirmAction: (() -> Unit)? by remember { mutableStateOf(null) }
    var confirmMessage by remember { mutableStateOf("") }

    // State for showing profile picture selection dialog
    var showPictureDialog by remember { mutableStateOf(false) }

//    // Launcher for image picker to select a profile picture
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { viewModel.changeProfilePicture(it) }
//    }
    var navShield by remember { mutableStateOf(false) }

    // Initial load of user profile
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Reload user profile on lifecycle resume (e.g., when returning to screen)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Main screen content composable, forwarding all required parameters and callbacks
    UserProfileScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navShield = navShield,
        navController = navController,
        undoEdits = viewModel::undoEdits,
        goBack = viewModel::navigateBack,
        onToggleEdit = viewModel::toggleEdit,
        onSaveEditing = viewModel::saveEditing,
        onUserNameChanged = viewModel::onDisplayNameChanged,
        onAbsence = viewModel::openAbsenceDialog,
        onAbsenceDismiss = viewModel::closeAbsenceDialog,
//        onRemoveProfilePicture = viewModel::onRemoveProfilePicture,
//        onNewPhotoSelected = { imagePickerLauncher.launch(IMAGE) },
        showPictureDialog = showPictureDialog,
        closePictureDialog = { showPictureDialog = false },
        openPictureDialog = { showPictureDialog = true },
        onAbsenceConfirm = viewModel::addAbsence,
        onLeaveWG = {
            confirmMessage = CONFIRM_LEAVE_WG
            confirmAction = {
                viewModel.leaveWG()
            }
        },
        onLogout = {
            confirmMessage = CONFIRM_LOGOUT
            confirmAction = {
                viewModel.logout()
            }
        },

        onDeleteAccount = {
            confirmMessage = CONFIRM_DELETE_ACCOUNT
            confirmAction = {
                viewModel.deleteAccount(navController)
            }
        },
        onWGClicked = {
            navShield = true
            if (uiState.isInWG) {
                navController.navigate(Routes.PROFILE_WG) {
                    popUpTo(Routes.PROFILE_USER) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(Routes.CHOOSE_WG) {
                    popUpTo(Routes.PROFILE_USER) { inclusive = true }
                    launchSingleTop = true
                }
            }
        },
        onProfileClicked = {},

        onDeleteAbsence = { absenceId ->
            viewModel.deleteAbsence(absenceId)
        },
        onEditAbsence = { id, start, end ->
            viewModel.editAbsence(id, start, end)
        }
    )

    // Confirm deletion / logout / leave dialogs
    if (confirmAction != null) {
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text(CONFIRMATION_TITLE, color = MaterialTheme.colorScheme.onBackground) },
            text = { Text(confirmMessage, style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                TextButton(onClick = {
                    confirmAction?.invoke()
                    confirmAction = null
                }) { Text(YES) }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmAction = null }) { Text(NO) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreenContent(
    uiState: UserProfileUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    undoEdits: () -> Unit,
    goBack: (navController: NavController) -> Unit,
    onUserNameChanged: (String) -> Unit,
    onAbsence: () -> Unit,
    onLeaveWG: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onAbsenceDismiss: () -> Unit,
    onAbsenceConfirm: (String, String) -> Unit,
    onToggleEdit: () -> Unit,
    onSaveEditing: () -> Unit,
    onWGClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    onDeleteAbsence: (String) -> Unit,
//    onRemoveProfilePicture: () -> Unit,
//    onNewPhotoSelected: () -> Unit,
    showPictureDialog: Boolean,
    openPictureDialog: () -> Unit,
    closePictureDialog: () -> Unit,
    onEditAbsence: (String, LocalDate, LocalDate) -> Unit,
    navShield: Boolean
) {
    val context = LocalContext.current

    // States for various dialogs and UI control
    var showAbsenceOptions by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingAbsence by remember { mutableStateOf<Absence?>(null) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var absenceToDeleteId by remember { mutableStateOf<String?>(null) }

    // Load profile picture using Coil, with cache busting param to avoid stale images
    val painter = when {
        uiState.localProfileImage != null -> rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(uiState.localProfileImage)
                .setParameter(NO_CACHE, System.currentTimeMillis().toString())
                .crossfade(true)
                .scale(Scale.FILL)
                .build()
        )
        uiState.profilePictureUri != null -> rememberAsyncImagePainter(uiState.profilePictureUri)
        else -> painterResource(id = R.drawable.ic_default_user_profile)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.isInWG) {
                                navController.navigate(Routes.CALENDAR_MONTH)
                            } else {
                                goBack(navController)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = NAVIGATION_BACK_DESC,
                            modifier = Modifier.size(BACK_ICON_BUTTON_SIZE),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        TextButton(
                            onClick = { undoEdits() },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = CANCEL_TEXT,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = FONTSIZE,
                                    lineHeight = LINEHEIGHT
                                )
                            )
                        }
                    } else {
                        IconButton(onClick = onToggleEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = EDIT_DESC,
                                modifier = Modifier.size(ICON_BUTTON_SIZE),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (uiState.isInWG) {
                BottomNavigationBarProfile(
                    selectedTab = BottomTab.PROFILE,
                    onWGClicked = onWGClicked,
                    onProfileClicked = onProfileClicked,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing
                            .only(WindowInsetsSides.Bottom)
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ICON_BUTTON_SIZE),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(VERTICAL_SPACER_SMALL))

            Image(
                painter = painter,
                contentDescription = PROFILE_PIC_DESC,
                modifier = Modifier
                    .size(PROFILE_IMAGE_SIZE)
                    .clip(CircleShape),
                // .clickable(enabled = uiState.isEditing) { showPictureDialog = true },
                contentScale = ContentScale.Crop,
                colorFilter = if (uiState.localProfileImage == null &&
                    uiState.profilePictureUri == null
                ) {
                    ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                } else {
                    null
                }
            )
            Spacer(modifier = Modifier.height(VERTICAL_SPACER_MEDIUM))

            if (uiState.isEditing) {
                val hasError = uiState.nameError != null
                val showCounter = !hasError

                OutlinedTextField(
                    value = uiState.userName,
                    onValueChange = onUserNameChanged,
                    label = { Text(USERNAME_LABEL) },
                    modifier = Modifier.fillMaxWidth(TEXT_FIELD_WIDTH_FRACTION),
                    singleLine = true,
                    isError = hasError,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiState.nameError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(Modifier.weight(1f))
                            if (showCounter) {
                                Text(
                                    text = "${uiState.userName.length}/$MAX_DISPLAY_NAME_LENGTH",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = TEXT_SIZE_MINI),
                                    color = if (uiState.userName.length > MAX_DISPLAY_NAME_LENGTH) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onBackground
                                    }
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = onSaveEditing) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = SAVE_DESC,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            } else {
                Text(
                    text = uiState.userName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = FONTSIZE
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isInWG) {
                ProfileActionButton(ABSENCES_BUTTON) { showAbsenceOptions = true }
                ProfileActionButton(LEAVE_WG_BUTTON, onClick = onLeaveWG)
            }

            ProfileActionButton(LOGOUT_BUTTON, onClick = onLogout)
            ProfileActionButton(DELETE_ACCOUNT_BUTTON, onClick = onDeleteAccount)
        }

        // Picture selection dialog
        if (showPictureDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = TEXT_FIELD_WIDTH))
                    .clickable { closePictureDialog() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(ICON_BUTTON_SIZE)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(VERTICAL_SPACER_LARGE),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.profilePictureUri != null) {
                        TextButton(onClick = {
                            // onRemoveProfilePicture()
                            closePictureDialog()
                        }) {
                            Text(PROFILE_REMOVE_TEXT, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = {
                        // onNewPhotoSelected()
                        closePictureDialog()
                    }) {
                        Text(PROFILE_NEW_PICTURE_TEXT, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Absence dialog inline for adding new absence
        if (uiState.showAbsenceDialog) {
            AbsenceDialogInline(
                onDismiss = onAbsenceDismiss,
                onConfirm = onAbsenceConfirm
            )
        }

        // Absence editing dialog
        if (editingAbsence != null) {
            AbsenceDialogInline(
                onDismiss = { editingAbsence = null },
                onConfirm = { newStart, newEnd ->
                    editingAbsence?.let {
                        onEditAbsence(
                            it.absenceId ?: return@let,
                            LocalDate.parse(newStart),
                            LocalDate.parse(newEnd)
                        )
                        editingAbsence = null
                    }
                },
                initialStartDate = editingAbsence!!.startDate,
                initialEndDate = editingAbsence!!.endDate
            )
        }

        // Show absence options dialog
        if (showAbsenceOptions) {
            AlertDialog(
                onDismissRequest = { showAbsenceOptions = false },
                title = { Text(ABSENCE_DIALOG_TITLE) },
                text = {
                    Column {
                        TextButton(onClick = {
                            showAbsenceOptions = false
                            onAbsence()
                        }) {
                            Text(ADD_ABSENCE_TEXT)
                        }
                        TextButton(onClick = {
                            showAbsenceOptions = false
                            showEditDialog = true
                        }) {
                            Text(EDIT_DELETE_ABSENCE_TEXT)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(onClick = { showAbsenceOptions = false }) {
                        Text(CANCEL_TEXT)
                    }
                }
            )
        }

        // Absence list dialog for edit/delete
        if (showEditDialog) {
            AbsenceListDialog(
                absences = uiState.userAbsences,
                onDismiss = { showEditDialog = false },
                onEdit = { absence ->
                    editingAbsence = absence
                    showEditDialog = false
                },
                onDelete = { absenceId ->
                    absenceToDeleteId = absenceId
                    showConfirmDeleteDialog = true
                }
            )
        }

        // Confirmation dialog for deleting absence
        if (showConfirmDeleteDialog && absenceToDeleteId != null) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmDeleteDialog = false
                    absenceToDeleteId = null
                },
                title = { Text(DELETE_ABSENCE_TITLE) },
                text = { Text(DELETE_ABSENCE_MESSAGE) },
                confirmButton = {
                    TextButton(onClick = {
                        absenceToDeleteId?.let { id ->
                            onDeleteAbsence(id)
                        }
                        showConfirmDeleteDialog = false
                        absenceToDeleteId = null
                    }) {
                        Text(CONFIRM_YES)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showConfirmDeleteDialog = false
                        absenceToDeleteId = null
                    }) {
                        Text(CONFIRM_NO)
                    }
                }
            )
        }
        if (navShield) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) { awaitPointerEvent() }
                        }
                    }
            )
        }
    }
}

@Composable
fun AbsenceDialogInline(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)

    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val isStartDateInvalid = startDate != null && startDate!!.isBefore(today)
    val isEndDateInvalid = endDate != null && (
        endDate!!.isBefore(today) ||
            (startDate != null && endDate!!.isBefore(startDate))
        )

    if (showStartPicker) {
        showDatePickerDialog(
            context = context,
            onDateSelected = {
                startDate = it
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        showDatePickerDialog(
            context = context,
            onDateSelected = {
                endDate = it
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(DIALOG_TITLE, color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column {
                DateField(
                    label = START_DATE_LABEL,
                    value = startDate?.format(formatter) ?: "",
                    onClick = { showStartPicker = true },
                    isError = isStartDateInvalid,
                    errorMessage = if (isStartDateInvalid) PAST_DATE_ERROR else null
                )
                Spacer(modifier = Modifier.height(SPACER_HEIGHT))
                DateField(
                    label = END_DATE_LABEL,
                    value = endDate?.format(formatter) ?: "",
                    onClick = { showEndPicker = true },
                    isError = isEndDateInvalid,
                    errorMessage = when {
                        endDate?.isBefore(today) == true -> PAST_DATE_ERROR
                        endDate != null && startDate != null &&
                            endDate!!.isBefore(startDate) -> END_BEFORE_START_ERROR
                        else -> null
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isStartDateInvalid && !isEndDateInvalid && startDate != null &&
                        endDate != null
                    ) {
                        onConfirm(
                            startDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            endDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        )
                        onDismiss()
                    }
                },
                enabled = startDate != null && endDate != null && !isStartDateInvalid &&
                    !isEndDateInvalid
            ) {
                Text(SAVE_BUTTON_TEXT)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(CANCEL_BUTTON_TEXT)
            }
        }
    )
}

@Composable
fun DateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label, color = MaterialTheme.colorScheme.onBackground) },
            readOnly = true,
            enabled = false,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                disabledBorderColor = MaterialTheme.colorScheme.onBackground
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun showDatePickerDialog(
    context: Context,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(selectedDate)
        },
        year,
        month,
        day
    ).apply {
        setOnCancelListener { onDismiss() }
        show()
    }
}

@Composable
fun AbsenceListDialog(
    absences: List<Absence>,
    onDismiss: () -> Unit,
    onEdit: (Absence) -> Unit,
    onDelete: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(EDIT_ABSENCES_TITLE) },
        text = {
            if (absences.isEmpty()) {
                Text(NO_ABSENCES_TEXT)
            } else {
                Column {
                    absences.forEach { absence ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = VERTICAL_PADDING),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${absence.startDate.format(formatter)} " +
                                    "– ${absence.endDate.format(formatter)}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row {
                                IconButton(onClick = { onEdit(absence) }) {
                                    Icon(Icons.Default.Edit, contentDescription = EDIT_DESC)
                                }
                                IconButton(onClick = {
                                    val id = absence.absenceId
                                    if (!id.isNullOrBlank()) {
                                        android.util.Log.d(
                                            ABSENCE,
                                            WITHOUT_ID
                                        )
                                        onDelete(id)
                                    } else {
                                        android.util.Log.e(ABSENCE, ABSENCE_ERROR)
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = DELETE_DESC,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(CLOSE_BUTTON_TEXT)
            }
        }
    )
}

@Composable
fun ProfileActionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(ACTION_BUTTON_WIDTH)
            .padding(vertical = VERTICAL_PADDING)
            .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() }
            .padding(vertical = ACTION_BUTTON_VERTICAL_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = ACTION_BUTTON_FONT_SIZE
            )
        )
    }
}

@Composable
fun BottomNavigationBarProfile(
    selectedTab: BottomTab,
    onWGClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val wgSelected = selectedTab == BottomTab.WG
        val profileSelected = selectedTab == BottomTab.PROFILE

        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    if (wgSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
                .padding(vertical = BOTTOM_NAV_VERTICAL_PADDING)
                .clickable(onClick = onWGClicked),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = WG_ICON_DESC,
                    tint = if (wgSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        Color.Black
                    }
                )
                Spacer(modifier = Modifier.width(SPACER_WIDTH))
                Text(
                    text = WG_TEXT,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (wgSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            Color.Black
                        },
                        fontSize = WG_FONT_SIZE
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    if (profileSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
                .padding(vertical = BOTTOM_NAV_VERTICAL_PADDING)
                .clickable(onClick = onProfileClicked),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = PROFILE_ICON_DESC,
                    tint = if (profileSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        Color.Black
                    }
                )
                Text(
                    text = PROFILE_TEXT,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = PROFILE_FONT_SIZE,
                        color = if (profileSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            Color.Black
                        }
                    )
                )
            }
        }
    }
}
