package com.wgeplant.model.repository

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import kotlinx.coroutines.flow.Flow

/**
 * This interface administrates the user data with the server and persistence.
 */
interface UserRepo {
    /**
     * This method uses the remote data source to create a new user account in the server.
     * This newly created user is saved in persistence with the local data source.
     * @param userId of the user
     * @param displayName of the user
     */
    suspend fun createUser(userId: String, displayName: String): Result<Unit, DomainError>

    /**
     * This method returns the user of the id from persistence.
     * @param userId of the searched user
     */
    fun getUserById(userId: String): Flow<Result<User, DomainError>>

    /**
     * This method saves the userId of the current user in persistence.
     * @param userId of the current user
     */
    suspend fun setLocalUserId(userId: String): Result<Unit, DomainError>

    /**
     * This method gets the userId of the current user from persistence.
     */
    suspend fun getLocalUserId(): Result<String, DomainError>

    /**
     * This method tries to fetch all users of the wg the current user is a part of.
     * As the persistence is always up to date, we only need the local data source.
     */
    fun getAllUsers(): Flow<Result<List<User>, DomainError>>

    /**
     * This method deletes the user data in case of account deletion or logout.
     * Then the user uses the remote data source to delete all user data on the server.
     */
    suspend fun deleteUserData(): Result<Unit, DomainError>

    /**
     * This method gets the user from the server, in case of a fcm message to update said user.
     * It uses the remote data source to get the updated user from the server.
     * The local data source is used to save it in persistence.
     * @param userId of the updated user
     */
    suspend fun fetchAndSafe(userId: String): Result<Unit, DomainError>

    /**
     * This method changes the user information (displayName, picture) of the current user.
     * First we use the remote data source to update the user on the server.
     * After we use the local data source to update the persistence.
     * @param user with the updated changes
     */
    suspend fun updateUser(user: User): Result<Unit, DomainError>

    /**
     * This method lets the current user join a wg.
     * The remote data source communicates with the server to add the user to  the wg.
     * @param code of the wg the user wants to join
     */
    suspend fun joinWGByInvitationCode(code: String): Result<Unit, DomainError>

    /**
     * This method lets the current user leave the wg he is in.
     * Through the remote data source, we delegate the command to the server.
     */
    suspend fun leaveWG(): Result<Unit, DomainError>
}
