package com.wgeplant.model.repository

import com.wgeplant.model.datasource.local.LocalDeleteDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import javax.inject.Inject

/**
 * This class manages the deletion of data locally.
 * @param localDeleteData to delete the wanted data
 */
class DeleteLocalDataRepoImpl @Inject constructor(
    private val localDeleteData: LocalDeleteDataSource
) : DeleteLocalDataRepo {
    /**
     * This method deletes all data from persistence.
     */
    override suspend fun deleteAllLocalData(): Result<Unit, DomainError> {
        return localDeleteData.deleteAllData()
    }

    /**
     * This method deletes all wg related data from persistence.
     */
    override suspend fun deleteAllWGRelatedData(): Result<Unit, DomainError> {
        return localDeleteData.deleteAllWGRelatedData()
    }
}
