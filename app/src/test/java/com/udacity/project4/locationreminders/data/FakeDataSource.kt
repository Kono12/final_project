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
                throw Exception("couldn't find reminders")
            }
            return Result.Success(ArrayList(reminderList))
        } catch (ex: Exception) {
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            val reminder = reminderList.find { it.id == id }
            if (foundError || reminder == null) {
                throw Exception("Not found $id")
            } else {
                return Result.Success(reminder)
            }
        } catch (ex: Exception) {
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }

    fun setShouldReturnError(value: Boolean) {
        foundError = value
    }

}