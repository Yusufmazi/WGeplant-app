package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import kotlinx.coroutines.flow.Flow

/**
 * This interface is responsible of managing the user data in persistence.
 */
interface LocalUserDataSource {

    /**
     * This method saves a user or replaces an existing one in persistence.
     * @param user to be saved in persistence
     */
    suspend fun saveUser(user: User): Result<Unit, DomainError>

    /**
     * This method returns a specific user of the wg from persistence.
     * @param userId of the searched user
     */
    fun getUserById(userId: String): Flow<Result<User, DomainError>>

    /**
     * This method saves the userId of the current user in Persistence.
     */
    suspend fun saveUserId(userId: String): Result<Unit, DomainError>

    /**
     * This method returns the userId of the current user from persistence.
     */
    suspend fun getLocalUserId(): Result<String, DomainError>

    /**
     * This method returns all user from persistence that are in the same wg as the current user.
     */
    fun getAllUsers(): Flow<Result<List<User>, DomainError>>
}
