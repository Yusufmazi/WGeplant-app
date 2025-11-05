package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for deleting local data from persistence.
 */
interface LocalDeleteDataSource {

    /**
     * This method deletes all data from persistence.
     */
    suspend fun deleteAllData(): Result<Unit, DomainError>

    /**
     * This method deletes all data related to the wg from persistence.
     */
    suspend fun deleteAllWGRelatedData(): Result<Unit, DomainError>
}
