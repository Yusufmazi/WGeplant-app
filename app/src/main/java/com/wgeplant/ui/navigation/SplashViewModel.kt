package com.wgeplant.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the splash screen logic and initial app setup.
 *
 * This ViewModel checks the user's login status, membership in a WG,
 * and loads the initial data required for the app to function properly.
 * It exposes app readiness, login state, membership state, and error states as [StateFlow]s.
 *
 * @property getInitialDataInteractor Interactor that provides initial data and user status checks.
 * @property manageDeviceInteractor Interactor that manages device-related operations.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getInitialDataInteractor: GetInitialDataInteractor,
    private val manageDeviceInteractor: ManageDeviceInteractor
) : ViewModel() {

    /**
     * StateFlow representing whether the app is fully initialized and ready.
     */
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()

    /**
     * StateFlow representing the user's login status.
     * - `true` if logged in,
     * - `false` if not logged in,
     * - `null` if unknown/undetermined yet.
     */
    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn.asStateFlow()

    /**
     * StateFlow representing whether the logged-in user is a member of a WG.
     * - `true` if user is member,
     * - `false` if not,
     * - `null` if unknown or user not logged in.
     */
    private val _isUserMember = MutableStateFlow<Boolean?>(null)
    val isUserMember: StateFlow<Boolean?> = _isUserMember.asStateFlow()

    /**
     * StateFlow holding any error messages that occur during initialization.
     * Null if no error.
     */
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    /**
     * StateFlow representing whether the user has a network connection or not.
     * - `true` if user has a network connection,
     * - `false` if not,
     * - `null` if unknown.
     */
    private val _networkStatus = MutableStateFlow<Boolean?>(null)

    companion object {
        const val UNEXPECTED_ERROR = "Unerwarteter Fehler"
        const val NO_CONNECTION = "Keine Internetverbindung vorhanden."
    }

    init {
        startAppInitialization()

        viewModelScope.launch {
            manageDeviceInteractor.getNetworkConnectionStatus().collect { status ->
                _networkStatus.value = status
                if (status && !_isAppReady.value) {
                    if (_errorState.value == NO_CONNECTION) {
                        startAppInitialization()
                    }
                }
            }
        }
    }

    /**
     * Starts the app initialization process.
     *
     * This method performs the following steps:
     * 1. Checks the current network connection status and stops if no connection is available.
     * 2. Checks if the user is logged in.
     * 3. If logged in, loads initial app data.
     * 4. Checks if the user is a member of a WG.
     * 5. Updates the corresponding state flows to reflect readiness, login status,
     *    membership, and any errors encountered.
     *
     * This is launched in the ViewModel's scope asynchronously.
     */
    fun startAppInitialization() {
        viewModelScope.launch {
            _isAppReady.value = false
            _errorState.value = null

            val currentNetworkConnected = manageDeviceInteractor.getNetworkConnectionStatus().first()
            _networkStatus.value = currentNetworkConnected

            if (!currentNetworkConnected) {
                _errorState.value = NO_CONNECTION
                return@launch
            }

            try {
                when (val loginResult = getInitialDataInteractor.isUserLoggedIn().first()) {
                    is Result.Success -> {
                        val isLoggedIn = loginResult.data
                        _isUserLoggedIn.value = isLoggedIn

                        if (isLoggedIn) {
                            when (val initialResult = getInitialDataInteractor.execute()) {
                                is Result.Success -> {
                                    when (val wgResult = getInitialDataInteractor.isUserInWG().first()) {
                                        is Result.Success -> {
                                            _isUserMember.value = wgResult.data
                                            _isAppReady.value = true
                                        }
                                        is Result.Error -> {
                                            _errorState.value = wgResult.error.message
                                        }
                                    }
                                }
                                is Result.Error -> {
                                    _errorState.value = initialResult.error.message
                                }
                            }
                        } else {
                            _isAppReady.value = true
                        }
                    }
                    is Result.Error -> {
                        _errorState.value = loginResult.error.message
                    }
                }
            } catch (e: Exception) {
                _errorState.value = UNEXPECTED_ERROR
            }
        }
    }
}
