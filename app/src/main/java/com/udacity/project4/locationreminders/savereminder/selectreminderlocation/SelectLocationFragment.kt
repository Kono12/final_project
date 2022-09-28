package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Transformations.map
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var myMarker: Marker? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.MyMap) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.BTN.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {

        myMarker?.let {
            _viewModel.reminderSelectedLocationStr.value = it.title
            _viewModel.latitude.value = it.position.latitude
            _viewModel.longitude.value = it.position.longitude
        }
        findNavController().popBackStack()
    }


    @SuppressLint("MissingPermission")
    private fun enableMapMyLocation() {
        map.isMyLocationEnabled = true
        addLastLocationCallback()
    }

    @SuppressLint("MissingPermission")
    private fun addLastLocationCallback() {

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val lastLocationTask = fusedLocationProviderClient.lastLocation

        // On complete zoom to user's location and add marker
        lastLocationTask.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                val result = task.result
                result?.run {
                    val latLng = LatLng(latitude, longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            latLng,
                            15F
                        )
                    )
                    addMarker(latLng)
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
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

    override fun onMapReady(p0: GoogleMap?) {

        if (p0 != null) {
            map = p0
        }

        // marker
        map.setOnMapLongClickListener { latLng ->
            addMarker(latLng)
            myMarker!!.showInfoWindow()
        }

        // todo: poi marker
        map.setOnPoiClickListener { poi ->
            addPoiMarker(poi)
            myMarker!!.showInfoWindow()
        }

        // todo: map style
         setMapStyle(map)

        var perm =ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        if (perm == PackageManager.PERMISSION_GRANTED) {
            enableMapMyLocation()
        } else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                1)
        }
    }

    private fun addMarker(latLng: LatLng) {
        //  Additional text that's displayed under the title.
        val snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude)

        if (myMarker!=null){
            myMarker!!.remove()
        }
     //   myMarker?.remove()
        myMarker = map.addMarker(MarkerOptions()
            .position(latLng)
            .title(getString(R.string.dropped_pin))
            .snippet(snippet)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    private fun addPoiMarker(poi: PointOfInterest) {
        if (myMarker!=null){
            myMarker!!.remove()
        }
        myMarker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                enableMapMyLocation()

            } else {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
                // my cutome style + I have changed the save button theme too
            val style = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.my_map))
        }catch (e:Exception){}
    }
}
