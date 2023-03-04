package com.abdallah_abdelazim.locationreminder.reminders.reminderslist

import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.base.BaseRecyclerViewAdapter


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}