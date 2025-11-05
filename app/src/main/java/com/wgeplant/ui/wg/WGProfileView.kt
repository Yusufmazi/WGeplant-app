package com.wgeplant.ui.wg

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.wgeplant.R
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.User
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.ui.user.BottomNavigationBarProfile
import com.wgeplant.ui.user.BottomTab
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

private val SCREEN_HORIZONTAL_PADDING = 24.dp
private val TOP_SPACER_HEIGHT = 8.dp
private val FIELD_SPACER_HEIGHT = 12.dp
private val LIST_BOTTOM_SPACER_HEIGHT = 16.dp
private val TEXT_FIELD_WIDTH_FRACTION = 0.7f
private val PROFILE_BUTTON_HEIGHT = 50.dp
private val BUTTON_CORNER_RADIUS = 10.dp
private val ICON_SIZE_BACK = 40.dp
private val ICON_SIZE_EDIT = 24.dp
private val PROFILE_CARD_SPACING_DP = 12.dp
private val PROFILE_BUTTON_ICON_SPACER = 8.dp

private val TEXT_SIZE_TITLE = 20.sp
private val TEXT_SIZE_LINE_HEIGHT = 29.sp
private val TEXT_SIZE_BODY = 16.sp

private const val BACK_BUTTON_DESC = "Zurück"
private const val EDIT_BUTTON_DESC = "Bearbeiten"
private const val SAVE_BUTTON_DESC = "Speichern"
private const val CANCEL_BUTTON_TEXT = "Abbrechen"
private const val WG_NAME_LABEL = "WG-Name"
private const val INVITE_BUTTON_TEXT = "Mitbewohner einladen"
private const val INVITE_ICON_DESC = "Einladen"
private const val WG_PIC_DESC = "WG Bild"
private const val DATE_FORMAT_PATTERN = "dd.MM.yy"
private const val PROFILE_IMAGE_DESC = "Profilbild"
private const val REMOVE_USER_DESC = "Entfernen"
private const val DIALOG_CLOSE_DESC = "Schließen"
private const val USER_ICON_DESC = "User Icon"
private const val ABSENCE_LABEL = "Abwesend bis"
private const val NO_ABSENCES_TEXT = "Keine weiteren Abwesenheiten"
private const val UPCOMING_ABSENCES_LABEL = "Nächste Abwesenheiten:"
private const val INVITATION_TITLE = "Einladungscode"
private const val INVITATION_CLOSE_TEXT = "Schließen"
private const val REMOVE_DIALOG_TITLE = "Mitbewohner entfernen"
private const val REMOVE_DIALOG_CONFIRM = "Ja"
private const val REMOVE_DIALOG_CANCEL = "Nein"
private const val REMOVE_DIALOG_TEXT_PREFIX = "Möchtest du "
private const val REMOVE_DIALOG_TEXT_SUFFIX = " wirklich aus der WG entfernen?"
private const val ME = " (ich)"
private const val NO_CACHE = "nocache"
private const val IMAGE = "image/*"
private const val REMOVE_PROFILE_PIC_TEXT = "Profilbild entfernen"
private const val SELECT_NEW_PIC_TEXT = "Neues Bild auswählen"
private val PROFILE_CARD_HEIGHT = 64.dp
private val PROFILE_CARD_BORDER_WIDTH = 1.dp
private val PROFILE_CARD_BORDER_RADIUS = 10.dp
private val PROFILE_CARD_HORIZONTAL_PADDING = 12.dp
private val PROFILE_CARD_VERTICAL_PADDING = 8.dp
private val PROFILE_IMAGE_SIZE_SMALL = 40.dp
private val PROFILE_IMAGE_SIZE = 120.dp

private val STATUS_DOT_SIZE = 12.dp
private val REMOVE_ICON_SPACER = 8.dp
private val PROFILE_DIALOG_PADDING = 24.dp
private val PROFILE_DIALOG_CORNER_RADIUS = 12.dp
private val PROFILE_DIALOG_INNER_PADDING = 16.dp
private const val BACKGROUND_ALPHA_DIALOG = 0.6f
private val AVATAR_SIZE = 64.dp
private val DIALOG_BORDER_WIDTH = 2.dp
private val DIALOG_CORNER_RADIUS = 16.dp
private val DIALOG_OUTER_PADDING = 24.dp
private val DIALOG_INNER_PADDING = 16.dp

