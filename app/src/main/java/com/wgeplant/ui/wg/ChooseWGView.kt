package com.wgeplant.ui.wg

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wgeplant.R
import com.wgeplant.ui.theme.WGeplantTheme

object ChooseWGScreenConstants {
    const val GREETING = "Hallo "
    const val SMILEY = " :)"
    const val CHOOSE_OPTION_TEXT = "Willst du ..."
    const val JOIN_WG = "Einer WG beitreten"
    const val CREATE_WG = "Eine neue WG erstellen"
    const val STANDARD_PROFILE_ICON = "Standard Profil-Icon"
    const val USER_PROFILE_PICTURE = "Nutzerprofilbild"
}

/**
 * Top-level composable for the Choose WG screen.
 *
 * Responsible for collecting state from the [IChooseWGViewModel] and passing it
 * to the content composable. Also injects the ViewModel via Hilt.
 *
 * @param navController Navigation controller to handle navigation events.
 * @param chooseWGViewModel ViewModel interface for the screen..
 */
@Composable
fun ChooseWGScreen(
    navController: NavController,
    chooseWGViewModel: IChooseWGViewModel = hiltViewModel()
) {
    val uiState by chooseWGViewModel.uiState.collectAsState()
    val errorMessage by chooseWGViewModel.errorMessage.collectAsState()

    ChooseWGScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        navController = navController,
        onCreateWGClicked = chooseWGViewModel::navigateToCreateWG,
        onJoinWGClicked = chooseWGViewModel::navigateToJoinWG,
        onUserProfileClicked = chooseWGViewModel::navigateToUserProfile
    )
}

/**
 * Displays the UI for choosing whether to create or join a WG.
 *
 * Shows a personalized greeting, two action buttons, and the user profile icon.
 * Handles navigation and displays errors if present.
 *
 * @param uiState UI state containing user display name and profile image.
 * @param errorMessage Optional error message to show to the user.
 * @param navController Navigation controller to manage navigation.
 * @param onCreateWGClicked Lambda to invoke when the "Create WG" button is pressed. Receives the [NavController].
 * @param onJoinWGClicked Lambda to invoke when the "Join WG" button is pressed. Receives the [NavController].
 * @param onUserProfileClicked Lambda to invoke when the profile icon is clicked. Receives the [NavController].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChooseWGScreenContent(
    uiState: ChooseWGUiState,
    errorMessage: String?,
    navController: NavController,
    onCreateWGClicked: (navController: NavController) -> Unit,
    onJoinWGClicked: (navController: NavController) -> Unit,
    onUserProfileClicked: (navController: NavController) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    UserProfileIcon(
                        profileImageUrl = uiState.userProfileImageUrl,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onUserProfileClicked(navController) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(200.dp))

            Text(
                text = buildString {
                    append(ChooseWGScreenConstants.GREETING)
                    append(uiState.userDisplayName)
                    append(ChooseWGScreenConstants.SMILEY)
                },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(80.dp))

            Text(
                text = ChooseWGScreenConstants.CHOOSE_OPTION_TEXT,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 24.sp
                )
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onJoinWGClicked(navController) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                enabled = true,
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = ChooseWGScreenConstants.JOIN_WG,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onCreateWGClicked(navController) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                enabled = true,
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = ChooseWGScreenConstants.CREATE_WG,
                    style = MaterialTheme.typography.bodyMedium
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

/**
 * Displays a circular user profile image or a default icon if unavailable.
 *
 * Attempts to load the image from [profileImageUrl]; if loading fails or the URL is null/blank,
 * a fallback icon is displayed instead.
 *
 * @param profileImageUrl URL to the user's profile image. Can be null or blank.
 * @param modifier Modifier to apply to the container box.
 * @param iconSize Size of the icon box (default is 64.dp).
 */
@Composable
fun UserProfileIcon(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
) {
    Box(
        modifier = modifier
            .size(iconSize)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!profileImageUrl.isNullOrBlank()) {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profileImageUrl)
                    .crossfade(true)
                    .build()
            )

            val painterState = painter.state

            if (painterState is AsyncImagePainter.State.Loading || painterState is AsyncImagePainter.State.Error) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_default_user_profile),
                    contentDescription = ChooseWGScreenConstants.STANDARD_PROFILE_ICON,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.secondary
                )
            } else {
                Image(
                    painter = painter,
                    contentDescription = ChooseWGScreenConstants.USER_PROFILE_PICTURE,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_default_user_profile),
                contentDescription = ChooseWGScreenConstants.STANDARD_PROFILE_ICON,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Choose WG Screen")
@Composable
fun ChooseWGScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        ChooseWGScreenContent(
            uiState = ChooseWGUiState(userDisplayName = "Max"),
            errorMessage = null,
            navController = previewNavController,
            onCreateWGClicked = {},
            onJoinWGClicked = {},
            onUserProfileClicked = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Choose WG Screen")
@Composable
fun ChooseWGScreenPlaceholderPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        ChooseWGScreenContent(
            uiState = ChooseWGUiState(
                userDisplayName = "Max",
                userProfileImageUrl = "https://example.com/nonexistent.jpg"
            ),
            errorMessage = null,
            navController = previewNavController,
            onCreateWGClicked = {},
            onJoinWGClicked = {},
            onUserProfileClicked = {}
        )
    }
}
