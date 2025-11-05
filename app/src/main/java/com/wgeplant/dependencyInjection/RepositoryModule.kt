package com.wgeplant.dependencyInjection

import com.wgeplant.model.repository.AbsenceRepo
import com.wgeplant.model.repository.AbsenceRepoImpl
import com.wgeplant.model.repository.AppointmentRepo
import com.wgeplant.model.repository.AppointmentRepoImpl
import com.wgeplant.model.repository.AuthRepo
import com.wgeplant.model.repository.AuthRepoImpl
import com.wgeplant.model.repository.DeleteLocalDataRepo
import com.wgeplant.model.repository.DeleteLocalDataRepoImpl
import com.wgeplant.model.repository.DeviceRepo
import com.wgeplant.model.repository.DeviceRepoImpl
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.InitialDataRepoImpl
import com.wgeplant.model.repository.StorageRepo
import com.wgeplant.model.repository.StorageRepoImpl
import com.wgeplant.model.repository.TaskRepo
import com.wgeplant.model.repository.TaskRepoImpl
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.UserRepoImpl
import com.wgeplant.model.repository.WGRepo
import com.wgeplant.model.repository.WGRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * This module provides the necessary dependencies for repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepo(
        authRepoImpl: AuthRepoImpl
    ): AuthRepo

    @Binds
    abstract fun bindStorageRepo(
        storageRepoImpl: StorageRepoImpl
    ): StorageRepo

    @Binds
    abstract fun bindUserRepo(
        userRepoImpl: UserRepoImpl
    ): UserRepo

    @Binds
    abstract fun bindWGRepo(
        wgRepoImpl: WGRepoImpl
    ): WGRepo

    @Binds
    abstract fun bindAppointmentRepo(
        appointmentRepoImpl: AppointmentRepoImpl
    ): AppointmentRepo

    @Binds
    abstract fun bindTaskRepo(
        taskRepoImpl: TaskRepoImpl
    ): TaskRepo

    @Binds
    abstract fun bindAbsenceRepo(
        absenceRepoImpl: AbsenceRepoImpl
    ): AbsenceRepo

    @Binds
    abstract fun bindInitialDataRepo(
        initialDataRepoImpl: InitialDataRepoImpl
    ): InitialDataRepo

    @Binds
    abstract fun bindDeleteLocalDataRepo(
        deleteLocalDataRepoImpl: DeleteLocalDataRepoImpl
    ): DeleteLocalDataRepo

    @Binds
    abstract fun provideDeviceRepo(
        deviceRepoImpl: DeviceRepoImpl
    ): DeviceRepo
}
