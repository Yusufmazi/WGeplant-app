package com.wgeplant.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.ui.navigation.AppNavigation
import com.wgeplant.ui.navigation.SplashViewModel
import com.wgeplant.ui.theme.WGeplantTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity serves as the app's entry point and is responsible for:
 * - Showing a splash screen until the app is ready.
 * - Initializing edge-to-edge UI.
 * - Observing splashViewModel state to determine app readiness, user login status, and errors.
 * - Launching the main app navigation once initialization is successful.
 *
 * Dependencies:
 * - [SplashViewModel] for app readiness and error state.
 * - [GetInitialDataInteractor] injected for checking if the user is part of a WG.
 * - [ManageDeviceInteractor] injected for network connection status.
 *
 * Lifecycle:
 * - onCreate installs the splash screen and sets up Compose content with theming.
 * - Uses collected state flows to conditionally display screens.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var getInitialDataInteractor: GetInitialDataInteractor

    @Inject
    lateinit var manageDeviceInteractor: ManageDeviceInteractor

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !splashViewModel.isAppReady.value
            }
        }

        enableEdgeToEdge()
        setContent {
            WGeplantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isAppReady by splashViewModel.isAppReady.collectAsState()

                    val liveLoginResult by getInitialDataInteractor.isUserLoggedIn().collectAsState(initial = null)
                    val liveMemberResult by getInitialDataInteractor.isUserInWG().collectAsState(initial = null)
                    val liveNetworkResult by manageDeviceInteractor.getNetworkConnectionStatus().collectAsState(
                        initial = null
                    )

                    val initialLoginState by splashViewModel.isUserLoggedIn.collectAsState()
                    val initialMemberState by splashViewModel.isUserMember.collectAsState()

                    val loginState: Boolean? = run {
                        when (val currentLoginState = liveLoginResult) {
                            is Result.Success -> currentLoginState.data
                            is Result.Error -> {
                                false
                            }
                            null -> null
                        }
                    }

                    val memberState: Boolean? = run {
                        when (val currentMemberResult = liveMemberResult) {
                            is Result.Success -> if (loginState == true) currentMemberResult.data else null
                            is Result.Error -> {
                                null
                            }
                            null -> null
                        }
                    }

                    val networkState: Boolean? = liveNetworkResult

                    if (isAppReady) {
                        AppNavigation(
                            initialLoginState = initialLoginState,
                            initialMemberState = initialMemberState,
                            loginState = loginState,
                            memberState = memberState,
                            networkState = networkState
                        )
                    }
                }
            }
        }
    }
}
