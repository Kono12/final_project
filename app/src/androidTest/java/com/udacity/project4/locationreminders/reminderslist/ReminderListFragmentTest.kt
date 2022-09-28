package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    // creating repo and context to be used in our test
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    //initializing Koin related code to be able to use it in out testing
    @Before
    fun init() {
        stopKoin()//stop the original app koin

        // initialising the context object
        appContext = getApplicationContext()
        //declaring module
        val modulee = module {
            //instance of RemindersLinstViewModel
            viewModel {

                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            //instance of SaveReminderViewModel
            single {
                SaveReminderViewModel(appContext, get() as ReminderDataSource)
            }
            //instance of  RemindersLocalRepository
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            //instance (returns dao)
            single { LocalDB.createRemindersDao(appContext) }
        }

        //starting koin with declared module
        startKoin {
            modules(listOf(modulee))
        }

        //Getting our real repository
        repository = get()

        //clearing data
        runBlocking {
            repository.deleteAllReminders()
        }
    }


     // Idling resources tell Espresso that the app is idle or busy.
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

     // Unregister your Idling Resource so it can be garbage collected
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    //testing navigations
    @Test
    fun testFragmentsNavigation() {

        // launching our fragment (ReminderListFragment)
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        //creating mock object with our nav controller with our fragment
        val navController = Mockito.mock(NavController::class.java)
        //assign the navController to our fragment
        fragmentScenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        // clicking on button addReminder to navigate
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        //verify that the navigation was called with no problem
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun testNoDataDisplayed() {
        //openning our fragment
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        // making sure that if there is no data it prints No Data
        Espresso.onView(ViewMatchers.withText(R.string.no_data)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }
}