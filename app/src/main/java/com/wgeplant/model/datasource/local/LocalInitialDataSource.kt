package com.wgeplant.model.datasource.local

import com.wgeplant.common.dto.response.InitialResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible of saving the initial data in persistence.
 */
interface LocalInitialDataSource {
    /**
     * This method saves the receiving data in persistence.
     * @param initialData the initial data that is received
     */
    suspend fun saveInitialData(initialData: InitialResponseDTO): Result<Unit, DomainError>
}
