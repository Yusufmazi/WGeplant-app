package com.wgeplant.ui.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

object RegisterScreenConstants {
    const val LABEL_EMAIL = "E-Mail"
    const val LABEL_PASSWORD = "Passwort"
    const val LABEL_DISPLAY_NAME = "Anzeigename"
    const val BUTTON_REGISTER = "Registrieren"
    const val DESCRIPTION_BACK = "Zurück"
    const val DESCRIPTION_PASSWORD_TOGGLE = "Passwort anzeigen/verbergen"
    const val DESCRIPTION_LOGO = "WGeplant Logo"
    const val APP_TITLE_PART1 = "WG"
    const val APP_TITLE_PART2 = "eplant"
    const val LOADING_INDICATOR_TEST_TAG = "loading_indicator"
    const val REGISTER_BUTTON_TEST_TAG = "register_button"
    const val MAX_DISPLAY_NAME_LENGTH = 15
}

/**
 * Composable function that displays the registration screen.
 *
 * It collects UI state, error messages, and loading state from the [IRegisterViewModel]
 * and passes them along with event handlers to [RegisterScreenContent].
 *
 * @param navController The NavController used for navigation actions.
 * @param registerViewModel The ViewModel implementing registration logic.
 */
@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: IRegisterViewModel = hiltViewModel()
) {
    val uiState by registerViewModel.uiState.collectAsState()
    val errorMessage by registerViewModel.errorMessage.collectAsState()
    val isLoading by registerViewModel.isLoading.collectAsState()

    RegisterScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        goBack = registerViewModel::navigateBack,
        onEmailChanged = registerViewModel::onEmailChanged,
        onPasswordChanged = registerViewModel::onPasswordChanged,
        onPasswordVisibilityChanged = registerViewModel::onPasswordVisibilityChanged,
        onDisplayNameChanged = registerViewModel::onDisplayNameChanged,
        register = registerViewModel::register
    )
}

/**
 * Private composable function that renders the content of the registration screen.
 *
 * Displays input fields for email, password, display name, error messages,
 * a loading indicator, and handles user interactions like input changes and registration action.
 *
 * @param uiState The current state of the registration form.
 * @param errorMessage An optional error message to display.
 * @param isLoading Flag indicating if a registration process is ongoing.
 * @param navController The NavController for navigation.
 * @param goBack Lambda to navigate back.
 * @param onEmailChanged Lambda called when email input changes.
 * @param onPasswordChanged Lambda called when password input changes.
 * @param onPasswordVisibilityChanged Lambda called to toggle password visibility.
 * @param onDisplayNameChanged Lambda called when display name input changes.
 * @param register Lambda to trigger the registration process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreenContent(
    uiState: RegisterUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    goBack: (navController: NavController) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    register: (navController: NavController) -> Unit
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
                            contentDescription = RegisterScreenConstants.DESCRIPTION_BACK,
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.wgeplant_logo),
                contentDescription = RegisterScreenConstants.DESCRIPTION_LOGO,
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(RegisterScreenConstants.APP_TITLE_PART1)
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(RegisterScreenConstants.APP_TITLE_PART2)
                    }
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = { Text(RegisterScreenConstants.LABEL_EMAIL, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = { Text(RegisterScreenConstants.LABEL_PASSWORD, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    val image = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = onPasswordVisibilityChanged) {
                        Icon(
                            imageVector = image,
                            contentDescription = RegisterScreenConstants.DESCRIPTION_PASSWORD_TOGGLE
                        )
                    }
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = onDisplayNameChanged,
                label = {
                    Text(
                        text = RegisterScreenConstants.LABEL_DISPLAY_NAME,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                isError = uiState.displayNameError != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.displayNameError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            text = "${uiState.displayName.length}/${RegisterScreenConstants.MAX_DISPLAY_NAME_LENGTH}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.displayName.length >
                                    RegisterScreenConstants.MAX_DISPLAY_NAME_LENGTH
                                ) {
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
                onClick = { register(navController) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).testTag(
                    RegisterScreenConstants.REGISTER_BUTTON_TEST_TAG
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
                        modifier = Modifier.size(24.dp).testTag(RegisterScreenConstants.LOADING_INDICATOR_TEST_TAG),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = RegisterScreenConstants.BUTTON_REGISTER,
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

@Preview(showBackground = true, showSystemUi = true, name = "Register Screen")
@Composable
fun RegisterScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        RegisterScreenContent(
            uiState = RegisterUiState(),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            goBack = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onPasswordVisibilityChanged = {},
            onDisplayNameChanged = {},
            register = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Register Screen")
@Composable
fun RegisterScreenInvalidPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        RegisterScreenContent(
            uiState = RegisterUiState(isValid = false, emailError = "Fülle dieses Feld aus."),
            errorMessage = null,
            isLoading = false,
            navController = previewNavController,
            goBack = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onPasswordVisibilityChanged = {},
            onDisplayNameChanged = {},
            register = {}
        )
    }
}
