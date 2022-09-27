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


    // Executes each task synchronously
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataBase: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in memory database , the information stored here should disappears when the process is done
        reminderDataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = reminderDataBase.close()

    @Test
    fun testInsertRetrieveData() = runBlockingTest {

        val data = ReminderDTO("title", "description", "location", 31.00, 24.00)

        reminderDataBase.reminderDao().saveReminder(data)

        val List = reminderDataBase.reminderDao().getReminders()
        MatcherAssert.assertThat(List.size, `is`(1))

        val loadedData = List[0]
        MatcherAssert.assertThat(loadedData.id, `is`(data.id))
        MatcherAssert.assertThat(loadedData.title, `is`(data.title))
        MatcherAssert.assertThat(loadedData.description, `is`(data.description))
        MatcherAssert.assertThat(loadedData.location, `is`(data.location))
        MatcherAssert.assertThat(loadedData.latitude, `is`(data.latitude))
        MatcherAssert.assertThat(loadedData.longitude, `is`(data.longitude))

    }

}