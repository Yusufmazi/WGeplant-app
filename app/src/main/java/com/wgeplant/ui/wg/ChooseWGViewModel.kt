package com.wgeplant.ui.wg

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.interactor.userManagement.ManageUserProfileInteractor
import com.wgeplant.ui.BaseViewModel
import com.wgeplant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the UI state and navigation logic
 * of the Choose WG selection screen.
 *
 * This ViewModel observes the user's profile data to display the correct
 * user name and profile picture, and provides navigation methods to
 * related screens such as Create WG, Join WG, and User Profile.
 *
 * It uses a [ManageUserProfileInteractor] to retrieve user-related data
 * and handles error states and loading indicators accordingly.
 *
 * This ViewModel implements the [IChooseWGViewModel] interface, which defines
 * the navigation methods and the exposed UI state.
 *
 * @param model The interactor used to manage and observe user profile data.
 */
@HiltViewModel
class ChooseWGViewModel @Inject constructor(
    model: ManageUserProfileInteractor
) : BaseViewModel<ChooseWGUiState, ManageUserProfileInteractor>(initialUiState = ChooseWGUiState(), model),
    IChooseWGViewModel {

    companion object {
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
    }

    private var userDataObservationsJob: Job? = null

    init {
        observeUserData()
    }

    private fun observeUserData() {
        userDataObservationsJob?.cancel()

        userDataObservationsJob = viewModelScope.launch {
            setLoading(true)

            model.getUserData()
                .distinctUntilChanged()
                .catch {
                    showError(UNEXPECTED_ERROR)
                    updateUiState { it.copy(userDisplayName = "", userProfileImageUrl = null) }
                    setLoading(false)
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            clearError()
                            val user: User? = result.data
                            updateUiState {
                                it.copy(
                                    userDisplayName = user?.displayName ?: "",
                                    userProfileImageUrl = user?.profilePicture
                                )
                            }
                        }
                        is Result.Error -> {
                            handleDomainError(result.error)
                            updateUiState { it.copy(userDisplayName = "", userProfileImageUrl = null) }
                        }
                    }
                    setLoading(false)
                }
        }
    }

    override fun navigateToCreateWG(navController: NavController) {
        navController.navigate(Routes.CREATE_WG)
    }

    override fun navigateToJoinWG(navController: NavController) {
        navController.navigate(Routes.JOIN_WG)
    }

    override fun navigateToUserProfile(navController: NavController) {
        navController.navigate(Routes.PROFILE_USER)
    }
}
