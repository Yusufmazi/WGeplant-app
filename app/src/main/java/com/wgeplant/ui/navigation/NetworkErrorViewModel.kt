package com.wgeplant.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel class responsible for managing the state of the network error screen.
 *
 * @param getInitialDataInteractor Interactor for retrieving initial data.
 */
@HiltViewModel
class NetworkErrorViewModel @Inject constructor(
    private val getInitialDataInteractor: GetInitialDataInteractor
) : ViewModel() {

    /**
     * StateFlow holding any error messages that occur during the loading of the data.
     * Null if no error.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * StateFlow indicating whether a loading operation is in progress.
     */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    companion object {
        const val ERROR_CLOSE_THE_APP = "Ein Fehler ist aufgetreten, bitte schließe die App und öffne sie erneut."
    }

    /**
     * Synchronizes Data after a network error by calling the [GetInitialDataInteractor].
     */
    fun synchronizeData() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            try {
                when (val loginResult = getInitialDataInteractor.isUserLoggedIn().first()) {
                    is Result.Success -> {
                        val isLoggedIn = loginResult.data

                        if (isLoggedIn) {
                            when (getInitialDataInteractor.execute()) {
                                is Result.Success -> { _isLoading.value = false }
                                is Result.Error -> { _errorMessage.value = ERROR_CLOSE_THE_APP }
                            }
                        } else {
                            _isLoading.value = false
                        }
                    }
                    is Result.Error -> {
                        _errorMessage.value = ERROR_CLOSE_THE_APP
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = ERROR_CLOSE_THE_APP
            }
        }
    }
}
