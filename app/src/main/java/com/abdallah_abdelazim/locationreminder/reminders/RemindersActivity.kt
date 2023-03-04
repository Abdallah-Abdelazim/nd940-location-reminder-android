package com.abdallah_abdelazim.locationreminder.reminders

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRemindersBinding.inflate(layoutInflater)

        setContentView(binding.root)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.popBackStack()
                || super.onSupportNavigateUp()
    }
}
