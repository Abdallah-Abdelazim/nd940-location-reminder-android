package com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.abdallah_abdelazim.locationreminder.BuildConfig
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.base.BaseFragment
import com.abdallah_abdelazim.locationreminder.base.NavigationCommand
import com.abdallah_abdelazim.locationreminder.databinding.FragmentSaveReminderBinding
import com.abdallah_abdelazim.locationreminder.feature.reminders.geofence.GeofenceBroadcastReceiver
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SaveReminderFragment : BaseFragment() {

    override val viewModel: SaveReminderViewModel by activityViewModel()

    private var _binding: FragmentSaveReminderBinding? = null
    private val binding get() = _binding!!

    private val isQOrLater = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)

    private val locationSettingsResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK)
            checkLocationSettingsThenAddReminderWithGeofence()
    }

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_save_reminder, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            viewModel.navigationCommand.value = NavigationCommand.To(
                SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
            )
        }

        binding.saveReminder.setOnClickListener {
            val reminder = viewModel.reminderUiModel
            if (viewModel.validateEnteredData(reminder)) {
                startAddReminderWithGeofenceFlow()
            }
        }
    }

    private fun startAddReminderWithGeofenceFlow() {
        if (hasForegroundAndBackgroundLocationPermissions())
            checkLocationSettingsThenAddReminderWithGeofence()
        else
            requestLocationPermissions()
    }

    @SuppressLint("InlinedApi")
    private fun hasForegroundAndBackgroundLocationPermissions(): Boolean {
        val hasForegroundPermission = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasBackgroundPermission = if (isQOrLater) {
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return hasForegroundPermission && hasBackgroundPermission
    }

    @SuppressLint("InlinedApi")
    private fun requestLocationPermissions() {
        val (requestCode, permissions) = if (isQOrLater) {
            RC_ACCESS_FOREGROUND_AND_BACKGROUND_PERMISSIONS to arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            RC_ACCESS_FOREGROUND_PERMISSION to arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        requestPermissions(permissions, requestCode)
    }

    private fun checkLocationSettingsThenAddReminderWithGeofence() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_LOW_POWER,
            1000
        ).build()

        val locationSettingsRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(locationSettingsRequest)

        locationSettingsResponseTask.addOnCompleteListener { task ->

            if (task.isSuccessful) addReminderWithGeofence()
            else Log.e(TAG, "Location settings task unsuccessful")

        }.addOnFailureListener { exc ->

            if (exc is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exc.resolution).build()
                    locationSettingsResultLauncher.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Error in getting location settings response: ${e.message}")
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.enable) {
                    checkLocationSettingsThenAddReminderWithGeofence()
                }.show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addReminderWithGeofence() {
        val reminder = viewModel.reminderUiModel

        // Create a Geofence for the reminder
        with(reminder) {
            if (longitude != null && latitude != null) {
                val geofence = Geofence.Builder()
                    .setRequestId(id)
                    .setCircularRegion(
                        reminder.latitude!!, reminder.longitude!!,
                        GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

                val geofencePendingIntent = run {
                    val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
                    intent.action = ACTION_GEOFENCE_TRIGGERED
                    PendingIntent.getBroadcast(
                        requireContext(),
                        0,
                        intent,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        } else {
                            PendingIntent.FLAG_UPDATE_CURRENT
                        }
                    )
                }
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener {

                        Log.d(TAG, "Geofence added with ID: ${geofence.requestId}")

                    }.addOnFailureListener { e ->

                        Log.e(TAG, "Error adding geofence", e)

                        Toast.makeText(
                            requireActivity(),
                            R.string.geofences_not_added,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            } else Log.e(TAG, "Geofence wasn't created as reminder latitude & longitude were null")
        }

        viewModel.validateAndSaveReminder(reminder)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (
            grantResults.isEmpty() ||
            grantResults[PERMISSION_INDEX_ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_DENIED ||
            (requestCode == RC_ACCESS_FOREGROUND_AND_BACKGROUND_PERMISSIONS &&
                    grantResults[PERMISSION_INDEX_ACCESS_BACKGROUND_LOCATION] == PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        } else {
            checkLocationSettingsThenAddReminderWithGeofence()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG = SaveReminderFragment::class.simpleName

        private const val RC_ACCESS_FOREGROUND_AND_BACKGROUND_PERMISSIONS = 100
        private const val RC_ACCESS_FOREGROUND_PERMISSION = 200

        private const val PERMISSION_INDEX_ACCESS_FINE_LOCATION = 0
        private const val PERMISSION_INDEX_ACCESS_BACKGROUND_LOCATION = 1

        private const val GEOFENCE_RADIUS_IN_METERS = 100f

        const val ACTION_GEOFENCE_TRIGGERED = "ACTION_GEOFENCE_TRIGGERED"
    }
}
