package com.abdallah_abdelazim.locationreminder.feature.reminders.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto

/**
 * The Room Database that contains the reminders table.
 */
@Database(entities = [ReminderDto::class], version = 1, exportSchema = false)
abstract class RemindersDatabase : RoomDatabase() {

    abstract fun reminderDao(): RemindersDao

    companion object {

        /**
         * Static method that creates a [RemindersDatabase] and returns the DAO of the reminders.
         */
        fun createRemindersDao(context: Context): RemindersDao {
            return Room.databaseBuilder(
                context.applicationContext,
                RemindersDatabase::class.java, "location_reminders.db"
            ).build().reminderDao()
        }

    }
}