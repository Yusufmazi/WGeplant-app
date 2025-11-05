package com.wgeplant.model.repository

import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * This interface administrates the data of absences in the server and persistence.
 */
interface AbsenceRepo {

    /**
     * This method creates a new absence on the server and saves it in persistence.
     * @param absence the new absence
     */
    suspend fun createAbsence(absence: Absence): Result<Unit, DomainError>

    /**
     * This method gets the absence of the given id from persistence
     * @param absenceId wanted absence
     */
    fun getAbsenceById(absenceId: String): Flow<Result<Absence, DomainError>>

    /**
     * This method gets all absences of a user from persistence.
     * @param userId of the user
     */
    fun getAbsencesByUserId(userId: String): Flow<Result<List<Absence>, DomainError>>

    /**
     * This method deletes an absence on the server before deleting it in persistence.
     * @param absenceId of the deleted absence
     */
    suspend fun deleteAbsence(absenceId: String): Result<Unit, DomainError>

    /**
     * This method updates the data of an absence on the server before its updated in persistence.
     * @param absence the updated absence
     */
    suspend fun updateAbsence(absence: Absence): Result<Unit, DomainError>

    /**
     * This method fetches an updated absence from the server to save it in persistence.
     * @param absenceId of the updated absence
     */
    suspend fun fetchAndSafe(absenceId: String): Result<Unit, DomainError>

    /**
     * This method deletes a local absence from persistence.
     * It is used when fcm informs the client about a deleted absence.
     * @param absenceId
     */
    suspend fun deleteLocalAbsence(absenceId: String): Result<Unit, DomainError>
}
