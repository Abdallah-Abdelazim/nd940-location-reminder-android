package com.abdallah_abdelazim.locationreminder

import android.app.Application
import com.abdallah_abdelazim.locationreminder.di.myModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LocationReminderApp : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoinDi()
    }

    /**
     * use Koin Library as a service locator.
     */
    private fun setupKoinDi() {
        startKoin {
            androidContext(this@LocationReminderApp)
            modules(myModule)
        }
    }
}