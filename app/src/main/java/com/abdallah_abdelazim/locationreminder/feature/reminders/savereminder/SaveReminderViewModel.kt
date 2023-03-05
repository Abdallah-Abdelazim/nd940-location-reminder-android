package com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.base.BaseViewModel
import com.abdallah_abdelazim.locationreminder.base.NavigationCommand
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.ReminderDataSource
import com.abdallah_abdelazim.locationreminder.feature.reminders.data.dto.ReminderDto
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.ReminderUiModel
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val reminderDataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPoi = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPoi.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderUiModel) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderUiModel) {
        showLoading.value = true
        viewModelScope.launch {
            reminderDataSource.saveReminder(
                ReminderDto(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderUiModel): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}