private val SPACER_4 = 4.dp
private val SPACER_8 = 8.dp
private val SPACER_12 = 12.dp
private val SPACER_16 = 16.dp
private val TEXT_SIZE_SMALL = 14.sp
private val TEXT_SIZE_MEDIUM = 16.sp
private val TEXT_SIZE_LARGE = 18.sp
private val TEXT_SIZE_XL = 20.sp
private val TEXT_FIELD_WIDTH = 0.6f
private val TEXT_SIZE_MINI = 12.sp
private const val MAX_DISPLAY_NAME_LENGTH = 15

/**
 * WGProfileScreen displays the shared flat profile,
 * its picture, member list, and editing options. Handles all UI and state logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WGProfileScreen(
    navController: NavController,
    viewModel: IWGProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var userToRemove by remember { mutableStateOf<User?>(null) }

    var showPictureDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var navShield by remember { mutableStateOf(false) }

    // Launcher for selecting profile picture from device
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            result.data?.data?.let {
//                selectedImageUri = it
//                viewModel.onNewProfilePictureSelected(it)
//            }
//        }
//    }

    LaunchedEffect(Unit) {
        navShield = false
        viewModel.loadWGData()
        viewModel.loadWGMembers()
    }

    // Refresh WG data when screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                navShield = false
                viewModel.loadWGData()
                viewModel.loadWGMembers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.uiState.value.isEditing) {
                viewModel.toggleEditMode()
            }
            navShield = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack(navController) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = BACK_BUTTON_DESC,
                            modifier = Modifier.size(ICON_SIZE_BACK),
                            tint = colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        TextButton(
                            onClick = { viewModel.undoEdits() },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = CANCEL_BUTTON_TEXT,
                                color = colorScheme.primary,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = TEXT_SIZE_TITLE,
                                    lineHeight = TEXT_SIZE_LINE_HEIGHT
                                )
                            )
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = EDIT_BUTTON_DESC,
                                modifier = Modifier.size(ICON_SIZE_EDIT),
                                tint = colorScheme.primary
                            )
                        }
                    }
                },
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBarProfile(
                selectedTab = BottomTab.WG,
                onWGClicked = { },
                onProfileClicked = {
                    navShield = true
                    navController.navigate(Routes.PROFILE_USER) {
                        popUpTo(Routes.PROFILE_USER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                )
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = SCREEN_HORIZONTAL_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(TOP_SPACER_HEIGHT))

            // Determine profile picture painter (local, remote, or fallback)
            val painter = when {
                uiState.localProfileImage != null -> rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(uiState.localProfileImage)
                        .setParameter(NO_CACHE, UUID.randomUUID().toString())
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .build()
                )
                !uiState.wg?.profilePicture.isNullOrBlank() -> rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(uiState.wg?.profilePicture)
                        .setParameter(NO_CACHE, UUID.randomUUID().toString())
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .build()
                )
                else -> painterResource(id = R.drawable.icon)
            }

            val isDefaultIcon = uiState.localProfileImage == null && uiState.wg?.profilePicture
                .isNullOrBlank()

            // WG profile image
            Image(
                painter = painter,
                contentDescription = WG_PIC_DESC,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(PROFILE_IMAGE_SIZE)
                    .clip(CircleShape),
                // click option
                // .clickable(enabled = uiState.isEditing) { showPictureDialog = true },
                contentScale = ContentScale.Crop,
                colorFilter = if (isDefaultIcon) ColorFilter.tint(colorScheme.primary) else null
            )

            Spacer(modifier = Modifier.height(FIELD_SPACER_HEIGHT))

            // WG name field (editable or static)
            if (uiState.isEditing) {
                val hasError = uiState.wgNameError != null

                OutlinedTextField(
                    value = uiState.wgName,
                    onValueChange = viewModel::onWGNameChanged,
                    label = { Text(WG_NAME_LABEL) },
                    modifier = Modifier.fillMaxWidth(TEXT_FIELD_WIDTH_FRACTION),
                    singleLine = true,
                    isError = hasError,
                    supportingText = {
                        if (hasError) {
                            Text(text = uiState.wgNameError!!, color = colorScheme.error)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "${uiState.wgName.length}/$MAX_DISPLAY_NAME_LENGTH",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.wgName.length > MAX_DISPLAY_NAME_LENGTH) {
                                            colorScheme.error
                                        } else {
                                            colorScheme.onSurface
                                        },
                                        fontSize = TEXT_SIZE_MINI
                                    )
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = TEXT_SIZE_BODY,
                        color = colorScheme.onBackground
                    ),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.saveEditing() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = SAVE_BUTTON_DESC,
                                tint = colorScheme.primary
                            )
                        }
                    }
                )
            } else {
                Text(
                    text = uiState.wgName,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = TEXT_SIZE_TITLE)
                )
            }

            Spacer(modifier = Modifier.height(SCREEN_HORIZONTAL_PADDING))

            // Member list (sorted, showing absences & remove buttons)
            val sortedUsers = uiState.users.distinctBy { it.userId }
                .sortedByDescending { it.userId == uiState.currentUserId }

            sortedUsers.forEach { user ->
                val isCurrentUser = user.userId == uiState.currentUserId
                val displayName = if (isCurrentUser) "${user.displayName}$ME" else user.displayName
                val absences = uiState.userAbsences[user.userId].orEmpty()
                val today = LocalDate.now()
                val isOnline = absences.none { it.startDate <= today && today <= it.endDate }

                WGProfileCard(
                    user = user,
                    displayName = displayName,
                    isOnline = isOnline,
                    isEditing = uiState.isEditing,
                    onClick = {
                        if (uiState.selectedUser == null && !uiState.showInvitationDialog) {
                            viewModel.onUserSelected(user.userId)
                        }
                    },
                    onRemove = {
                        if (!isCurrentUser) userToRemove = user
                    },
                    showRemove = !isCurrentUser
                )

                Spacer(modifier = Modifier.height(FIELD_SPACER_HEIGHT))
            }

            Spacer(modifier = Modifier.height(LIST_BOTTOM_SPACER_HEIGHT))

            // Invite member button
            Button(
                onClick = {
                    if (uiState.selectedUser == null && !uiState.showInvitationDialog) {
                        viewModel.showInvitationDialog()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PROFILE_BUTTON_HEIGHT),
                shape = RoundedCornerShape(BUTTON_CORNER_RADIUS)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = INVITE_ICON_DESC,
                    tint = colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(PROFILE_BUTTON_ICON_SPACER))
                Text(
                    INVITE_BUTTON_TEXT,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colorScheme.onPrimary,
                        fontSize = TEXT_SIZE_BODY
                    )
                )
            }
        }

        // Profile picture action dialog
        if (showPictureDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.scrim.copy(alpha = BACKGROUND_ALPHA_DIALOG))
                    .clickable { showPictureDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(PROFILE_DIALOG_PADDING)
                        .background(
                            colorScheme.surface,
                            RoundedCornerShape(PROFILE_DIALOG_CORNER_RADIUS)
                        )
                        .padding(PROFILE_DIALOG_INNER_PADDING),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!uiState.wg?.profilePicture.isNullOrEmpty()) {
                        TextButton(onClick = {
                            // viewModel.onRemoveProfilePicture()
                            showPictureDialog = false
                        }) {
                            Text(REMOVE_PROFILE_PIC_TEXT, color = colorScheme.error)
                        }
                    }
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = IMAGE }
                        // imagePickerLauncher.launch(intent)
                        showPictureDialog = false
                    }) {
                        Text(SELECT_NEW_PIC_TEXT, color = colorScheme.primary)
                    }
                }
            }
        }

        // Dialogs
        uiState.selectedUser?.let { user ->
            AbsenceDialog(
                user = user,
                absences = uiState.userAbsences[user.userId].orEmpty(),
                onDismiss = { viewModel.clearSelectedUser() }
            )
        }

        if (uiState.showInvitationDialog && uiState.invitationCode != null) {
            InvitationCodeDialog(
                invitationCode = uiState.invitationCode!!,
                onDismiss = viewModel::hideInvitationDialog
            )
        }

        userToRemove?.let { user ->
            ConfirmRemoveDialog(
                user = user,
                onConfirm = {
                    viewModel.removeUserFromWG(user.userId)
                    userToRemove = null
                },
                onDismiss = { userToRemove = null }
            )
        }
        if (navShield) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    }
            )
        }
    }
}

@Composable
fun WGProfileCard(
    user: User,
    displayName: String,
    isOnline: Boolean,
    isEditing: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(PROFILE_CARD_HEIGHT)
            .background(Color.Transparent, RoundedCornerShape(PROFILE_CARD_SPACING_DP))
            .border(
                PROFILE_CARD_BORDER_WIDTH,
                colorScheme.outline,
                RoundedCornerShape(PROFILE_CARD_BORDER_RADIUS)
            )
            .clickable { onClick() }
            .padding(
                horizontal = PROFILE_CARD_HORIZONTAL_PADDING,
                vertical =
                PROFILE_CARD_VERTICAL_PADDING
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = if (!user.profilePicture.isNullOrBlank()) {
                rememberAsyncImagePainter(model = user.profilePicture)
            } else {
                painterResource(id = R.drawable.ic_default_user_profile)
            },
            contentDescription = PROFILE_IMAGE_DESC,
            modifier = Modifier
                .size(PROFILE_IMAGE_SIZE_SMALL)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            colorFilter = if (user.profilePicture.isNullOrBlank()) {
                ColorFilter.tint(colorScheme.secondary)
            } else {
                null
            }
        )

        Spacer(modifier = Modifier.width(PROFILE_CARD_HORIZONTAL_PADDING))

        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = TEXT_SIZE_MEDIUM),
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(STATUS_DOT_SIZE)
                .clip(CircleShape)
                .background(if (isOnline) colorScheme.tertiary else colorScheme.error)
        )

        if (isEditing && showRemove) {
            Spacer(modifier = Modifier.width(REMOVE_ICON_SPACER))
            Icon(
                imageVector = Icons.Default.RemoveCircle,
                contentDescription = REMOVE_USER_DESC,
                tint = colorScheme.error,
                modifier = Modifier.clickable { onRemove() }
            )
        }
    }
}

@Composable
fun AbsenceDialog(user: User, absences: List<Absence>, onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val today = LocalDate.now()
    val currentAbsence = absences.find { it.startDate <= today && today <= it.endDate }
    val upcomingAbsences = absences.filter { it.startDate > today }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(colorScheme.secondaryContainer, RoundedCornerShape(DIALOG_CORNER_RADIUS))
                .border(DIALOG_BORDER_WIDTH, colorScheme.primary, RoundedCornerShape(DIALOG_CORNER_RADIUS))
                .padding(DIALOG_OUTER_PADDING)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = DIALOG_CLOSE_DESC, tint = colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(SPACER_8))

                Icon(
                    painter = painterResource(id = R.drawable.ic_default_user_profile),
                    contentDescription = USER_ICON_DESC,
                    modifier = Modifier
                        .size(AVATAR_SIZE)
                        .clip(CircleShape),
                    tint = colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(SPACER_8))

                Text(
                    text = user.displayName,
                    fontSize = TEXT_SIZE_XL,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSecondaryContainer
                )

                currentAbsence?.let {
                    Spacer(modifier = Modifier.height(SPACER_4))
                    Text(
                        text = "$ABSENCE_LABEL ${it.endDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))}",
                        color = colorScheme.tertiary,
                        fontSize = TEXT_SIZE_SMALL
                    )
                }

                Spacer(modifier = Modifier.height(SPACER_16))

                if (upcomingAbsences.isNotEmpty()) {
                    Text(
                        text = UPCOMING_ABSENCES_LABEL,
                        fontWeight = FontWeight.Medium,
                        fontSize = TEXT_SIZE_MEDIUM,
                        modifier = Modifier.align(Alignment.Start),
                        color = colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(SPACER_8))

                    Column(modifier = Modifier.align(Alignment.Start)) {
                        upcomingAbsences.forEach {
                            Text(
                                text = "• ${it.startDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))}–" +
                                    it.endDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)),
                                fontSize = TEXT_SIZE_SMALL,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        NO_ABSENCES_TEXT,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = TEXT_SIZE_SMALL
                    )
                }
            }
        }
    }
}

@Composable
fun InvitationCodeDialog(invitationCode: String, onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(colorScheme.surface, RoundedCornerShape(PROFILE_CARD_SPACING_DP))
                .padding(DIALOG_INNER_PADDING)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    INVITATION_TITLE,
                    fontSize = TEXT_SIZE_LARGE,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(SPACER_12))
                SelectionContainer {
                    Text(invitationCode, fontSize = TEXT_SIZE_XL, color = colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(SPACER_16))
                Button(onClick = onDismiss) {
                    Text(INVITATION_CLOSE_TEXT)
                }
            }
        }
    }
}

@Composable
fun ConfirmRemoveDialog(user: User, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(REMOVE_DIALOG_TITLE, color = colorScheme.onSurface) },
        text = {
            Text(
                "$REMOVE_DIALOG_TEXT_PREFIX${user.displayName}$REMOVE_DIALOG_TEXT_SUFFIX",
                color = colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(REMOVE_DIALOG_CONFIRM, color = colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    REMOVE_DIALOG_CANCEL,
                    color = colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        containerColor = colorScheme.surface
    )
}
