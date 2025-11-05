package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.WG
import kotlinx.coroutines.flow.Flow

/**
 * This interface is responsible to manage the wg data locally in persistence.
 */
interface LocalWGDataSource {

    /**
     * This method saves a wg object in persistence and replaces it if the ids equal.
     * @param wg the wg to save in persistence
     */
    suspend fun saveWG(wg: WG): Result<Unit, DomainError>

    /**
     * This methode returns the wg of the current user from persistence if he is in any.
     */
    fun getWG(): Flow<Result<WG, DomainError>>

    /**
     * This method deletes the wg of the current user from persistence.
     * @param wgId of the current wg
     */
    suspend fun deleteWG(wgId: String): Result<Unit, DomainError>
}
