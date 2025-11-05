package com.wgeplant.model.datasource.local

import com.wgeplant.common.dto.response.InitialResponseDTO
import com.wgeplant.common.dto.response.toPersistence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import javax.inject.Inject

/**
 * This class is responsible for saving the initial data in persistence.
 */
class LocalInitialDataSourceImpl @Inject constructor() : LocalInitialDataSource {
    /**
     * This method saves the receiving data in persistence.
     * @param initialData the initial data that is received
     */
    override suspend fun saveInitialData(initialData: InitialResponseDTO):
        Result<Unit, DomainError> {
        return initialData.toPersistence()
    }
}
