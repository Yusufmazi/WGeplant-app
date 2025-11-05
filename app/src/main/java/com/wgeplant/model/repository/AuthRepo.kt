package com.wgeplant.model.repository

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * This interface administrates the authentication of the current user.
 */
interface AuthRepo {
    /**
     * This method uses the data source, to delegate the login to firebase.
     * @param email of the user
     * @param password of the user
     */
    suspend fun login(email: String, password: String): Result<String, DomainError>

    /**
     * This method uses the data source, to delegate the sign up to firebase.
     * @param email of the user
     * @param password of the user
     */
    suspend fun register(email: String, password: String): Result<String, DomainError>

    /**
     * This method uses the data source to delegate the logout to firebase.
     */
    suspend fun logout(): Result<Unit, DomainError>

    /**
     * This method uses the data source to delegate the deletion of the account to firebase.
     */
    suspend fun deleteAccount(): Result<Unit, DomainError>

    /**
     * This method uses the data source to get the local uid from firebase.
     */
    suspend fun getLocalUserId(): Result<String, DomainError>

    /**
     * This method returns a unit if the current user is logged in Firebase Authentication.
     */
    suspend fun isLoggedIn(): Result<String, DomainError>

    /**
     * This method returns whether the current user is logged in or not.
     */
    fun getAuthStateFlow(): Flow<Result<Boolean, DomainError>>

    /**
     * This method returns whether the current user could be reloaded or not.
     */
    suspend fun reloadCurrentUser(): Result<Unit, DomainError>
}
