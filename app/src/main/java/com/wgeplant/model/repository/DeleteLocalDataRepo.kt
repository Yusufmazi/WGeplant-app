package com.wgeplant.model.repository

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface administrates the deletion of local data.
 */
interface DeleteLocalDataRepo {
    /**
     * This method deletes all data from persistence.
     */
    suspend fun deleteAllLocalData(): Result<Unit, DomainError>

    /**
     * This method deletes all wg related data from persistence.
     */
    suspend fun deleteAllWGRelatedData(): Result<Unit, DomainError>
}
