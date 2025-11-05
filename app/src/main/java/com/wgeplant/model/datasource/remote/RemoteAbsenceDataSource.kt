package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.AbsenceRequestDTO
import com.wgeplant.common.dto.response.AbsenceResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for the communication with the server regarding absence data.
 */
interface RemoteAbsenceDataSource {
    /**
     * This method creates a new absence on the server.
     * @param absence the new absence
     */
    suspend fun createAbsence(absence: AbsenceRequestDTO): Result<AbsenceResponseDTO, DomainError>

    /**
     * This method updates an existing absence on the server.
     * @param absence updated absence
     */
    suspend fun updateAbsence(absence: AbsenceRequestDTO): Result<AbsenceResponseDTO, DomainError>

    /**
     * This method fetches a specific absence from the server.
     * @param absenceId of the absence
     */
    suspend fun getAbsenceById(absenceId: String): Result<AbsenceResponseDTO, DomainError>

    /**
     * This method deletes an absence from the server.
     * @param absenceId of the absence
     */
    suspend fun deleteAbsence(absenceId: String): Result<Unit, DomainError>
}
