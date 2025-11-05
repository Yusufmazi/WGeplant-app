package com.wgeplant.model.interactor.remoteUpdateManagement

import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for starting the remote update related use cases.
 */
interface RemoteUpdateInteractor {
    /**
     * This method gets the device Token from firebase cloud messaging.
     */
    suspend fun getDeviceTokenFromFCM(): Result<String, DomainError>

    /**
     * This method starts the remote update process and actualizes the model.
     */
    suspend fun updateModel(operationType: String, objectId: String)
}
