package com.wgeplant.model.datasource.local

import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import javax.inject.Inject

/**
 * This class is responsible to manage absences locally in persistence.
 */
class LocalAbsenceDataSourceImpl @Inject constructor() : LocalAbsenceDataSource {
    /**
     * This method saves a new absence or updates an existing one in persistence.
     * @param absence saving absence
     */
    override suspend fun saveAbsence(absence: Absence): Result<Unit, DomainError> {
        return try {
            Persistence.saveAbsence(absence)
        } catch (e: Exception) {
            Result.Error(DomainError.PersistenceError)
        }
    }

    /**
     * This method returns a specific absence from persistence.
     * @param absenceId of the absence
     */
    override fun getAbsenceById(absenceId: String): Flow<Result<Absence, DomainError>> {
        return Persistence.getAbsence(absenceId).map { absence ->
            if (absence != null) {
                Result.Success(absence)
            } else {
                Result.Error(DomainError.NotFoundError)
            }
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method returns all absences of a specific user from persistence.
     * @param userId of the user
     */
    override fun getAllAbsencesOfUser(userId: String): Flow<Result<List<Absence>, DomainError>> {
        return Persistence.getAbsenceOfUser(userId).map { absenceList ->
            Result.Success(absenceList)
        }.catch { e ->
            val domainError = when (e) {
                is IOException -> DomainError.NetworkError
                else -> { DomainError.Unknown(e) }
            }
            Result.Error(domainError)
        }
    }

    /**
     * This method deletes an absence from persistence.
     * @param absenceId of the absence
     */
    override suspend fun deleteAbsence(absenceId: String): Result<Unit, DomainError> {
        return Persistence.deleteAbsence(absenceId)
    }
}
