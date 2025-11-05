package com.wgeplant.model.repository

import com.wgeplant.common.dto.requests.UserRequestDTO
import com.wgeplant.common.dto.response.toDomain
import com.wgeplant.model.datasource.local.LocalUserDataSource
import com.wgeplant.model.datasource.remote.RemoteUserDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.toRequestDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This class manges the user data with the server and persistence.
 * @param remoteUserData to communicate with the server
 * @param localUserData to manage the data locally in persistence
 */
class UserRepoImpl @Inject constructor(
    private val remoteUserData: RemoteUserDataSource,
    private val localUserData: LocalUserDataSource
) : UserRepo {
    /**
     * This method uses the remote data source to create a new user account in the server.
     * This newly created user is saved in persistence with the local data source.
     * @param userId of the user
     * @param displayName of the user
     */
    override suspend fun createUser(
        userId: String,
        displayName: String
    ): Result<Unit, DomainError> {
        val userRequestDto = UserRequestDTO(userId, displayName, null)
        when (val remoteUser = remoteUserData.createUserRemote(userRequestDto)) {
            is Result.Success -> {
                return when (val localUser = localUserData.saveUser(remoteUser.data.toDomain())) {
                    is Result.Success -> {
                        localUser
                    }
                    is Result.Error -> {
                        Result.Error(localUser.error)
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(remoteUser.error)
            }
        }
    }

    /**
     * This method returns the user of the id from persistence.
     * @param userId of the searched user
     */
    override fun getUserById(userId: String): Flow<Result<User, DomainError>> {
        return localUserData.getUserById(userId)
    }

    /**
     * This method saves the userId of the current user in persistence.
     * @param userId of the current user
     */
    override suspend fun setLocalUserId(userId: String): Result<Unit, DomainError> {
        return localUserData.saveUserId(userId)
    }

    /**
     * This method gets the userId of the current user from persistence.
     */
    override suspend fun getLocalUserId(): Result<String, DomainError> {
        return localUserData.getLocalUserId()
    }

    /**
     * This method tries to fetch all users of the wg the current user is a part of.
     * As the persistence is always up to date, we only need the local data source.
     */
    override fun getAllUsers(): Flow<Result<List<User>, DomainError>> {
        return localUserData.getAllUsers()
    }

    /**
     * This method deletes the user data in case of account deletion or logout.
     * Then the user uses the remote data source to delete all user data on the server.
     */
    override suspend fun deleteUserData(): Result<Unit, DomainError> {
        return remoteUserData.deleteUserData()
    }

    /**
     * This method gets the user from the server, in case of a fcm message to update said user.
     * It uses the remote data source to get the updated user from the server.
     * The local data source is used to save it in persistence.
     * @param userId of the updated user
     */
    override suspend fun fetchAndSafe(userId: String): Result<Unit, DomainError> {
        when (val fetchedUser = remoteUserData.getUserById(userId)) {
            is Result.Success -> {
                val fetchedUserDomain: User = fetchedUser.data.toDomain()
                return when (val localSafedUser = localUserData.saveUser(fetchedUserDomain)) {
                    is Result.Success -> {
                        Result.Success(Unit)
                    }
                    is Result.Error -> {
                        Result.Error(localSafedUser.error)
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(fetchedUser.error)
            }
        }
    }

    /**
     * This method changes the user information (displayName, picture) of the current user.
     * First we use the remote data source to update the user on the server.
     * After we use the local data source to update the persistence.
     * @param user with the updated changes
     */
    override suspend fun updateUser(user: User): Result<Unit, DomainError> {
        when (val changedUser = remoteUserData.updateUser(user.toRequestDto())) {
            is Result.Success -> {
                val localChangedUser = localUserData.saveUser(changedUser.data.toDomain())
                return when (localChangedUser) {
                    is Result.Success -> {
                        Result.Success(Unit)
                    }
                    is Result.Error -> {
                        Result.Error(localChangedUser.error)
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(changedUser.error)
            }
        }
    }

    /**
     * This method lets the current user join a wg.
     * The remote data source communicates with the server to add the user to  the wg.
     * @param code of the wg the user wants to join
     */
    override suspend fun joinWGByInvitationCode(code: String): Result<Unit, DomainError> {
        return remoteUserData.joinWG(code)
    }

    /**
     * This method lets the current user leave the wg he is in.
     * Through the remote data source, we delegate the command to the server.
     */
    override suspend fun leaveWG(): Result<Unit, DomainError> {
        return remoteUserData.leaveWG()
    }
}
