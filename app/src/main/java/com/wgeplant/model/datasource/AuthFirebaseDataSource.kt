package com.wgeplant.model.datasource

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * This interface is responsible for communicating with firebase authentication.
 */
interface AuthFirebaseDataSource {
    /**
     * This method sends firebase authentication a request to sign in the current user.
     * The idToken is returned when the process was successful.
     * @param email of the current user
     * @param password of the current user
     */
    suspend fun loginWithEmailAndPassword(email: String, password: String):
        Result<String, DomainError>

    /**
     * This method sends firebase authentication a request to register a new user.
     * It returns the idToken when it was successful.
     * @param email of the new user
     * @param password of the new user
     */
    suspend fun registerNewUser(email: String, password: String):
        Result<String, DomainError>

    /**
     * This method sends firebase authentication a request to logout the current user.
     */
    suspend fun logout(): Result<Unit, DomainError>

    /**
     * This method sends firebase authentication a request to delete the account of the user.
     */
    suspend fun deleteAccount(): Result<Unit, DomainError>

    /**
     * This method sends firebase authentication a request to get the uid of the current user.
     */
    suspend fun getCurrentUserId(): Result<String, DomainError>

    /**
     * This method returns a unit if the current user is logged in Firebase Authentication.
     */
    suspend fun isLoggedIn(): Result<String, DomainError>

    /**
     * This method uses the callback Flow from Firebase to get the authentication state of the
     * current user.
     */
    fun getAuthStateFlow(): Flow<Result<Boolean, DomainError>>

    /**
     * This method reloads the current users authentication state from firebase authentication.
     */
    suspend fun reloadCurrentUser(): Result<Unit, DomainError>
}
