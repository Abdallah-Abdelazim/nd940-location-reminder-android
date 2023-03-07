package com.abdallah_abdelazim.locationreminder.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.data.FakeDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.Result
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.ReminderUiModel
import com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder.SaveReminderViewModel
import com.abdallah_abdelazim.locationreminder.utils.MainCoroutineScopeRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var remindersList: MutableList<ReminderDto>
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private val testReminder1 = ReminderUiModel(
        "TestReminder1",
        "TestDescription1",
        "TestLocation1",
        1.0,
        1.0
    )
    private val testReminderWithNullTitle = ReminderUiModel(
        null,
        "TestDescription2",
        "TestLocation2",
        2.0,
        2.0
    )
    private val testReminderWithEmptyTitle = ReminderUiModel(
        "",
        "TestDescription3",
        "TestLocation3",
        3.0,
        3.0
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineScopeRule = MainCoroutineScopeRule()

    @Before
    fun setupSaveReminderViewModel() {
        stopKoin()
        remindersList = mutableListOf()
        fakeDataSource = FakeDataSource(remindersList)
        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun testSaveReminder_valid() = mainCoroutineScopeRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()

        viewModel.validateAndSaveReminder(testReminder1)

        val savedReminder =
            (fakeDataSource.getReminder(testReminder1.id) as? Result.Success<ReminderDto>)?.data

        assertNotNull(savedReminder)
    }

    @Test
    fun testSaveReminder_notValidNullTitle() = mainCoroutineScopeRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()

        viewModel.validateAndSaveReminder(testReminderWithNullTitle)

        val savedReminder =
            (fakeDataSource.getReminder(testReminder1.id) as? Result.Success<ReminderDto>)?.data

        assertNull(savedReminder)
    }

    @Test
    fun testSaveReminder_notValidEmptyTitle() = mainCoroutineScopeRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()

        viewModel.validateAndSaveReminder(testReminderWithEmptyTitle)

        val savedReminder =
            (fakeDataSource.getReminder(testReminder1.id) as? Result.Success<ReminderDto>)?.data

        assertNull(savedReminder)
    }

    @Test
    fun testSaveReminderErrorSnackbar_notValidEmptyTitle() {
        viewModel.validateAndSaveReminder(testReminderWithEmptyTitle)

        MatcherAssert.assertThat(viewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))
    }

}