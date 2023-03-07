package com.abdallah_abdelazim.locationreminder.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abdallah_abdelazim.locationreminder.data.FakeDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.RemindersListViewModel
import com.abdallah_abdelazim.locationreminder.utils.MainCoroutineScopeRule
import com.abdallah_abdelazim.locationreminder.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersList: MutableList<ReminderDto>
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    private val testReminder1 = ReminderDto(
        "TestReminder1",
        "TestReminderDescription1",
        "TestReminderLocation1",
        1.0,
        1.0
    )
    private val testReminder2 = ReminderDto(
        "TestReminder2",
        "TestReminderDescription2",
        "TestReminderLocation2",
        2.0,
        2.0
    )
    private val testReminder3 = ReminderDto(
        "TestReminder3",
        "TestReminderDescription3",
        "TestReminderLocation3",
        3.0,
        3.0
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineScopeRule = MainCoroutineScopeRule()

    @Before
    fun setupRemindersListViewModel() {
        stopKoin()
        remindersList = mutableListOf(
            testReminder1, testReminder2, testReminder3
        )
        fakeDataSource = FakeDataSource(remindersList)
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun testLoadReminders_loadingProgress() = mainCoroutineScopeRule.runBlockingTest {
        mainCoroutineScopeRule.pauseDispatcher()

        viewModel.loadReminders()
        assertEquals(true, viewModel.showLoading.getOrAwaitValue())

        mainCoroutineScopeRule.resumeDispatcher()

        assertEquals(false, viewModel.showLoading.getOrAwaitValue())
    }

    @Test
    fun testLoadReminders_showNoData() = mainCoroutineScopeRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()

        viewModel.loadReminders()

        assertEquals(true, viewModel.showNoData.getOrAwaitValue())
    }

    @Test
    fun testLoadReminders_showsErrorMessage() = mainCoroutineScopeRule.runBlockingTest {
        fakeDataSource.shouldError = true

        viewModel.loadReminders()

        assertEquals("ERROR", viewModel.showSnackBar.value)
    }

}