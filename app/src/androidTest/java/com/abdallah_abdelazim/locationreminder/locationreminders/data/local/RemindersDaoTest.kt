package com.abdallah_abdelazim.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testReminderDto = ReminderDto(
        "TestTitle",
        "TestDescription",
        "TestLocation",
        1.0,
        1.0
    )

    @Before
    fun setupRoomDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeRoomDatabase() {
        database.close()
    }

    @Test
    fun testSaveReminder() = runBlockingTest {
        database.reminderDao().saveReminder(testReminderDto)

        val savedReminderDto = database.reminderDao().getReminderById(testReminderDto.id)

        assertNotNull(savedReminderDto)
        assertEquals(testReminderDto, savedReminderDto)
    }

    @Test
    fun testDeleteReminders() = runBlockingTest {

        database.reminderDao().saveReminder(testReminderDto)

        database.reminderDao().deleteAllReminders()

        assertEquals(0, database.reminderDao().getReminders().size)
    }

}