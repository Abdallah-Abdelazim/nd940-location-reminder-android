package com.abdallah_abdelazim.locationreminder.feature.reminders

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.databinding.ActivityReminderDescriptionBinding
import com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist.ReminderUiModel

/**
 * Activity that displays the reminder details after the user clicks on the notification.
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)

        val reminder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_REMINDER_DATA_ITEM, ReminderUiModel::class.java)
        } else {
            intent.getSerializableExtra(EXTRA_REMINDER_DATA_ITEM) as ReminderUiModel
        }
        binding.reminder = reminder
    }

    companion object {

        private const val EXTRA_REMINDER_DATA_ITEM = "EXTRA_ReminderDataItem"

        fun newIntent(context: Context, reminderUiModel: ReminderUiModel): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_REMINDER_DATA_ITEM, reminderUiModel)
            return intent
        }
    }
}
