package com.wgeplant.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.wgeplant.model.domain.DomainError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Abstract base ViewModel class providing common state management and error handling.
 *
 * Manages:
 * - UI state of type [T] as a [StateFlow].
 * - Error messages as a nullable [String] StateFlow.
 * - Loading state as a [Boolean] StateFlow.
 *
 * Also provides utility functions for:
 * - Updating UI state via a lambda updater function.
 * - Showing and clearing error messages.
 * - Setting loading state.
 * - Navigating back via [NavController].
 * - Handling domain errors by displaying error messages.
 *
 * @param T The type representing the UI state.
 * @param M The type of the model or interactor used by the ViewModel.
 * @property model The model or interactor instance used for business logic.
 * @property uiState The current UI state exposed as a read-only StateFlow.
 * @property errorMessage The current error message exposed as a read-only StateFlow.
 * @property isLoading The loading state exposed as a read-only StateFlow.
 */
abstract class BaseViewModel<T, M>(
    initialUiState: T,
    protected val model: M
) : ViewModel() {

    companion object {
        const val UNEXPECTED_ERROR = "Ein unerwarteter Fehler ist aufgetreten."
    }

    private val _uiState = MutableStateFlow(initialUiState)

    /**
     * Read-only StateFlow holding the current UI state.
     */
    val uiState: StateFlow<T> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    /**
     * Read-only StateFlow holding the current error message, or null if none.
     */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    /**
     * Read-only StateFlow indicating whether a loading operation is in progress.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Updates the current UI state using the provided lambda [updater].
     *
     * @param updater A function that takes the current state and returns the new state.
     */
    protected fun updateUiState(updater: (T) -> T) {
        _uiState.update(updater)
    }

    /**
     * Shows an error message by setting [_errorMessage].
     *
     * @param message The error message to show.
     */
    fun showError(message: String) {
        _errorMessage.value = message
    }

    /**
     * Clears any current error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Sets the loading state.
     *
     * @param isLoading True if loading is in progress, false otherwise.
     */
    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    /**
     * Navigates back in the navigation stack.
     *
     * @param navController The NavController to perform back navigation.
     */
    fun navigateBack(navController: NavController) {
        navController.popBackStack()
    }

    /**
     * Handles a domain error by displaying its message or a generic fallback message.
     *
     * @param error The [DomainError] instance to handle.
     */
    fun handleDomainError(error: DomainError) {
        error.message?.let { showError(it) } ?: showError(UNEXPECTED_ERROR)
    }
}
