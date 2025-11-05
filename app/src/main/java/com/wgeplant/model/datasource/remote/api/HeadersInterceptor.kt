package com.wgeplant.model.datasource.remote.api

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * This interceptor uses the header information stored in HeaderConfiguration to intercept the
 * client request and set the header data accordingly.
 * @param headerConfiguration data class of the header information
 */
class HeadersInterceptor @Inject constructor(
    private val headerConfiguration: HeaderConfiguration
) : Interceptor {

    companion object {
        private const val AUTHORIZATION_HEADER_NAME = "Authorization"
        private const val DEVICE_ID_HEADER_NAME = "deviceId"
    }

    /**
     * This method sets the header for a request to the server.
     * @param chain original Request
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        headerConfiguration.getFirebaseIdToken()?.let { firebaseIdToken ->
            builder.header(AUTHORIZATION_HEADER_NAME, "Bearer $firebaseIdToken")
        }
        headerConfiguration.getDeviceId()?.let { deviceId ->
            builder.header(DEVICE_ID_HEADER_NAME, deviceId)
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
