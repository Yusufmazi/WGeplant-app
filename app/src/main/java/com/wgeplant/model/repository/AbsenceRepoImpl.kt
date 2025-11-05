package com.wgeplant.model.repository

import com.wgeplant.common.dto.response.toDomain
import com.wgeplant.model.datasource.local.LocalAbsenceDataSource
import com.wgeplant.model.datasource.remote.RemoteAbsenceDataSource
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.toRequestDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This class manages the absence data in the server with the data in persistence.
 * @param remoteAbsenceData to manage the remote absence data
 * @param localAbsenceData to manage the local absence
 */
class AbsenceRepoImpl @Inject constructor(
    private val remoteAbsenceData: RemoteAbsenceDataSource,
    private val localAbsenceData: LocalAbsenceDataSource
) : AbsenceRepo {
    /**
     * This method creates a new absence on the server and saves it in persistence.
     * @param absence the new absence
     */
    override suspend fun createAbsence(absence: Absence): Result<Unit, DomainError> {
        return when (val remoteCreation = remoteAbsenceData.createAbsence(absence.toRequestDto())) {
            is Result.Success -> {
                localAbsenceData.saveAbsence(remoteCreation.data.toDomain())
            }
            is Result.Error -> {
                remoteCreation
            }
        }
    }

    /**
     * This method gets the absence of the given id from persistence
     * @param absenceId wanted absence
     */
    override fun getAbsenceById(absenceId: String): Flow<Result<Absence, DomainError>> {
        return localAbsenceData.getAbsenceById(absenceId)
    }

    /**
     * This method gets all absences of a user from persistence.
     * @param userId of the user
     */
    override fun getAbsencesByUserId(userId: String): Flow<Result<List<Absence>, DomainError>> {
        return localAbsenceData.getAllAbsencesOfUser(userId)
    }

    /**
     * This method deletes an absence on the server before deleting it in persistence.
     * @param absenceId of the deleted absence
     */
    override suspend fun deleteAbsence(absenceId: String): Result<Unit, DomainError> {
        return when (val remoteDeletion = remoteAbsenceData.deleteAbsence(absenceId)) {
            is Result.Success -> {
                localAbsenceData.deleteAbsence(absenceId)
            }
            is Result.Error -> {
                remoteDeletion
            }
        }
    }

    /**
     * This method updates the data of an absence on the server before its updated in persistence.
     * @param absence the updated absence
     */
    override suspend fun updateAbsence(absence: Absence): Result<Unit, DomainError> {
        return when (val remoteUpdate = remoteAbsenceData.updateAbsence(absence.toRequestDto())) {
            is Result.Success -> {
                localAbsenceData.saveAbsence(remoteUpdate.data.toDomain())
            }
            is Result.Error -> {
                remoteUpdate
            }
        }
    }

    /**
     * This method fetches an updated absence from the server to save it in persistence.
     * @param absenceId of the updated absence
     */
    override suspend fun fetchAndSafe(absenceId: String): Result<Unit, DomainError> {
        return when (val remoteAbsence = remoteAbsenceData.getAbsenceById(absenceId)) {
            is Result.Success -> {
                localAbsenceData.saveAbsence(remoteAbsence.data.toDomain())
            }
            is Result.Error -> {
                remoteAbsence
            }
        }
    }

    /**
     * This method deletes a local absence from persistence.
     * It is used when fcm informs the client about a deleted absence.
     * @param absenceId
     */
    override suspend fun deleteLocalAbsence(absenceId: String): Result<Unit, DomainError> {
        return localAbsenceData.deleteAbsence(absenceId)
    }
}
