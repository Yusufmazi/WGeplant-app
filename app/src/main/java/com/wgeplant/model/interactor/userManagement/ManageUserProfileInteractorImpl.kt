package com.wgeplant.model.interactor.userManagement

import android.net.Uri
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.repository.AbsenceRepo
import com.wgeplant.model.repository.StorageRepo
import com.wgeplant.model.repository.UserRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * This class is responsible for implementing the user profile related use cases.
 * @param userRepo: The repository for users.
 * @param absenceRepo: The repository for absences.
 * @param storageRepo: The repository for storage.
 */
class ManageUserProfileInteractorImpl @Inject constructor(
    private val userRepo: UserRepo,
    private val absenceRepo: AbsenceRepo,
    private val storageRepo: StorageRepo
) : ManageUserProfileInteractor {

    /**
     * This method gets the user data from the repository
     * and also passes all changes to the user object along into the flow.
     */
    override suspend fun getUserData(): Flow<Result<User?, DomainError>> {
        return flow {
            when (val getUserIdResult = userRepo.getLocalUserId()) {
                is Result.Success -> {
                    val userId = getUserIdResult.data
                    userRepo.getUserById(userId)
                        .catch { e ->
                            // catch all unexpected exceptions in the getUserById-Flow
                            emit(Result.Error(DomainError.Unknown(e)))
                        }
                        .collect { emittedResult ->
                            // every emitted Result from the getUserById-Flow is emitted to the main flow
                            emit(emittedResult)
                        }
                }
                is Result.Error -> {
                    emit(Result.Error(getUserIdResult.error))
                }
            }
        }
    }

    /**
     * This method changes the user display name through creating a new User object with the new name.
     * @param displayName: The new display name of the user.
     */
    override suspend fun executeDisplayNameChange(displayName: String): Result<Unit, DomainError> {
        return when (val getUserResult = getCurrentUser()) {
            is Result.Success -> {
                val currentUser = getUserResult.data
                val newUser = User(
                    displayName = displayName,
                    userId = currentUser.userId,
                    profilePicture = currentUser.profilePicture
                )
                return userRepo.updateUser(newUser)
            }
            is Result.Error -> Result.Error(getUserResult.error)
        }
    }

    /**
     * This method changes the user profile picture through uploading the new image
     * to the storage repository and deleting the old one, if one already exists.
     * @param uri: The uri of the new image.
     */
    override suspend fun executeProfilePictureChange(uri: Uri): Result<Unit, DomainError> {
        return when (val getUserResult = getCurrentUser()) {
            is Result.Success -> {
                val currentUser = getUserResult.data
                val userId = currentUser.userId

                if (currentUser.profilePicture != null) {
                    val deleteUserImageResult = storageRepo.deleteUserImage()
                    if (deleteUserImageResult is Result.Error) {
                        return Result.Error(deleteUserImageResult.error)
                    }
                }
                return when (val uploadUserImageResult = storageRepo.uploadUserImage(userId, uri)) {
                    is Result.Success -> {
                        val profilePictureUrl = uploadUserImageResult.data
                        val newUser = User(
                            displayName = currentUser.displayName,
                            userId = userId,
                            profilePicture = profilePictureUrl
                        )
                        return userRepo.updateUser(newUser)
                    }
                    is Result.Error -> {
                        Result.Error(uploadUserImageResult.error)
                    }
                }
            }
            is Result.Error -> Result.Error(getUserResult.error)
        }
    }

    /**
     * This method returns the current user through the repository.
     */
    private suspend fun getCurrentUser(): Result<User, DomainError> {
        return when (val getUserIdResult = userRepo.getLocalUserId()) {
            is Result.Success -> {
                val userId = getUserIdResult.data

                try {
                    return userRepo.getUserById(userId).first()
                } catch (e: Exception) {
                    return Result.Error(DomainError.Unknown(e))
                }
            }
            is Result.Error -> Result.Error(getUserIdResult.error)
        }
    }

    /**
     * This method adds an absence entry of the local user through the absence repository.
     * @param startDate: The start date of the absence.
     * @param endDate: The end date of the absence.
     */
    override suspend fun executeAbsenceEntry(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Unit, DomainError> {
        return when (val getUserIdResult = userRepo.getLocalUserId()) {
            is Result.Success -> {
                val userId = getUserIdResult.data
                val newAbsence = Absence(
                    userId = userId,
                    startDate = startDate,
                    endDate = endDate
                )
                return absenceRepo.createAbsence(newAbsence)
            }
            is Result.Error -> Result.Error(getUserIdResult.error)
        }
    }
}
