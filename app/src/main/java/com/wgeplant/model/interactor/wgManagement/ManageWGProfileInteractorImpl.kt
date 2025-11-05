package com.wgeplant.model.interactor.wgManagement

import android.net.Uri
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.WG
import com.wgeplant.model.repository.AbsenceRepo
import com.wgeplant.model.repository.StorageRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * This class is responsible for implementing the WG profile related use cases.
 * @param wgRepo: The repository for WG.
 * @param absenceRepo: The repository for absences.
 * @param userRepo: The repository for users.
 * @param storageRepo: The repository for storage.
 */
class ManageWGProfileInteractorImpl @Inject constructor(
    private val wgRepo: WGRepo,
    private val absenceRepo: AbsenceRepo,
    private val userRepo: UserRepo,
    private val storageRepo: StorageRepo
) : ManageWGProfileInteractor {

    /**
     * This method gets the WG data from the WG repository and sends all changes of the saved WG object.
     */
    override fun getWGData(): Flow<Result<WG?, DomainError>> {
        return wgRepo.getWG()
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }

    /**
     * This method gets the WG members from the User repository
     * and sends all changes of the saved list of users.
     */
    override fun getWGMembers(): Flow<Result<List<User>, DomainError>> {
        return userRepo.getAllUsers()
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }

    /**
     * This method gets the invitation code through the saved WG object given by the WG repository.
     */
    override suspend fun executeInvitationCodeInquiry(): Result<String, DomainError> {
        val getWGResult: Result<WG, DomainError> = try {
            wgRepo.getWG().first()
        } catch (e: Exception) {
            return Result.Error(DomainError.Unknown(e))
        }
        return when (getWGResult) {
            is Result.Success -> {
                val wg = getWGResult.data
                Result.Success(wg.invitationCode)
            }
            is Result.Error -> Result.Error(getWGResult.error)
        }
    }

    /**
     * This method changes the WG display name through creating a new WG object with the new name.
     * @param name: The new display name of the WG.
     */
    override suspend fun executeDisplayNameChange(name: String): Result<Unit, DomainError> {
        val getWGResult: Result<WG, DomainError> = try {
            wgRepo.getWG().first()
        } catch (e: Exception) {
            return Result.Error(DomainError.Unknown(e))
        }
        return when (getWGResult) {
            is Result.Success -> {
                val currentWG = getWGResult.data
                val newWG = WG(
                    wgId = currentWG.wgId,
                    displayName = name,
                    invitationCode = currentWG.invitationCode,
                    profilePicture = currentWG.profilePicture
                )
                return wgRepo.updateWG(newWG)
            }
            is Result.Error -> Result.Error(getWGResult.error)
        }
    }

    /**
     * This method changes the WG profile picture through uploading a new image to the storage repository.
     * The current WG object gets replaced with a new one with the new image.
     * @param uri: The uri of the new image.
     */
    override suspend fun executeProfilePictureChange(uri: Uri): Result<Unit, DomainError> {
        val getWGResult: Result<WG, DomainError> = try { // get the currently saved WG object
            wgRepo.getWG().first()
        } catch (e: Exception) {
            return Result.Error(DomainError.Unknown(e))
        }
        return when (getWGResult) {
            is Result.Success -> {
                val currentWG = getWGResult.data
                // delete image if profile already has an profile picture
                if (currentWG.profilePicture != null) {
                    val deleteWGImageResult = storageRepo.deleteWGImage(currentWG.wgId)
                    if (deleteWGImageResult is Result.Error) {
                        return Result.Error(deleteWGImageResult.error)
                    }
                }
                val uploadImageResult = storageRepo.uploadWGImage(currentWG.wgId, uri)
                return when (uploadImageResult) {
                    is Result.Success -> {
                        val imageUrl = uploadImageResult.data
                        val newWG = WG(
                            wgId = currentWG.wgId,
                            displayName = currentWG.displayName,
                            invitationCode = currentWG.invitationCode,
                            profilePicture = imageUrl
                        )
                        return wgRepo.updateWG(newWG)
                    }
                    is Result.Error -> Result.Error(uploadImageResult.error)
                }
            }
            is Result.Error -> Result.Error(getWGResult.error)
        }
    }

    /**
     * This method creates a new absence entry through creating a new Absence object.
     */
    override suspend fun executeAbsenceEditing(
        absenceId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Unit, DomainError> {
        val getAbsenceResult: Result<Absence, DomainError> = try {
            absenceRepo.getAbsenceById(absenceId).first()
        } catch (e: Exception) {
            return Result.Error(DomainError.Unknown(e))
        }

        return when (getAbsenceResult) {
            is Result.Success -> {
                val oldAbsence = getAbsenceResult.data

                val newAbsence = Absence(
                    absenceId = oldAbsence.absenceId,
                    userId = oldAbsence.userId,
                    startDate = startDate,
                    endDate = endDate
                )
                return absenceRepo.updateAbsence(newAbsence)
            }
            is Result.Error -> Result.Error(getAbsenceResult.error)
        }
    }

    /**
     * This method deletes an absence entry through the absence repository.
     * @param absenceId: The id of the absence that should get deleted.
     */
    override suspend fun executeAbsenceDeletion(absenceId: String): Result<Unit, DomainError> {
        return absenceRepo.deleteAbsence(absenceId)
    }

    /**
     * This method gets the absences of a user through the absence repository
     * and sends all changes of the saved absence list.
     * @param userId: The id of the user whose absences should be retrieved.
     */
    override fun getAbsence(userId: String): Flow<Result<List<Absence>, DomainError>> {
        return absenceRepo.getAbsencesByUserId(userId)
            .catch { e ->
                emit(Result.Error(DomainError.Unknown(e)))
            }
    }
}
