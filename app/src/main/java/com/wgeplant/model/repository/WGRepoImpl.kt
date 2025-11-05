package com.wgeplant.model.repository

import com.wgeplant.common.dto.requests.WGRequestDTO
import com.wgeplant.common.dto.response.toDomain
import com.wgeplant.model.datasource.local.LocalInitialDataSource
import com.wgeplant.model.datasource.local.LocalWGDataSource
import com.wgeplant.model.datasource.remote.RemoteInitialDataSource
import com.wgeplant.model.datasource.remote.RemoteWGDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.WG
import com.wgeplant.model.domain.toRequestDTO
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This class manages the wg data with the server and persistence.
 * @param remoteWGData to communicate the wg data with the server
 * @param localWGData to manage the wg data locally
 * @param remoteInitialData to request new initial data from the server
 * @param localInitialData to save the new initial data locally
 */
class WGRepoImpl @Inject constructor(
    private val remoteWGData: RemoteWGDataSource,
    private val localWGData: LocalWGDataSource,
    private val remoteInitialData: RemoteInitialDataSource,
    private val localInitialData: LocalInitialDataSource
) : WGRepo {
    /**
     * This method returns the current wg of the current user from persistence.
     */
    override fun getWG(): Flow<Result<WG, DomainError>> {
        return localWGData.getWG()
    }

    /**
     * This method removes a user from the wg. It does not include the current user.
     * First we use the remote data source to let the server handle the request.
     * As it includes new data, we save the new initial data of the server
     * and use the local data source to save it in persistence.
     * @param userId of the user that is removed
     */
    override suspend fun removeUserFromWG(userId: String): Result<Unit, DomainError> {
        when (val remoteRemoval = remoteInitialData.removeUserFromWG(userId)) {
            is Result.Success -> {
                val localNewData = localInitialData.saveInitialData(remoteRemoval.data)
                return when (localNewData) {
                    is Result.Success -> {
                        Result.Success(Unit)
                    }
                    is Result.Error -> {
                        Result.Error(localNewData.error)
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(remoteRemoval.error)
            }
        }
    }

    /**
     * This method creates a new wg remote and saves it locally in persistence.
     * It returns the invitation code for the wg.
     * @param displayName of the new wg
     */
    override suspend fun createWG(displayName: String): Result<String, DomainError> {
        val remoteWG = remoteWGData.createWGRemote(
            WGRequestDTO(null, displayName, null, null)
        )
        when (remoteWG) {
            is Result.Success -> {
                val createdWG = remoteWG.data.toDomain()
                return when (val localWG = localWGData.saveWG(createdWG)) {
                    is Result.Success -> {
                        Result.Success(createdWG.invitationCode)
                    }
                    is Result.Error -> {
                        Result.Error(localWG.error)
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(remoteWG.error)
            }
        }
    }

    /**
     * This method asks the server to get the requested wg and updates it in persistence.
     * @param wgId of the requested wg
     */
    override suspend fun fetchAndSafe(wgId: String): Result<Unit, DomainError> {
        when (val remoteWG = remoteWGData.getWGById(wgId)) {
            is Result.Success -> {
                return when (val localUpdate = localWGData.saveWG(remoteWG.data.toDomain())) {
                    is Result.Success -> {
                        Result.Success(Unit)
                    }
                    is Result.Error -> {
                        Result.Error(localUpdate.error)
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(remoteWG.error)
            }
        }
    }

    /**
     * This method changes the wg information (displayName, picture).
     * First we use the remote data source to update the wg on the server.
     * After we use the local data source to update the persistence.
     * @param wg with the updated changes
     */
    override suspend fun updateWG(wg: WG): Result<Unit, DomainError> {
        when (val remoteWGUpdate = remoteWGData.updateWG(wg.toRequestDTO())) {
            is Result.Success -> {
                return when (val localUpdate = localWGData.saveWG(remoteWGUpdate.data.toDomain())) {
                    is Result.Success -> {
                        Result.Success(Unit)
                    }
                    is Result.Error -> {
                        Result.Error(localUpdate.error)
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(remoteWGUpdate.error)
            }
        }
    }
}
