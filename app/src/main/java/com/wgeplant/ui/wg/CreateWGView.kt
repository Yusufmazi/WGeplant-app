package com.wgeplant.ui.wg

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wgeplant.R
import com.wgeplant.ui.theme.WGeplantTheme

object CreateWGScreenConstants {
    const val BACK = "Zurück"
    const val APP_LOGO = "WGeplant Logo"
    const val APP_TITLE_PART1 = "WG"
    const val APP_TITLE_PART2 = "eplant"
    const val WG_NAME = "WG-Name"
    const val CREATE_WG = "WG erstellen"
    const val LOADING_INDICATOR_TEST_TAG = "loading_indicator"
    const val CREATE_WG_BUTTON_TEST_TAG = "create_wg_button"
    const val MAX_DISPLAY_NAME_LENGTH = 15
}

/**
 * Composable entry point for the Create WG screen.
 *
 * Collects state from the provided [ICreateWGViewModel] and delegates the UI rendering
 * to [CreateWGScreenContent]. Uses Hilt to obtain the default ViewModel instance.
 *
 * @param navController The [NavController] used to handle navigation actions.
 * @param createWGViewModel The ViewModel implementation
 */
@Composable
fun CreateWGScreen(
    navController: NavController,
    createWGViewModel: ICreateWGViewModel = hiltViewModel()
) {
    val uiState by createWGViewModel.uiState.collectAsState()
    val errorMessage by createWGViewModel.errorMessage.collectAsState()
    val isLoading by createWGViewModel.isLoading.collectAsState()

    CreateWGScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        goBack = createWGViewModel::navigateBack,
        onWGNameChanged = createWGViewModel::onWGNameChanged,
        createWG = createWGViewModel::createWG
    )
}

/**
 * Renders the UI content for the Create WG screen.
 *
 * The screen allows the user to input a WG name, validate the input,
 * and trigger the creation process. It also includes a back button,
 * loading indicator, and error message display.
 *
 * @param uiState Holds the current state of the screen, including the WG name and validation errors.
 * @param errorMessage Optional error message to display below the action button.
 * @param isLoading Flag indicating whether a loading spinner should be shown inside the button.
 * @param navController Controller used for navigation within the app.
 * @param goBack Lambda to invoke when the back icon is pressed.
 * @param onWGNameChanged Lambda to invoke when the WG name input text changes.
 * @param createWG Lambda to invoke when the "Create WG" button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateWGScreenContent(
    uiState: CreateWGUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    goBack: (navController: NavController) -> Unit,
    onWGNameChanged: (String) -> Unit,
    createWG: (navController: NavController) -> Unit
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
                            contentDescription = CreateWGScreenConstants.BACK,
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
                contentDescription = CreateWGScreenConstants.APP_LOGO,
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(CreateWGScreenConstants.APP_TITLE_PART1)
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(CreateWGScreenConstants.APP_TITLE_PART2)
                    }
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.wgName,
                onValueChange = onWGNameChanged,
                label = { Text(CreateWGScreenConstants.WG_NAME, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.wgNameError != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.wgNameError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = "${uiState.wgName.length}/${CreateWGScreenConstants.MAX_DISPLAY_NAME_LENGTH}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.wgName.length > CreateWGScreenConstants.MAX_DISPLAY_NAME_LENGTH) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontSize = 12.sp
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { createWG(navController) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).testTag(
                    CreateWGScreenConstants.CREATE_WG_BUTTON_TEST_TAG
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
                        modifier = Modifier.size(24.dp).testTag(CreateWGScreenConstants.LOADING_INDICATOR_TEST_TAG),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = CreateWGScreenConstants.CREATE_WG,
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

@Preview(showBackground = true, showSystemUi = true, name = "Create WG Screen")
@Composable
fun CreateWGScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        CreateWGScreenContent(
            uiState = CreateWGUiState(),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            goBack = {},
            onWGNameChanged = {},
            createWG = {}
        )
    }
}
