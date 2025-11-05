package com.wgeplant.model.datasource

import kotlinx.coroutines.flow.Flow

/**
 * This interface is responsible to check the network connection.
 */
interface NetworkDataSource {
    /**
     * This method checks whether the device has network connection or not and returns it with a flow.
     */
    fun observeNetworkConnection(): Flow<Boolean>
}
