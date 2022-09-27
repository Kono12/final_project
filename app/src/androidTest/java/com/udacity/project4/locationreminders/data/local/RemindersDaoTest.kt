package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
//comments done here (for me to not come back here)

    // Executes each task synchronously
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataBase: RemindersDatabase

    // Using an in memory database , the information stored here should disappears when the process is done
    @Before
    fun initDb() {
        //making DB and using Application provider.getApplicationContext to get context
        // and running it before every test
        reminderDataBase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).build()
    }

    //closing the database after use and running it after every test
    @After
    fun closeDb() = reminderDataBase.close()

    @Test
    fun testInsertRetrieveData() = runBlockingTest {

        //making object of ReminderDTD with these attributes for testing
        val data = ReminderDTO("title", "description", "location", 31.00, 24.00)

            // saving the object into out DB
        reminderDataBase.reminderDao().saveReminder(data)

        // getting the DB data inside list
        val List = reminderDataBase.reminderDao().getReminders()

        // at this point here we just added one reminder so we should have size of 1
        MatcherAssert.assertThat(List.size, `is`(1))

        //checking if that one reminder is the same reminder that we have just added
        val loadedData = List[0]
        MatcherAssert.assertThat(loadedData.id, `is`(data.id))
        MatcherAssert.assertThat(loadedData.title, `is`(data.title))
        MatcherAssert.assertThat(loadedData.description, `is`(data.description))
        MatcherAssert.assertThat(loadedData.location, `is`(data.location))
        MatcherAssert.assertThat(loadedData.latitude, `is`(data.latitude))
        MatcherAssert.assertThat(loadedData.longitude, `is`(data.longitude))

    }

}