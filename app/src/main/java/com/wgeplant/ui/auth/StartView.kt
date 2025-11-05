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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wgeplant.R
import com.wgeplant.ui.theme.WGeplantTheme

object StartScreenConstants {
    const val DESCRIPTION_LOGO = "WGeplant Logo"
    const val BUTTON_LOGIN = "Anmelden"
    const val BUTTON_REGISTER = "Registrieren"
    const val APP_TITLE_PART1 = "WG"
    const val APP_TITLE_PART2 = "eplant"
}

/**
 * Composable function to display the start screen with Login and Register options.
 *
 * @param navController The NavController to handle navigation events.
 * @param startViewModel The ViewModel handling navigation logic.
 */
@Composable
fun StartScreen(
    navController: NavController,
    startViewModel: IStartViewModel = hiltViewModel()
) {
    StartScreenContent(
        onLoginClicked = startViewModel::navigateToLogin,
        onRegisterClicked = startViewModel::navigateToRegister,
        navController = navController
    )
}

/**
 * Private composable rendering the actual content of the start screen.
 *
 * @param navController The NavController to be passed on click events.
 * @param onLoginClicked Lambda called when the login button is clicked.
 * @param onRegisterClicked Lambda called when the register button is clicked.
 */
@Composable
private fun StartScreenContent(
    navController: NavController,
    onLoginClicked: (navController: NavController) -> Unit,
    onRegisterClicked: (navController: NavController) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(80.dp))

            Image(
                painter = painterResource(id = R.drawable.wgeplant_logo),
                contentDescription = StartScreenConstants.DESCRIPTION_LOGO,
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(StartScreenConstants.APP_TITLE_PART1)
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(StartScreenConstants.APP_TITLE_PART2)
                    }
                },
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(56.dp))

            Button(
                onClick = { onLoginClicked(navController) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                enabled = true,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = StartScreenConstants.BUTTON_LOGIN,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onRegisterClicked(navController) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                enabled = true,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = StartScreenConstants.BUTTON_REGISTER,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Start Screen")
@Composable
fun StartScreenPreview() {
    WGeplantTheme {
        val previewNavController = rememberNavController()

        StartScreenContent(
            onLoginClicked = {},
            onRegisterClicked = {},
            navController = previewNavController
        )
    }
}
