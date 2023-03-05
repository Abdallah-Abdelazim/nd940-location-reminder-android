package com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist

import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.base.BaseRecyclerViewAdapter

class RemindersListAdapter(callBack: (selectedReminder: ReminderUiModel) -> Unit) :
    BaseRecyclerViewAdapter<ReminderUiModel>(callBack) {

    override fun getLayoutRes(viewType: Int) = R.layout.item_reminder
}