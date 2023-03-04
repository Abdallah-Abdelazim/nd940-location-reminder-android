package com.abdallah_abdelazim.locationreminder.reminders.data

import com.abdallah_abdelazim.locationreminder.reminders.data.dto.ReminderDTO
import com.abdallah_abdelazim.locationreminder.reminders.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Result<List<ReminderDTO>>
    suspend fun saveReminder(reminder: ReminderDTO)
    suspend fun getReminder(id: String): Result<ReminderDTO>
    suspend fun deleteAllReminders()
}