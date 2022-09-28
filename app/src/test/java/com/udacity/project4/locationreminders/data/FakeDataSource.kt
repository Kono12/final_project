package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    // fake data source to act as a double to the real data source
    var reminderList = mutableListOf<ReminderDTO>()

    private var foundError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try {
            if (foundError) {
                //if there is an error we throw this exeption
                throw Exception("couldn't find reminders")
            }
            // if no error we get list of reminders
            return Result.Success(ArrayList(reminderList))
        } catch (ex: Exception) {
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //add the reminder to the list
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            //getting a specific reminder with the id we passed
            val reminder = reminderList.find { it.id == id }
            if (foundError || reminder == null) {
                //exeption if no reminder was found
                throw Exception("Not found $id")
            } else {
                //getting the reminder
                return Result.Success(reminder)
            }
        } catch (ex: Exception){
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        //clearing the list of it's reminders
        reminderList.clear()
    }

    fun setShouldReturnError(value: Boolean) {
        //used when knowing that an error is happening here
        //used in tests
        foundError = value
    }

}