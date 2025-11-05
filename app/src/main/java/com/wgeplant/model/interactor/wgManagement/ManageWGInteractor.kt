package com.wgeplant.model.interactor.wgManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for starting the WG related use cases.
 */
interface ManageWGInteractor {
    /**
     * This method creates a new WG.
     * @param displayName: The display name of the WG.
     */
    suspend fun executeCreation(displayName: String): Result<Unit, DomainError>

    /**
     * This method lets the local user join the WG of the given invitation code.
     * @param invitationCode: The invitation code of the WG to be joined.
     */
    suspend fun executeJoining(invitationCode: String): Result<Unit, DomainError>

    /**
     * This method lets the local user kick out a member of the WG.
     * @param userId: The ID of the member to be kicked out.
     */
    suspend fun executeMemberKickOut(userId: String): Result<Unit, DomainError>

    /**
     * This method lets the local user leave the WG.
     */
    suspend fun executeLeaving(): Result<Unit, DomainError>
}
