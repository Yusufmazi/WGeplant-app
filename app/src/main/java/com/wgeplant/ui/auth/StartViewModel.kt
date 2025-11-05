package com.wgeplant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel implementation of [IStartViewModel] which handles user navigation
 * actions from the start screen to login and registration screens.
 */
@HiltViewModel
class StartViewModel @Inject constructor() : ViewModel(), IStartViewModel {

    override fun navigateToLogin(navController: NavController) {
        navController.navigate(Routes.LOGIN)
    }

    override fun navigateToRegister(navController: NavController) {
        navController.navigate(Routes.REGISTER)
    }
}
