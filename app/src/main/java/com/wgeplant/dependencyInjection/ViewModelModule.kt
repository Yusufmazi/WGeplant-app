package com.wgeplant.dependencyInjection

import com.wgeplant.ui.auth.IRegisterViewModel
import com.wgeplant.ui.auth.IStartViewModel
import com.wgeplant.ui.auth.RegisterViewModel
import com.wgeplant.ui.auth.StartViewModel
import com.wgeplant.ui.calendar.CalendarViewModel
import com.wgeplant.ui.calendar.ICalendarViewModel
import com.wgeplant.ui.calendar.entry.AppointmentViewModel
import com.wgeplant.ui.calendar.entry.IAppointmentViewModel
import com.wgeplant.ui.calendar.entry.ITaskViewModel
import com.wgeplant.ui.calendar.entry.TaskViewModel
import com.wgeplant.ui.toDo.IToDoViewModel
import com.wgeplant.ui.toDo.ToDoViewModel
import com.wgeplant.ui.wg.ChooseWGViewModel
import com.wgeplant.ui.wg.CreateWGViewModel
import com.wgeplant.ui.wg.IChooseWGViewModel
import com.wgeplant.ui.wg.ICreateWGViewModel
import com.wgeplant.ui.wg.IJoinWGViewModel
import com.wgeplant.ui.wg.JoinWGViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    abstract fun bindStartViewModel(
        startViewModel: StartViewModel
    ): IStartViewModel

    @Binds
    abstract fun bindRegisterViewModel(
        registerViewModel: RegisterViewModel
    ): IRegisterViewModel

    @Binds
    abstract fun bindAppointmentViewModel(
        appointmentViewModel: AppointmentViewModel
    ): IAppointmentViewModel

    @Binds
    abstract fun bindTaskViewModel(
        taskViewModel: TaskViewModel
    ): ITaskViewModel

    @Binds
    abstract fun bindCalendarViewModel(
        calendarViewModel: CalendarViewModel
    ): ICalendarViewModel

    @Binds
    abstract fun bindChooseWGViewModel(
        chooseWGViewModel: ChooseWGViewModel
    ): IChooseWGViewModel

    @Binds
    abstract fun bindJoinWGViewModel(
        joinWGViewModel: JoinWGViewModel
    ): IJoinWGViewModel

    @Binds
    abstract fun bindCreateWGViewModel(
        createWGViewModel: CreateWGViewModel
    ): ICreateWGViewModel

    @Binds
    abstract fun bindToDoViewModel(
        toDoViewModel: ToDoViewModel
    ): IToDoViewModel
}
