package com.wgeplant.dependencyInjection

import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractor
import com.wgeplant.model.interactor.calendarManagement.GetCalendarDataInteractorImpl
import com.wgeplant.model.interactor.calendarManagement.ManageAppointmentInteractor
import com.wgeplant.model.interactor.calendarManagement.ManageAppointmentInteractorImpl
import com.wgeplant.model.interactor.calendarManagement.ManageTaskInteractor
import com.wgeplant.model.interactor.calendarManagement.ManageTaskInteractorImpl
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractorImpl
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractor
import com.wgeplant.model.interactor.initialDataRequest.GetInitialDataInteractorImpl
import com.wgeplant.model.interactor.remoteUpdateManagement.RemoteUpdateInteractor
import com.wgeplant.model.interactor.remoteUpdateManagement.RemoteUpdateInteractorImpl
import com.wgeplant.model.interactor.userManagement.AuthInteractor
import com.wgeplant.model.interactor.userManagement.AuthInteractorImpl
import com.wgeplant.model.interactor.userManagement.ManageUserProfileInteractor
import com.wgeplant.model.interactor.userManagement.ManageUserProfileInteractorImpl
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractorImpl
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractor
import com.wgeplant.model.interactor.wgManagement.ManageWGProfileInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This module provides the necessary dependencies for the interactors.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class InteractorModule {

    @Binds
    @Singleton
    abstract fun bindAuthInteractorImpl(
        authInteractorImpl: AuthInteractorImpl
    ): AuthInteractor

    @Binds
    @Singleton
    abstract fun bindManageUserProfileInteractorImpl(
        manageUserProfileInteractorImpl: ManageUserProfileInteractorImpl
    ): ManageUserProfileInteractor

    @Binds
    @Singleton
    abstract fun bindManageWGInteractorImpl(
        manageWGInteractorImpl: ManageWGInteractorImpl
    ): ManageWGInteractor

    @Binds
    @Singleton
    abstract fun bindManageWGProfileInteractorImpl(
        manageWGProfileInteractorImpl: ManageWGProfileInteractorImpl
    ): ManageWGProfileInteractor

    @Binds
    @Singleton
    abstract fun bindGetInitialDataInteractorImpl(
        getInitialDataInteractorImpl: GetInitialDataInteractorImpl
    ): GetInitialDataInteractor

    @Binds
    @Singleton
    abstract fun bindGetCalendarDataInteractorImpl(
        getCalendarDataInteractorImpl: GetCalendarDataInteractorImpl
    ): GetCalendarDataInteractor

    @Binds
    @Singleton
    abstract fun bindManageAppointmentInteractorImpl(
        manageAppointmentInteractorImpl: ManageAppointmentInteractorImpl
    ): ManageAppointmentInteractor

    @Binds
    @Singleton
    abstract fun bindManageTaskInteractorImpl(
        manageTaskInteractorImpl: ManageTaskInteractorImpl
    ): ManageTaskInteractor

    @Binds
    @Singleton
    abstract fun bindRemoteUpdateInteractorImpl(
        remoteUpdateInteractorImpl: RemoteUpdateInteractorImpl
    ): RemoteUpdateInteractor

    @Binds
    @Singleton
    abstract fun bindManageDeviceInteractorImpl(
        manageDeviceInteractorImpl: ManageDeviceInteractorImpl
    ): ManageDeviceInteractor
}
