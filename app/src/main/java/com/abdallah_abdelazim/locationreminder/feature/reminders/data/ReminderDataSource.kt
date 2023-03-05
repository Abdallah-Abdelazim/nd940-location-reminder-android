package com.abdallah_abdelazim.locationreminder.feature.reminders.data

import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {

    suspend fun getReminders(): Result<List<ReminderDto>>

    suspend fun saveReminder(reminder: ReminderDto)

    suspend fun getReminder(id: String): Result<ReminderDto>

    suspend fun deleteAllReminders()
}