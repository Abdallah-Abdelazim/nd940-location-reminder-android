package com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.abdallah_abdelazim.locationreminder.base.BaseViewModel
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.Result
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val reminderDataSource: ReminderDataSource
) : BaseViewModel(app) {

    // list that holds the reminder data to be displayed on the UI
    private val _remindersList = MutableLiveData<List<ReminderUiModel>>()
    val remindersList: LiveData<List<ReminderUiModel>>
        get() = _remindersList

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any.
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            val result = reminderDataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success -> {
                    val dataList = mutableListOf<ReminderUiModel>()
                    dataList.addAll(result.data.map { reminderDto ->
                        // map the reminderDto data from the DB to the be ready to be displayed on the UI
                        ReminderUiModel(
                            reminderDto.title,
                            reminderDto.description,
                            reminderDto.location,
                            reminderDto.latitude,
                            reminderDto.longitude,
                            reminderDto.id
                        )
                    })
                    _remindersList.value = dataList
                }
                is Result.Error -> showSnackBar.value = result.message
            }

            // check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty.
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }
}