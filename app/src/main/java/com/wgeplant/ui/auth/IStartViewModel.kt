package com.wgeplant.ui.auth

import androidx.navigation.NavController

/**
 * Interface for StartViewModel, responsible for navigation to Login and Register screens.
 */
interface IStartViewModel {
    /**
     * Navigates to the login screen.
     * @param navController The NavController used for navigation.
     */
    fun navigateToLogin(navController: NavController)

    /**
     * Navigates to the registration screen.
     * @param navController The NavController used for navigation.
     */
    fun navigateToRegister(navController: NavController)
}
