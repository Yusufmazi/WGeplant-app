package com.wgeplant.model.repository

import com.wgeplant.model.datasource.AuthFirebaseDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This class administrates the authentication with the current user.
 * @param authDataSource to authenticate the user
 */
class AuthRepoImpl @Inject constructor(
    private val authDataSource: AuthFirebaseDataSource
) : AuthRepo {
    /**
     * This method uses the data source, to delegate the login to firebase.
     * @param email of the user
     * @param password of the user
     */
    override suspend fun login(email: String, password: String): Result<String, DomainError> {
        return authDataSource.loginWithEmailAndPassword(email, password)
    }

    /**
     * This method uses the data source, to delegate the sign up to firebase.
     * @param email of the user
     * @param password of the user
     */
    override suspend fun register(email: String, password: String): Result<String, DomainError> {
        return authDataSource.registerNewUser(email, password)
    }

    /**
     * This method uses the data source to delegate the logout to firebase.
     */
    override suspend fun logout(): Result<Unit, DomainError> {
        return authDataSource.logout()
    }

    /**
     * This method uses the data source to delegate the deletion of the account to firebase.
     */
    override suspend fun deleteAccount(): Result<Unit, DomainError> {
        return authDataSource.deleteAccount()
    }

    /**
     * This method uses the data source to get the local uid from firebase.
     */
    override suspend fun getLocalUserId(): Result<String, DomainError> {
        return authDataSource.getCurrentUserId()
    }

    /**
     * This method returns a unit if the current user is logged in Firebase Authentication.
     */
    override suspend fun isLoggedIn(): Result<String, DomainError> {
        return authDataSource.isLoggedIn()
    }

    /**
     * This method returns whether the current user is logged in or not.
     */
    override fun getAuthStateFlow(): Flow<Result<Boolean, DomainError>> {
        return authDataSource.getAuthStateFlow()
    }

    /**
     * This method returns whether the current user could be reloaded or not.
     */
    override suspend fun reloadCurrentUser(): Result<Unit, DomainError> {
        return authDataSource.reloadCurrentUser()
    }
}
