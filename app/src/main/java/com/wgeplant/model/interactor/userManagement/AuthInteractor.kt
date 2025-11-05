package com.wgeplant.model.interactor.userManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for starting the authentication related use cases.
 */
interface AuthInteractor {
    /**
     * This method registers a new user.
     * @param email: The email of the new user.
     * @param password: The password of the new user.
     * @param displayName: The display name of the new user.
     */
    suspend fun executeRegistration(email: String, password: String, displayName: String):
        Result<Unit, DomainError>

    /**
     * This method logs in a user.
     * @param email: The email of the user.
     * @param password: The password of the user.
     */
    suspend fun executeLogin(email: String, password: String): Result<Unit, DomainError>

    /**
     * This method logs out the local user.
     */
    suspend fun executeLogout(): Result<Unit, DomainError>

    /**
     * This method deletes the local user.
     */
    suspend fun executeAccountDeletion(): Result<Unit, DomainError>
}
