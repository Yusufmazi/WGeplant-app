package com.wgeplant.model.repository

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface manages the request for the initial data from the server.
 */
interface InitialDataRepo {
    /**
     * This method uses the remote data source to request the initial data from the server.
     * It also uses the local data source to save it in perception.
     */
    suspend fun getInitialData(): Result<Unit, DomainError>
}
