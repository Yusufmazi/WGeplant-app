package com.wgeplant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Composable function representing the initial screen of the app.
 *
 * @param navController NavController used for navigation within the app.
 * @param initialLoginState Nullable Boolean indicating if the user is initially logged in.
 * @param initialMemberState Nullable Boolean indicating if the user is initially a member of a WG.
 */
@Composable
fun AppStartScreen(
    navController: NavController,
    initialLoginState: Boolean?,
    initialMemberState: Boolean?
) {
    LaunchedEffect(initialLoginState, initialMemberState) {
        if (initialLoginState != true) {
            navController.navigate(Routes.AUTH_START) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (initialMemberState == false) {
            navController.navigate(Routes.CHOOSE_WG) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(Routes.CALENDAR_GRAPH) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
