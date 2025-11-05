package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.WG
import com.wgeplant.model.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import javax.inject.Inject

/**
 * This class is managing the wg data of the user in persistence.
 */
class LocalWGDataSourceImpl @Inject constructor() : LocalWGDataSource {
    /**
     * This method saves a wg object in persistence and replaces it if the ids equal.
     * @param wg the wg to save in persistence
     */
    override suspend fun saveWG(wg: WG): Result<Unit, DomainError> {
        return try {
            Persistence.saveWGOfLocalUser(wg)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    /**
     * This methode returns the wg of the current user from persistence if he is in any.
     */
    override fun getWG(): Flow<Result<WG, DomainError>> {
        return Persistence.wgOfLocalUser.map {
                wg ->
            if (wg != null) {
                Result.Success(wg)
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
     * This method deletes the wg of the current user from persistence.
     * @param wgId of the current wg
     */
    override suspend fun deleteWG(wgId: String): Result<Unit, DomainError> {
        val localWG = Persistence.wgOfLocalUser.value
        return try {
            if (localWG != null && localWG.wgId == wgId) {
                try {
                    Persistence.saveWGOfLocalUser(null)
                    Result.Success(Unit)
                } catch (e: Exception) {
                    Result.Error(DomainError.PersistenceError)
                }
            } else {
                Result.Error(DomainError.NotFoundError)
            }
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }
}
