package com.wgeplant.ui.auth

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wgeplant.R

private val SCREEN_HORIZONTAL_PADDING = 24.dp
private val TOP_SPACER_HEIGHT = 24.dp
private val SMALL_SPACER_HEIGHT = 4.dp
private val FORM_BOTTOM_SPACER = 16.dp
private val LOGO_SIZE = 200.dp
private val BUTTON_MIN_HEIGHT = 56.dp
private val BUTTON_CORNER_RADIUS = 10.dp
private val ICON_BUTTON_SIZE = 40.dp
private val TEXT_FIELD_CORNER_RADIUS = 10.dp
private val LOADING_INDICATOR_SIZE = 24.dp
private val LOADING_INDICATOR_STROKE = 2.dp

private const val BACK_BUTTON_DESC = "Zurück"
private const val LOGO_DESC = "WGeplant Logo"
private const val EMAIL_LABEL = "E-Mail"
private const val PASSWORD_LABEL = "Passwort"
private const val PASSWORD_TOGGLE_DESC = "Passwort anzeigen/verbergen"
private const val LOGIN_BUTTON_TEXT = "Login"
private const val WG = "WG"
private const val EPLANT = "eplant"

/**
 * Composable entry point for the LoginScreen.
 * Connects to the ViewModel and renders the login UI.
 *
 * @param navController Used for navigation after login
 * @param loginViewModel ViewModel interface handling login state and actions
 */
@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: ILoginViewModel = hiltViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()

    LoginScreenContent(
        uiState = uiState,
        errorMessage = errorMessage,
        isLoading = isLoading,
        navController = navController,
        goBack = loginViewModel::navigateBack,
        onEmailChanged = loginViewModel::onEmailChanged,
        onPasswordChanged = loginViewModel::onPasswordChanged,
        onPasswordVisibilityChanged = loginViewModel::onPasswordVisibilityChanged,
        login = loginViewModel::login
    )
}

/**
 * Composable that defines the login screen content including:
 * - Logo and branding
 * - Email/password form
 * - Login button
 * - Error handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    errorMessage: String?,
    isLoading: Boolean,
    navController: NavController,
    goBack: (NavController) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    login: (NavController) -> Unit
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
                            contentDescription = BACK_BUTTON_DESC,
                            modifier = Modifier.size(ICON_BUTTON_SIZE),
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
                .padding(horizontal = SCREEN_HORIZONTAL_PADDING)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(TOP_SPACER_HEIGHT))

            Image(
                painter = painterResource(id = R.drawable.wgeplant_logo),
                contentDescription = LOGO_DESC,
                modifier = Modifier.size(LOGO_SIZE)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(WG)
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(EPLANT)
                    }
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(TOP_SPACER_HEIGHT))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = { Text(EMAIL_LABEL, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(TEXT_FIELD_CORNER_RADIUS)
            )

            Spacer(Modifier.height(SMALL_SPACER_HEIGHT))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = { Text(PASSWORD_LABEL, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    val icon = if (uiState.isPasswordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }
                    IconButton(onClick = onPasswordVisibilityChanged) {
                        Icon(icon, contentDescription = PASSWORD_TOGGLE_DESC)
                    }
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(TEXT_FIELD_CORNER_RADIUS)
            )

            Spacer(Modifier.height(FORM_BOTTOM_SPACER))

            Button(
                onClick = { login(navController) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = BUTTON_MIN_HEIGHT),
                enabled = !isLoading,
                shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(LOADING_INDICATOR_SIZE),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = LOADING_INDICATOR_STROKE
                    )
                } else {
                    Text(LOGIN_BUTTON_TEXT, style = MaterialTheme.typography.bodyMedium)
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = FORM_BOTTOM_SPACER),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
