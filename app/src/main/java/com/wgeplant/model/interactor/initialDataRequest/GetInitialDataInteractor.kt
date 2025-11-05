package com.wgeplant.model.interactor.initialDataRequest

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * This interface is responsible for requesting the initial data.
 */
interface GetInitialDataInteractor {
    /**
     * This method starts the process of requesting the initial data
     * and the data gets saved in persistence.
     */
    suspend fun execute(): Result<Unit, DomainError>

    /**
     * This method checks if the user is logged in
     * and sets the header for the communication if the user is logged in.
     */
    fun isUserLoggedIn(): Flow<Result<Boolean, DomainError>>

    /**
     * This method checks if the user is in the WG.
     */
    fun isUserInWG(): Flow<Result<Boolean, DomainError>>
}
