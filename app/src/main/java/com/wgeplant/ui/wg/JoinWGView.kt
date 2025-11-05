package com.wgeplant.ui.wg

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wgeplant.R
import com.wgeplant.ui.theme.WGeplantTheme
object JoinWGScreenConstants {
    const val BACK = "Zurück"
    const val APP_LOGO = "WGeplant Logo"
    const val APP_TITLE_PART1 = "WG"
    const val APP_TITLE_PART2 = "eplant"
    const val INVITATION_CODE = "Einladungscode"
    const val JOIN_WG = "WG beitreten"
    const val LOADING_INDICATOR_TEST_TAG = "loading_indicator"
    const val JOIN_WG_BUTTON_TEST_TAG = "join_wg_button"
}

/**
 * Composable function that hosts the Join WG screen and connects it to the ViewModel.
 *
 * Collects UI state, error messages, and loading state from the ViewModel and passes them
 * down to [JoinWGScreenContent]. Also passes navigation and event callbacks.
 *
 * @param navController Controller used for navigation within the app.
 * @param joinWGViewModel ViewModel interface providing state and event handlers.
 */
@Composable
fun JoinWGScreen(
    navController: NavController,
    joinWGViewModel: IJoinWGViewModel = hiltViewModel()
) {
    val uiState by joinWGViewModel.uiState.collectAsState()
    val errorMessage by joinWGViewModel.errorMessage.collectAsState()
    val isLoading by joinWGViewModel.isLoading.collectAsState()

    JoinWGScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        goBack = joinWGViewModel::navigateBack,
        onInvitationCodeChanged = joinWGViewModel::onInvitationCodeChanged,
        joinWG = joinWGViewModel::joinWG
    )
}

/**
 * Renders the UI content for the Join WG screen.
 *
 * The screen allows the user to input an invitation code, validates the input,
 * and triggers the joining process. It also includes a back button,
 * loading indicator, and error message display.
 *
 * @param uiState Holds the current state of the screen, including the invitation code and validation errors.
 * @param errorMessage Optional error message to display below the action button.
 * @param isLoading Flag indicating whether a loading spinner should be shown inside the button.
 * @param navController Controller used for navigation within the app.
 * @param goBack Lambda invoked when the back icon is pressed.
 * @param onInvitationCodeChanged Lambda invoked when the invitation code input changes.
 * @param joinWG Lambda invoked when the "Join WG" button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinWGScreenContent(
    uiState: JoinWGUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    goBack: (navController: NavController) -> Unit,
    onInvitationCodeChanged: (String) -> Unit,
    joinWG: (navController: NavController) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { goBack(navController) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = JoinWGScreenConstants.BACK,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.wgeplant_logo),
                contentDescription = JoinWGScreenConstants.APP_LOGO,
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(JoinWGScreenConstants.APP_TITLE_PART1)
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(JoinWGScreenConstants.APP_TITLE_PART2)
                    }
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.invitationCode,
                onValueChange = onInvitationCodeChanged,
                label = { Text(JoinWGScreenConstants.INVITATION_CODE, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.invitationCodeError != null,
                supportingText = {
                    uiState.invitationCodeError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { joinWG(navController) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).testTag(
                    JoinWGScreenConstants.JOIN_WG_BUTTON_TEST_TAG
                ),
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).testTag(JoinWGScreenConstants.LOADING_INDICATOR_TEST_TAG),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = JoinWGScreenConstants.JOIN_WG,
                        style = MaterialTheme.typography.bodyMedium
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

@Preview(showBackground = true, showSystemUi = true, name = "Join WG Screen")
@Composable
fun JoinWGScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        JoinWGScreenContent(
            uiState = JoinWGUiState(),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            goBack = {},
            onInvitationCodeChanged = {},
            joinWG = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Join WG Screen")
@Composable
fun JoinWGScreenInvalidPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        JoinWGScreenContent(
            uiState = JoinWGUiState(
                isValid = false,
                invitationCodeError = "Ungültiges Format für den Code.",
                invitationCode = "abcdef"
            ),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            goBack = {},
            onInvitationCodeChanged = {},
            joinWG = {}
        )
    }
}
