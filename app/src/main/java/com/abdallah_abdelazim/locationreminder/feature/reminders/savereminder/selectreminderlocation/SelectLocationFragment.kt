package com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.abdallah_abdelazim.locationreminder.BuildConfig
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.base.BaseFragment
import com.abdallah_abdelazim.locationreminder.databinding.FragmentSelectLocationBinding
import com.abdallah_abdelazim.locationreminder.feature.reminders.savereminder.SaveReminderViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val viewModel: SaveReminderViewModel by activityViewModel()

    private var _binding: FragmentSelectLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var selectedPoi: PointOfInterest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }
    }

    private fun onLocationSelected() {
        viewModel.selectedPoi.value = selectedPoi
        viewModel.reminderSelectedLocationStr.value = selectedPoi.name
        viewModel.latitude.value = selectedPoi.latLng.latitude
        viewModel.longitude.value = selectedPoi.latLng.longitude
        findNavController().popBackStack()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setupMap()
    }

    private fun setupMap() {
        setupMapCurrentLocation()
        setupPoiOnMap()
        setLongClickOnMap()
        setMapStyle()
    }

    @SuppressLint("MissingPermission")
    private fun setupMapCurrentLocation() {
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val lat = it.latitude
                        val long = it.longitude
                        val currentPosition = LatLng(lat, long)

                        map.addMarker(
                            MarkerOptions().position(currentPosition)
                                .title(getString(R.string.current_position_marker_title))
                        )

                        val zoom = 20f
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoom))
                    }
                }

        } else {
            requestLocationPermission()
        }
    }

    private fun setupPoiOnMap() {
        map.setOnPoiClickListener { poi ->
            selectedPoi = poi
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker!!.showInfoWindow()
            binding.btnSave.visibility = View.VISIBLE
        }
    }

    private fun setLongClickOnMap() {
        map.setOnMapLongClickListener { latLng ->
            val title = "(%.2f, %.2f)".format(latLng.latitude, latLng.longitude)
            selectedPoi = PointOfInterest(latLng, title, title)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            binding.btnSave.visibility = View.VISIBLE
        }
    }

    private fun setMapStyle() {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting map style", e)
        }
    }

    private fun isLocationPermissionGranted() = ActivityCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (shouldShowRationale) {
            Snackbar.make(
                binding.root,
                R.string.location_required_error,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.permission_denied_explanation) {
                requestPermissions(permissions, RC_LOCATION_PERMISSION)
            }.setDuration(Snackbar.LENGTH_LONG)
                .show()
        } else {
            requestPermissions(permissions, RC_LOCATION_PERMISSION)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            setupMapCurrentLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG = SelectLocationFragment::class.simpleName

        private const val RC_LOCATION_PERMISSION = 100
    }

}
