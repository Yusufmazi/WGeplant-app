package com.wgeplant.model.datasource.remote.api

import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages the data that is used for the OkHttpClient - Header.
 */

@Singleton
class HeaderConfiguration @Inject constructor() {
    private var currentFirebaseToken: String? = null
    private var currentDeviceId: String? = null

    fun getFirebaseIdToken(): String? {
        return currentFirebaseToken
    }

    fun getDeviceId(): String? {
        return currentDeviceId
    }

    fun setAuthData(idToken: String?, deviceToken: String?) {
        currentFirebaseToken = idToken
        currentDeviceId = deviceToken
    }

    fun clearAuthData() {
        currentFirebaseToken = null
        currentDeviceId = null
    }
}
