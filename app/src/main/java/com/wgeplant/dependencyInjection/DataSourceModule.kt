package com.wgeplant.dependencyInjection

import com.wgeplant.model.datasource.AuthFirebaseDataSource
import com.wgeplant.model.datasource.AuthFirebaseDataSourceImpl
import com.wgeplant.model.datasource.FirebaseStorageDataSource
import com.wgeplant.model.datasource.FirebaseStorageDataSourceImpl
import com.wgeplant.model.datasource.NetworkDataSource
import com.wgeplant.model.datasource.NetworkDataSourceImpl
import com.wgeplant.model.datasource.local.LocalAbsenceDataSource
import com.wgeplant.model.datasource.local.LocalAbsenceDataSourceImpl
import com.wgeplant.model.datasource.local.LocalAppointmentDataSource
import com.wgeplant.model.datasource.local.LocalAppointmentDataSourceImpl
import com.wgeplant.model.datasource.local.LocalDeleteDataSource
import com.wgeplant.model.datasource.local.LocalDeleteDataSourceImpl
import com.wgeplant.model.datasource.local.LocalInitialDataSource
import com.wgeplant.model.datasource.local.LocalInitialDataSourceImpl
import com.wgeplant.model.datasource.local.LocalTaskDataSource
import com.wgeplant.model.datasource.local.LocalTaskDataSourceImpl
import com.wgeplant.model.datasource.local.LocalUserDataSource
import com.wgeplant.model.datasource.local.LocalUserDataSourceImpl
import com.wgeplant.model.datasource.local.LocalWGDataSource
import com.wgeplant.model.datasource.local.LocalWGDataSourceImpl
import com.wgeplant.model.datasource.remote.RemoteAbsenceDataSource
import com.wgeplant.model.datasource.remote.RemoteAbsenceDataSourceImpl
import com.wgeplant.model.datasource.remote.RemoteAppointmentDataSource
import com.wgeplant.model.datasource.remote.RemoteAppointmentDataSourceImpl
import com.wgeplant.model.datasource.remote.RemoteDeviceDataSource
import com.wgeplant.model.datasource.remote.RemoteDeviceDataSourceImpl
import com.wgeplant.model.datasource.remote.RemoteInitialDataSource
import com.wgeplant.model.datasource.remote.RemoteInitialDataSourceImpl
import com.wgeplant.model.datasource.remote.RemoteTaskDataSource
import com.wgeplant.model.datasource.remote.RemoteTaskDataSourceImpl
import com.wgeplant.model.datasource.remote.RemoteUserDataSource
import com.wgeplant.model.datasource.remote.RemoteUserDataSourceImpl
import com.wgeplant.model.datasource.remote.RemoteWGDataSource
import com.wgeplant.model.datasource.remote.RemoteWGDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * This module provides the necessary dependencies for the data source.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindAuthFirebaseDataSource(
        authFirebaseDataSourceImpl: AuthFirebaseDataSourceImpl
    ): AuthFirebaseDataSource

    @Binds
    abstract fun bindFirebaseStorageDataSource(
        firebaseStorageDataSourceImpl: FirebaseStorageDataSourceImpl
    ): FirebaseStorageDataSource

    @Binds
    abstract fun bindNetworkDataSource(
        networkDataSourceImpl: NetworkDataSourceImpl
    ): NetworkDataSource

    @Binds
    abstract fun bindRemoteUserDataSource(
        remoteUserDataSourceImpl: RemoteUserDataSourceImpl
    ): RemoteUserDataSource

    @Binds
    abstract fun bindLocalUserDataSource(
        localUserDataSourceImpl: LocalUserDataSourceImpl
    ): LocalUserDataSource

    @Binds
    abstract fun bindRemoteWGDataSource(
        remoteWGDataSourceImpl: RemoteWGDataSourceImpl
    ): RemoteWGDataSource

    @Binds
    abstract fun bindLocalWGDataSource(
        localWGDataSourceImpl: LocalWGDataSourceImpl
    ): LocalWGDataSource

    @Binds
    abstract fun bindRemoteAppointmentDataSource(
        remoteAppointmentDataSourceImpl: RemoteAppointmentDataSourceImpl
    ): RemoteAppointmentDataSource

    @Binds
    abstract fun bindLocalAppointmentDataSource(
        localAppointmentDataSourceImpl: LocalAppointmentDataSourceImpl
    ): LocalAppointmentDataSource

    @Binds
    abstract fun bindRemoteTaskDataSource(
        remoteTaskDataSourceImpl: RemoteTaskDataSourceImpl
    ): RemoteTaskDataSource

    @Binds
    abstract fun bindLocalTaskDataSource(
        localTaskDataSourceImpl: LocalTaskDataSourceImpl
    ): LocalTaskDataSource

    @Binds
    abstract fun bindRemoteAbsenceDataSource(
        remoteAbsenceDataSourceImpl: RemoteAbsenceDataSourceImpl
    ): RemoteAbsenceDataSource

    @Binds
    abstract fun bindLocalAbsenceDataSource(
        localAbsenceDataSourceImpl: LocalAbsenceDataSourceImpl
    ): LocalAbsenceDataSource

    @Binds
    abstract fun bindRemoteInitialDataSource(
        remoteInitialDataSourceImpl: RemoteInitialDataSourceImpl
    ): RemoteInitialDataSource

    @Binds
    abstract fun bindLocalInitialDataSource(
        localInitialDataSourceImpl: LocalInitialDataSourceImpl
    ): LocalInitialDataSource

    @Binds
    abstract fun bindRemoteDeviceDataSource(
        remoteDeviceDataSourceImpl: RemoteDeviceDataSourceImpl
    ): RemoteDeviceDataSource

    @Binds
    abstract fun bindLocalDeleteDataSource(
        localDeleteDataSourceImpl: LocalDeleteDataSourceImpl
    ): LocalDeleteDataSource
}
