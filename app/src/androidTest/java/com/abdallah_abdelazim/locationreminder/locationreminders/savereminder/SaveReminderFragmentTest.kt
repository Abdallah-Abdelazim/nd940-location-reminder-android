package com.abdallah_abdelazim.locationreminder.locationreminders.savereminder

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersDatabase
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersLocalRepository
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.ReminderUiModel
import com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder.SaveReminderFragment
import com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder.SaveReminderViewModel
import com.abdallah_abdelazim.locationreminder.util.DataBindingIdlingResource
import com.abdallah_abdelazim.locationreminder.util.wrapEspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.mock

class SaveReminderFragmentTest : KoinTest {

    private lateinit var reminderDataSource: ReminderDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private val testReminder = ReminderUiModel(
        "TestTitle",
        "TestDescription",
        "TestLocation",
        1.0,
        1.0
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()

        val koinModule = module {
            single { RemindersDatabase.createRemindersDao(ApplicationProvider.getApplicationContext()) }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }

            viewModel {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(koinModule)
        }
        reminderDataSource = get()

        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)

        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Test
    fun testSaveReminderWithEmptyTitle_showErrorSnackbar() {
        val mockedNavController = mock(NavController::class.java)

        val scenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle.EMPTY,
            R.style.Theme_LocationReminder
        )

        monitorSaveReminderFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }

        onView(withId(R.id.saveReminder))
            .perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun testValidateAbdSaveReminder_saveSuccess() {
        val mockedNavController = mock(NavController::class.java)

        val scenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle.EMPTY,
            R.style.Theme_LocationReminder
        )
        monitorSaveReminderFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }

        wrapEspressoIdlingResource {
            viewModel.validateAndSaveReminder(testReminder)
        }

        onView(withId(R.id.saveReminder)).perform(click())
        assertThat(viewModel.showToast.value, `is`(getResourceString(R.string.reminder_saved)))
    }

    private fun monitorSaveReminderFragment(fragmentScenario: FragmentScenario<SaveReminderFragment>) {
        fragmentScenario.onFragment { fragment ->
            dataBindingIdlingResource.activity = fragment.requireActivity()
        }
    }

    private fun getResourceString(@StringRes id: Int): String {
        val targetContext: Context = ApplicationProvider.getApplicationContext()
        return targetContext.resources.getString(id)
    }

}