package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * This interface is responsible for the communication and interaction with persistence
 * regarding absences.
 */
interface LocalAbsenceDataSource {

    /**
     * This method saves a new absence or updates an existing one in persistence.
     * @param absence saving absence
     */
    suspend fun saveAbsence(absence: Absence): Result<Unit, DomainError>

    /**
     * This method returns a specific absence from persistence.
     * @param absenceId of the absence
     */
    fun getAbsenceById(absenceId: String): Flow<Result<Absence, DomainError>>

    /**
     * This method returns all absences of a specific user from persistence.
     * @param userId of the user
     */
    fun getAllAbsencesOfUser(userId: String): Flow<Result<List<Absence>, DomainError>>

    /**
     * This method deletes an absence from persistence.
     * @param absenceId of the absence
     */
    suspend fun deleteAbsence(absenceId: String): Result<Unit, DomainError>
}
