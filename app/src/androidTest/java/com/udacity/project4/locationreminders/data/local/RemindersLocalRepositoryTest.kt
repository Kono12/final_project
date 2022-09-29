package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // getting objects from our real repo and DB to be used in out tests
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    // Using an in memory database , the information stored here disappears when the process is finished.
    @Before
    fun setup() {
        //creating the DB and making sure that it will run before each test case
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
         // same fot the repo
        repository = RemindersLocalRepository(remindersDatabase.reminderDao())
    }


    //closing out DB after done so that it's not in memory any more
    @After
    fun cleanUp() = remindersDatabase.close()


    // runBlocking is used here because the job isn't completed yet
    @Test
    fun testInsertRetrieveData() = runBlocking {
        // creating object to be added to our DB with testing attributes
        val data = ReminderDTO("title abc", "description abc", "location abc", 31.00, 24.00)

        // adding this reminder object to the DB
        repository.saveReminder(data)

        // getting our specific reminder
        val result = repository.getReminder(data.id) as Result.Success

        // our reminder should exist and not be null
        MatcherAssert.assertThat(result.data != null, CoreMatchers.`is`(true))

        // making sure it's the same reminder
        val loadedData = result.data
        MatcherAssert.assertThat(loadedData.id, CoreMatchers.`is`(data.id))
        MatcherAssert.assertThat(loadedData.title, CoreMatchers.`is`(data.title))
        MatcherAssert.assertThat(loadedData.description, CoreMatchers.`is`(data.description))
        MatcherAssert.assertThat(loadedData.location, CoreMatchers.`is`(data.location))
        MatcherAssert.assertThat(loadedData.latitude, CoreMatchers.`is`(data.latitude))
        MatcherAssert.assertThat(loadedData.longitude, CoreMatchers.`is`(data.longitude))
    }

    // getting a reminder that doesn't exist
    @Test
    fun testDataNotFound_returnError() = runBlocking {
        //we didn't add this id to our DB
        val result = repository.getReminder("155")
        val error = (result is Result.Error)
        // there should be an error
        MatcherAssert.assertThat(error, CoreMatchers.`is`(true))
    }

}