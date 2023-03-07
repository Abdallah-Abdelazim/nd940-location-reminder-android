package com.abdallah_abdelazim.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.Result
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersDatabase
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderDatabase: RemindersDatabase

    private lateinit var repository: RemindersLocalRepository

    private val testReminder = ReminderDto(
        "TestTitle",
        "TestDescription",
        "TestLocation",
        1.0,
        1.0
    )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupRoomDatabase() {
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(reminderDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeRoomDatabase() {
        reminderDatabase.close()
    }

    @Test
    fun testSaveReminder() = runBlocking {
        repository.deleteAllReminders()

        repository.saveReminder(testReminder)

        val savedReminder =
            (repository.getReminder(testReminder.id) as? Result.Success<ReminderDto>)?.data

        assertEquals(testReminder, savedReminder)
    }

    @Test
    fun testGetReminder_norFoundError() = runBlocking {
        repository.deleteAllReminders()

        val result = repository.getReminder(testReminder.id)

        assertTrue { result is Result.Error }
    }

}