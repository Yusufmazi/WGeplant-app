package com.wgeplant.model.repository

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.WG
import kotlinx.coroutines.flow.Flow

/**
 * This interface is administrating the wg data with the server and the persistence.
 */
interface WGRepo {
    /**
     * This method returns the current wg of the current user from persistence.
     */
    fun getWG(): Flow<Result<WG, DomainError>>

    /**
     * This method removes a user from the wg. It does not include the current user.
     * First we use the remote data source to let the server handle the request.
     * As it includes new data, we save the new initial data of the server
     * and use the local data source to save it in persistence.
     * @param userId of the user that is removed
     */
    suspend fun removeUserFromWG(userId: String): Result<Unit, DomainError>

    /**
     * This method creates a new wg remote and saves it locally in persistence.
     * It returns the invitation code for the wg.
     * @param displayName of the new wg
     */
    suspend fun createWG(displayName: String): Result<String, DomainError>

    /**
     * This method asks the server to get the requested wg and updates it in persistence.
     * @param wgId of the requested wg
     */
    suspend fun fetchAndSafe(wgId: String): Result<Unit, DomainError>

    /**
     * This method changes the wg information (displayName, picture).
     * First we use the remote data source to update the wg on the server.
     * After we use the local data source to update the persistence.
     * @param wg with the updated changes
     */
    suspend fun updateWG(wg: WG): Result<Unit, DomainError>
}
