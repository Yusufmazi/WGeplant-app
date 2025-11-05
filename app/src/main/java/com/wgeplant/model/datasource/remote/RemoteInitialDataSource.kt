package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.response.InitialResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for communicating with the server about initial data.
 */
interface RemoteInitialDataSource {

    /**
     * This method sends the request to the server to get all the initial data of the current user.
     */
    suspend fun getInitialData(): Result<InitialResponseDTO, DomainError>

    /**
     * This method sends the request to the server to remove the given user from the wg.
     * It should return the new initial data for the current user.
     * @param userId of the removed user
     */
    suspend fun removeUserFromWG(userId: String): Result<InitialResponseDTO, DomainError>
}
