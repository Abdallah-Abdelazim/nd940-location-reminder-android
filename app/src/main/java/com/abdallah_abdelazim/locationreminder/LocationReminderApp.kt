package com.abdallah_abdelazim.locationreminder

import android.app.Application
import com.abdallah_abdelazim.locationreminder.authentication.AuthenticationViewModel
import com.abdallah_abdelazim.locationreminder.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.reminders.data.local.LocalDB
import com.abdallah_abdelazim.locationreminder.reminders.data.local.RemindersLocalRepository
import com.abdallah_abdelazim.locationreminder.reminders.reminderslist.RemindersListViewModel
import com.abdallah_abdelazim.locationreminder.reminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

class LocationReminderApp : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoinDi()
    }

    /**
     * use Koin Library as a service locator
     */
    private fun setupKoinDi() {

        val myModule = module {
            viewModelOf(::AuthenticationViewModel)

            viewModel {
                RemindersListViewModel(
                    androidApplication(),
                    get()
                )
            }

            single {
                SaveReminderViewModel(
                    androidApplication(),
                    get()
                )
            }

            single<ReminderDataSource> {
                RemindersLocalRepository(get())
            }

            single {
                LocalDB.createRemindersDao(this@LocationReminderApp)
            }
        }

        startKoin {
            androidContext(this@LocationReminderApp)
            modules(myModule)
        }
    }
}