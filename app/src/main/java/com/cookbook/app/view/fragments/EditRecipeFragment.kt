package com.cookbook.app.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cookbook.app.R
import com.cookbook.app.databinding.FragmentEditRecipeBinding
import com.cookbook.app.model.Recipe
import com.cookbook.app.model.RecipeLocation
import com.cookbook.app.utils.Constants
import com.cookbook.app.viewmodel.RecipeViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditRecipeFragment : Fragment() {

    private lateinit var binding: FragmentEditRecipeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private val recipeViewModel: RecipeViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var recipeLocation: RecipeLocation? = null
    private var recipe: Recipe? = null


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    requireActivity(), "Permission denied. Cannot fetch location.", Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipe = arguments?.getSerializable("recipe") as? Recipe
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.recipeImage.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlaces()

        recipe?.let {
            if (it.imageUrl != null) {
                binding.uploadImageWrapper.visibility = View.GONE
                Picasso.get().load(it.imageUrl)
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.placeholder)
                    .resize(600,400)
                    .centerCrop()
                    .into(binding.recipeImage)
            }

            binding.title.setText(it.title)
            binding.description.setText(it.description)
            binding.ingredients.setText(it.ingredients)
            binding.authorId.setText(it.authorId)
            recipeLocation = it.location
            binding.location.setText(recipeLocation?.address)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupLocationRequest()

        checkLocationPermission()

        binding.uploadRecipeImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.recipeImage.setOnClickListener {
            if (selectedImageUri != null || (recipe != null && recipe?.imageUrl!!.isNotEmpty())) {
                pickImageLauncher.launch("image/*")
            }
        }

        binding.updateBtn.setOnClickListener {

            val title = binding.title.text.toString()
            val description = binding.description.text.toString()
            val ingredients = binding.ingredients.text.toString()
            val authorId = binding.authorId.text.toString()
            val location = binding.location.text.toString().trim()
            if (validation(title,description,ingredients,authorId, location)) {
                Constants.startLoading(requireActivity())
                if (selectedImageUri != null) {
                    recipe?.let {
                        it.title = title
                        it.description = description
                        it.ingredients = ingredients
                        it.authorId = authorId
                        it.location = recipeLocation
                        it.imageUri = selectedImageUri.toString()
                    }
                    recipeViewModel.updateRecipe(requireActivity(),recipe!!) { status, message ->
                        Constants.dismiss()
                        Constants.showAlert(requireActivity(),message!!
                        ) { p0, p1 ->  }
                    }
                } else {
                    recipe?.let {
                        it.title = title
                        it.description = description
                        it.ingredients = ingredients
                        it.authorId = authorId
                        it.location = recipeLocation
                    }
                    recipeViewModel.updateRecipe(requireActivity(),recipe!!){status,message->
                        Constants.dismiss()
                        Constants.showAlert(requireActivity(),message!!
                        ) { p0, p1 ->  }
                    }
                }
            }

        }
    }

    private fun validation(title: String,description: String, ingredients: String,authorId:String, location: String): Boolean {
        if (title.isEmpty()) {
            Constants.showAlert(requireActivity(), "Title field is required!")
            return false
        }
        else if (description.isEmpty()) {
            Constants.showAlert(requireActivity(), "Description field is required!")
            return false
        } else if (ingredients.isEmpty()) {
            Constants.showAlert(requireActivity(), "Ingredients field is required!")
            return false
        } else if (authorId.isEmpty()) {
            Constants.showAlert(requireActivity(), "Author ID field is required!")
            return false
        }
        else if (location.isEmpty()) {
            Constants.showAlert(requireActivity(), "Location field is required!")
            return false
        }

        return true
    }

    private fun initPlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().applicationContext, getString(R.string.google_map_api_key))
        }

        locationPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                handleLocationPickerResult(result.resultCode, result.data)
            }

        binding.location.setOnClickListener {
            launchLocationPicker()

        }
    }

    private fun launchLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireActivity())
        locationPickerLauncher.launch(intent)
    }

    private fun handleLocationPickerResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val locationName = place.address
                val locationLatLng = place.latLng
                recipeLocation =
                    RecipeLocation(locationLatLng!!.latitude, locationLatLng.longitude, locationName)
                binding.location.setText(locationName)
            }

            AutocompleteActivity.RESULT_ERROR -> {
                val status: Status = Autocomplete.getStatusFromIntent(data!!)

            }

            RESULT_CANCELED -> {

            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(8000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}