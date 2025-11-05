package com.wgeplant.dependencyInjection

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.HeaderConfiguration
import com.wgeplant.model.datasource.remote.api.HeadersInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * This module provides the necessary dependencies for retrofit to communicate with the server.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BW_CLOUD_IP_ADDRESS = ""

    @Provides
    @Singleton
    @Named("BaseUrl")
    fun provideBaseUrl(): String = BW_CLOUD_IP_ADDRESS // bwCloud Ip

    @Provides
    @Singleton
    fun provideHeaderConfiguration(): HeaderConfiguration {
        return HeaderConfiguration()
    }

    @Provides
    @Singleton
    fun provideHeadersInterceptor(
        headerConfiguration: HeaderConfiguration
    ): HeadersInterceptor {
        return HeadersInterceptor(headerConfiguration)
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        headersInterceptor: HeadersInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(headersInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("BaseUrl") baseUrl: String,
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
