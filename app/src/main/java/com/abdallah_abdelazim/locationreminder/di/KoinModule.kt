package com.abdallah_abdelazim.locationreminder.di

import com.abdallah_abdelazim.locationreminder.feature.authentication.AuthenticationViewModel
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersDatabase
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersLocalRepository
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.RemindersListViewModel
import com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val myModule = module {
    viewModelOf(::AuthenticationViewModel)

    viewModel {
        RemindersListViewModel(
            androidApplication(),
            get()
        )
    }

    viewModel {
        SaveReminderViewModel(
            androidApplication(),
            get()
        )
    }

    single<ReminderDataSource> {
        RemindersLocalRepository(get())
    }

    single {
        RemindersDatabase.createRemindersDao(androidContext())
    }
}