package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Transformations.map
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    //creating our objects
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application


    // run before each test
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        //declaring module
        val myModule = module {
            //instance of RemindersLinstViewModel
            viewModel {
                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            //instance of SaveReminderViewModel
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            //instance of  RemindersLocalRepository
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            //instance (returns dao)
            single { LocalDB.createRemindersDao(appContext) }
        }
        //starting koin with declared module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    // run before each test
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }


    // Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun testErrorEnterTitleSnackBar() {
        // openning our activity (RemindersActivity)
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // clicking add then save with no inputs
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        // making sure that snak bar is deisplayed with a message that says Pleae enter title
        Espresso.onView(ViewMatchers.withText(snackBarMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //closing our activity after done
        activityScenario.close()
    }


    // creating a method that returns instance of ReminderActivity
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


    @Test
    fun testReminderSavedToastMessage() {
        // openning activity RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // getting instance of it
        val activity = getActivity(activityScenario)

        //clicking add and entering all fields then clicking save
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description"))
        Espresso.closeSoftKeyboard()
        //maing sure it's the same fields we entered
        Espresso.onView(ViewMatchers.withText("Title"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("Description")).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
        //entering the location
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.MyMap)).perform(ViewActions.longClick())
        Espresso.onView(ViewMatchers.withId(R.id.BTN))
            .perform(ViewActions.click()) // save button from (select Location)
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder))
            .perform(ViewActions.click()) // save from (save reminder fragment)
        // toast with message Reminder Saved !
        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                // matching toast token
                CoreMatchers.not(CoreMatchers.`is`(activity?.window?.decorView))
            )
        )
            .check(
                ViewAssertions.matches(
                    // toast is displayed
                    ViewMatchers.isDisplayed()
                )
            )
        //closing it
        activityScenario.close()

    }


}
