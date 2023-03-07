package com.abdallah_abdelazim.locationreminder.data

import com.abdallah_abdelazim.locationreminder.feature.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.Result

/**
 * FakeDataSource acts as a test double to the LocalDataSource.
 */
class FakeDataSource(private val remindersList: MutableList<ReminderDto> = mutableListOf()) :
    ReminderDataSource {

    var shouldError = false

    override suspend fun getReminders(): Result<List<ReminderDto>> = if (shouldError) {
        Result.Error("ERROR")
    } else {
        Result.Success(remindersList)
    }

    override suspend fun saveReminder(reminder: ReminderDto) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDto> = if (shouldError) {
        Result.Error("ERROR")
    } else {
        val reminder = remindersList.find { it.id == id }

        if (reminder != null) Result.Success(reminder)
        else Result.Error("Not found")
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }

}