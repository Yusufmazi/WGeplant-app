package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.persistence.Persistence
import javax.inject.Inject

/**
 * This class is responsible for the deletion of data in persistence.
 */
class LocalDeleteDataSourceImpl @Inject constructor() : LocalDeleteDataSource {
    /**
     * This method deletes all data from persistence.
     */
    override suspend fun deleteAllData(): Result<Unit, DomainError> {
        return Persistence.deleteAllData()
    }

    /**
     * This method deletes all data related to the wg from persistence.
     */
    override suspend fun deleteAllWGRelatedData(): Result<Unit, DomainError> {
        return Persistence.deleteWGData()
    }
}
