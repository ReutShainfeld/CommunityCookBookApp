package com.cookbook.app.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cookbook.app.R
import com.cookbook.app.databinding.FragmentMapBinding
import com.cookbook.app.model.Recipe
import com.cookbook.app.utils.Constants
import com.cookbook.app.viewmodel.RecipeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val recipeViewModel: RecipeViewModel by viewModels()
    private var recipesList = mutableListOf<Recipe>()


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showCurrentLocation()
            } else {
                showPermissionDeniedMessage()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Constants.startLoading(requireActivity())
        recipeViewModel.allRecipes.observe(requireActivity()) { recipes ->
            if (recipes.isNotEmpty()) {
                Constants.dismiss()
                recipesList.clear()
                recipesList.addAll(recipes)

                if (isAdded) {
                    showMarkersOnMap()
                }
            } else {
                Constants.dismiss()
            }
        }
        recipeViewModel.fetchAllRecipes()
    }

    private fun showMarkersOnMap() {
        if (recipesList.isNotEmpty()) {
            if(isAdded){
                requireActivity().runOnUiThread {
                    for (recipe in recipesList) {
                        val latLng = LatLng(recipe.location!!.latitude, recipe.location!!.longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Recipe at ${recipe.location!!.latitude}, ${recipe.location!!.longitude}")
                        )
                        marker?.tag = recipe
                    }

                    googleMap.setOnMarkerClickListener { marker ->
                        val recipe = marker.tag as? Recipe
                        if (recipe != null) {
                            val bundle = Bundle().apply {
                                putSerializable("recipe", recipe)
                            }
                            findNavController().navigate(R.id.action_map_to_recipe_detail, bundle)
                        }
                        true
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                showCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationale()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(32.109333, 34.855499)

                googleMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }

    private fun showPermissionRationale() {
        Toast.makeText(
            requireContext(),
            "Location permission is required to show your current location.",
            Toast.LENGTH_LONG
        ).show()

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            requireContext(),
            "Permission denied. Cannot show your location.",
            Toast.LENGTH_LONG
        ).show()
    }
}