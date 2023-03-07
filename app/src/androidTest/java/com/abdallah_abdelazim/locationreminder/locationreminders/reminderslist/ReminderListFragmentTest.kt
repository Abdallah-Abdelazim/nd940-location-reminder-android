package com.abdallah_abdelazim.locationreminder.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersDatabase
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersLocalRepository
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.ReminderListFragment
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.ReminderListFragmentDirections
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.RemindersListViewModel
import com.abdallah_abdelazim.locationreminder.util.DataBindingIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private lateinit var reminderDataSource: ReminderDataSource

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private val testReminderDto = ReminderDto(
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
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get()
                )
            }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(koinModule)
        }

        reminderDataSource = get()

        runBlocking { reminderDataSource.deleteAllReminders() }
    }

    @Test
    fun testDisplayReminderInUi() = runBlockingTest {
        runBlocking { reminderDataSource.saveReminder(testReminderDto) }

        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle.EMPTY,
            R.style.Theme_LocationReminder
        )
        val mockedNavController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }
        monitorReminderListFragment(scenario)

        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))

        Espresso.onView(withText(testReminderDto.title))
            .check(ViewAssertions.matches(withText("TestTitle")))

        Espresso.onView(withText(testReminderDto.description))
            .check(ViewAssertions.matches(withText("TestDescription")))

        Espresso.onView(withText(testReminderDto.location))
            .check(ViewAssertions.matches(withText("TestLocation")))
    }

    @Test
    fun testEmptyRemindersList() {
        launchFragmentInContainer<ReminderListFragment>(
            Bundle.EMPTY,
            R.style.Theme_LocationReminder
        )

        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testNavigationToAddReminder() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.Theme_LocationReminder
        )

        val mockedNavController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }

        Espresso.onView(withId(R.id.addReminderFAB))
            .perform(ViewActions.click())

        Mockito.verify(mockedNavController)
            .navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    private fun monitorReminderListFragment(fragmentScenario: FragmentScenario<ReminderListFragment>) {
        fragmentScenario.onFragment { fragment ->
            dataBindingIdlingResource.activity = fragment.requireActivity()
        }
    }

}