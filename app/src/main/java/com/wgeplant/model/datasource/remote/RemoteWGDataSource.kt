package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.WGRequestDTO
import com.wgeplant.common.dto.response.WGResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for communicating with the server about wg data.
 */
interface RemoteWGDataSource {

    /**
     * This method sends the server the request to create a new wg.
     * @param wg the new wg
     */
    suspend fun createWGRemote(wg: WGRequestDTO): Result<WGResponseDTO, DomainError>

    /**
     * This method sends the server the request to get the wg of the id.
     * @param wgId of the wanted wg
     */
    suspend fun getWGById(wgId: String): Result<WGResponseDTO, DomainError>

    /**
     * This method sends the server the request to update the data of the given wg.
     * @param wg the updated data of the wg
     */
    suspend fun updateWG(wg: WGRequestDTO): Result<WGResponseDTO, DomainError>
}
