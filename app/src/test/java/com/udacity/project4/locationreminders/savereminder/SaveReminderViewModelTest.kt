package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    // provide testing to the SaveReminderView and its live data objects
    private lateinit var reminderDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutine = MainCoroutine()

    // to get new instance of view model in each test
    @Before
    fun setupViewModel() {
        reminderDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun shouldReturnError() = runBlockingTest {

        //getting the result of the method validateEnterData with a testing data that has no title
        val result = saveReminderViewModel.validateEnteredData(createIncompleteReminderDataItem())
        // this should be an error
        MatcherAssert.assertThat(result, CoreMatchers.`is`(false))

    }

    private fun createIncompleteReminderDataItem(): ReminderDataItem {
        // creating fake reminder object
        return ReminderDataItem("", "description", "location", 31.00, 24.00)
    }

    @Test
    fun check_loading() = runBlockingTest {
        //pausing our dispatcher so loading is visible
        mainCoroutine.pauseDispatcher()
        saveReminderViewModel.saveReminder(createFakeReminderDataItem())
        //it should stay visible untill now
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.value, CoreMatchers.`is`(true))
        //after resuming loading should diappear
        mainCoroutine.resumeDispatcher()
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.value, CoreMatchers.`is`(false))
    }

    private fun createFakeReminderDataItem(): ReminderDataItem {
        return ReminderDataItem("hello", "description", "location", 31.00, 24.00)

    }

    @After
    fun tearDown() {
        // to stop koin
        stopKoin()
    }
}
