package com.wgeplant.model.interactor.wgManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.repository.DeleteLocalDataRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import javax.inject.Inject

/**
 * This class is responsible for implementing the WG related use cases.
 * @param wgRepo: The repository for WG.
 * @param userRepo: The repository for users.
 * @param initialDataRepo: The repository for initial data.
 * @param deleteLocalDataRepo: The repository for deleting local data.
 */
class ManageWGInteractorImpl @Inject constructor(
    private val wgRepo: WGRepo,
    private val userRepo: UserRepo,
    private val initialDataRepo: InitialDataRepo,
    private val deleteLocalDataRepo: DeleteLocalDataRepo
) : ManageWGInteractor {

    /**
     * This method creates a new WG and adds the local user to the new created WG through the WG repository.
     * @param displayName: The display name of the new WG.
     */
    override suspend fun executeCreation(displayName: String): Result<Unit, DomainError> {
        return when (val createWGResult = wgRepo.createWG(displayName)) {
            is Result.Success -> {
                val invitationCode = createWGResult.data
                return userRepo.joinWGByInvitationCode(invitationCode)
            }
            is Result.Error -> Result.Error(createWGResult.error)
        }
    }

    /**
     * This method joins a WG by the given invitation code through the WG repository.
     * @param invitationCode: The invitation code of the WG.
     */
    override suspend fun executeJoining(invitationCode: String): Result<Unit, DomainError> {
        return when (val joinWGResult = userRepo.joinWGByInvitationCode(invitationCode)) {
            is Result.Success -> initialDataRepo.getInitialData()
            is Result.Error -> Result.Error(joinWGResult.error)
        }
    }

    /**
     * This method removes a user from the WG through the WG repository.
     * @param userId: The id of the user that should get removed.
     */
    override suspend fun executeMemberKickOut(userId: String): Result<Unit, DomainError> {
        return wgRepo.removeUserFromWG(userId)
    }

    /**
     * This method removes the local user from his WG and deletes all local WG related data
     * through the WG repository.
     */
    override suspend fun executeLeaving(): Result<Unit, DomainError> {
        return when (val removeUserResult = userRepo.leaveWG()) {
            is Result.Success -> {
                return deleteLocalDataRepo.deleteAllWGRelatedData()
            }
            is Result.Error -> Result.Error(removeUserResult.error)
        }
    }
}
