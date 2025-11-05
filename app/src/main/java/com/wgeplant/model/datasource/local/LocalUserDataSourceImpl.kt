package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import javax.inject.Inject

/**
 * This class is managing the data of the current user and wg members in persistence.
 */
class LocalUserDataSourceImpl @Inject constructor() : LocalUserDataSource {
    /**
     * This method saves a user or replaces an existing one in persistence.
     * @param user to be saved in persistence
     */
    override suspend fun saveUser(user: User): Result<Unit, DomainError> {
        return try {
            Persistence.updateUserInWG(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    /**
     * This method returns a specific user of the wg from persistence.
     * @param userId of the searched user
     */
    override fun getUserById(userId: String): Flow<Result<User, DomainError>> {
        return Persistence.getUserInWG(userId).map {
                foundUser ->
            if (foundUser != null) {
                Result.Success(foundUser)
            } else {
                Result.Error(DomainError.PersistenceError)
            }
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method saves the userId of the current user in Persistence.
     */
    override suspend fun saveUserId(userId: String): Result<Unit, DomainError> {
        Persistence.setLocalUserId(userId)
        return Result.Success(Unit)
    }

    /**
     * This method returns the userId of the current user from persistence.
     */
    override suspend fun getLocalUserId(): Result<String, DomainError> {
        val userId = Persistence.getLocalUserId()
        return if (userId != null) {
            Result.Success(userId)
        } else {
            Result.Error(DomainError.NotFoundError)
        }
    }

    /**
     * This method returns all user from persistence that are in the same wg as the current user.
     */
    override fun getAllUsers(): Flow<Result<List<User>, DomainError>> {
        return Persistence.getUsersInWG().map {
                foundUserList ->
            Result.Success(foundUserList)
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }
}
