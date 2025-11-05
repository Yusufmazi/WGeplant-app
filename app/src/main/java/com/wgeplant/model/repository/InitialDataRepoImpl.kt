package com.wgeplant.model.repository

import com.wgeplant.model.datasource.local.LocalInitialDataSource
import com.wgeplant.model.datasource.remote.RemoteInitialDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import javax.inject.Inject

/**
 * This class administrates the initial data from the server with the persistence.
 * @param remoteInitialDataSource to request the data from the server
 * @param localInitialDataSource to safe the data locally
 */
class InitialDataRepoImpl @Inject constructor(
    private val remoteInitialDataSource: RemoteInitialDataSource,
    private val localInitialDataSource: LocalInitialDataSource
) : InitialDataRepo {
    /**
     * This method uses the remote data source to request the initial data from the server.
     * It also uses the local data source to save it in perception.
     */
    override suspend fun getInitialData(): Result<Unit, DomainError> {
        when (val remoteData = remoteInitialDataSource.getInitialData()) {
            is Result.Success -> {
                val localData = localInitialDataSource.saveInitialData(remoteData.data)
                return when (localData) {
                    is Result.Success -> {
                        Result.Success(Unit)
                    }
                    is Result.Error -> {
                        localData
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(remoteData.error)
            }
        }
    }
}
