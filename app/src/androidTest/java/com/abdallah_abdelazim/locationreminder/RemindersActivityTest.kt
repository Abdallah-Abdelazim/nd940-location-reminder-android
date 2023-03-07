package com.abdallah_abdelazim.locationreminder

import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.abdallah_abdelazim.locationreminder.feature.reminders.RemindersActivity
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersDatabase
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.local.RemindersLocalRepository
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.RemindersListViewModel
import com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder.SaveReminderViewModel
import com.abdallah_abdelazim.locationreminder.util.DataBindingIdlingResource
import com.abdallah_abdelazim.locationreminder.util.EspressoIdlingResource
import com.abdallah_abdelazim.locationreminder.util.RecyclerViewItemCountAssertion
import com.abdallah_abdelazim.locationreminder.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
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


@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : KoinTest {

    private lateinit var repository: ReminderDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private val testReminderDto = ReminderDto(
        "TestTitle",
        "TestDescription",
        "TestLocation",
        1.0,
        1.0
    )

    @get:Rule
    var activityTestRule: ActivityTestRule<RemindersActivity> =
        ActivityTestRule(RemindersActivity::class.java)

    @Before
    fun init() {
        stopKoin()

        val koinModule = module {
            single { RemindersDatabase.createRemindersDao(ApplicationProvider.getApplicationContext()) }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }

            viewModel {
                RemindersListViewModel(ApplicationProvider.getApplicationContext(), get())
            }

            viewModel {
                SaveReminderViewModel(ApplicationProvider.getApplicationContext(), get())
            }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(koinModule)
        }

        repository = get()

        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), repository)

        runBlocking { repository.deleteAllReminders() }
    }

    @Before
    fun registerEspressoIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterEspressoIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun testAddReminder_showInRemindersList() = runBlocking {
        val activityScenario = launchActivity<RemindersActivity>()

        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB))
            .perform(ViewActions.click())

        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText(testReminderDto.title))

        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText(testReminderDto.description))

        Espresso.closeSoftKeyboard()

        onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        onView(withId(R.id.fragment_map)).perform(ViewActions.longClick())
        onView(withId(R.id.btn_save)).perform(ViewActions.click())

        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        onView(withId(R.id.remindersRecyclerView))
            .check(RecyclerViewItemCountAssertion(1));

        activityScenario.close()
    }
}