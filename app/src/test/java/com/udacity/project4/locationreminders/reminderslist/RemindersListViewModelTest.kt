package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.MainCoroutine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    //provide testing to the RemindersListViewModel and its data objects
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersViewModel: RemindersListViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutine = MainCoroutine()

    // to get new instance of view model in each test
    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        remindersViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource)
    }

    @Test
    fun testShouldReturn_Error () = runBlockingTest  {

        fakeDataSource.setShouldReturnError(true)
        // creating reminder for testing
        saveReminderFakeData()
        //loading reminders from DB
        remindersViewModel.loadReminders()
        //making sure snack bar says couldn't find reminders when result is error
        MatcherAssert.assertThat(
            remindersViewModel.showSnackBar.value, CoreMatchers.`is`("couldn't find reminders")
        )
    }

    @Test
    fun check_loading() = runBlockingTest {
        //pausing our dispatcher so loading is visible
        mainCoroutine.pauseDispatcher()
        saveReminderFakeData()
        remindersViewModel.loadReminders()
        //it should stay visible untill now
        MatcherAssert.assertThat(remindersViewModel.showLoading.value, CoreMatchers.`is`(true))
        //after resuming loading should diappear
        mainCoroutine.resumeDispatcher()
        MatcherAssert.assertThat(remindersViewModel.showLoading.value, CoreMatchers.`is`(false))
    }

    private suspend fun saveReminderFakeData() {
        fakeDataSource.saveReminder(ReminderDTO("title", "description", "location", 31.00, 24.00))
    }

    @After
    fun tearDown() {
        //closing our koin after done
        stopKoin()
    }

}