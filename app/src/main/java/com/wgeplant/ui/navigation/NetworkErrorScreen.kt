package com.wgeplant.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wgeplant.ui.theme.WGeplantTheme

object NetworkErrorScreenConstants {
    const val ERROR_ICON = "Error Icon"
    const val FAILED_INITIALISATION = "Netzwerkfehler!"
    const val CHECK_INTERNET = "Überprüfe deine Internetverbindung."
}

/**
 * Composable function representing the screen displaying a network error.
 *
 * @param navController NavController used for navigation within the app.
 * @param networkErrorViewModel ViewModel associated with the network error screen.
 * @param networkState Nullable Boolean indicating the current network state.
 * @param previousRoute Nullable String representing the previous route before a network error occurred.
 */
@Composable
fun NetworkErrorScreen(
    navController: NavController,
    networkErrorViewModel: NetworkErrorViewModel = hiltViewModel(),
    networkState: Boolean?,
    previousRoute: String?
) {
    val errorMessage by networkErrorViewModel.errorMessage.collectAsState()
    val isLoading by networkErrorViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = true) {
        (context as? ComponentActivity)?.finish()
    }

    LaunchedEffect(networkState) {
        if (networkState == true && !previousRoute.isNullOrEmpty()) {
            networkErrorViewModel.synchronizeData()
        }
    }

    LaunchedEffect(isLoading, errorMessage) {
        if (!isLoading && errorMessage == null && !previousRoute.isNullOrEmpty()) {
            navController.navigate(previousRoute) {
                popUpTo(Routes.NETWORK_ERROR) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = NetworkErrorScreenConstants.ERROR_ICON,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = NetworkErrorScreenConstants.FAILED_INITIALISATION,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = NetworkErrorScreenConstants.CHECK_INTERNET,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NetworkErrorScreenPreview() {
    WGeplantTheme {
        NetworkErrorScreen(
            navController = NavController(LocalContext.current),
            networkState = true,
            previousRoute = null
        )
    }
}
