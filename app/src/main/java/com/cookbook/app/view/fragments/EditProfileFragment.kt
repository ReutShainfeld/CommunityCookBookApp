package com.cookbook.app.view.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cookbook.app.R
import com.cookbook.app.databinding.FragmentEditProfileBinding
import com.cookbook.app.interaces.OnFragmentChangeListener
import com.cookbook.app.model.User
import com.cookbook.app.utils.Constants
import com.cookbook.app.utils.ExifTransformation
import com.cookbook.app.viewmodel.AuthViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var listener: OnFragmentChangeListener? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null
    private var user: User? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentChangeListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentChangeListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getSerializable("user") as? User
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.profileImage.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uploadImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        if (user != null) {
            Picasso.get().load(user?.profileImageUrl)
                .transform(ExifTransformation(user?.profileImageUrl!!))
                .placeholder(R.drawable.loader)
                .error(R.drawable.placeholder)
                .into(binding.profileImage)
            binding.name.setText(user?.name)
        }

        binding.updateBtn.setOnClickListener {
            val name = binding.name.text.toString().trim()
            if (name.isNotEmpty()) {
                Constants.startLoading(requireActivity())
                user?.name = name
                authViewModel.updateUserData(
                    user!!, selectedImageUri
                ) { status, message ->
                    Constants.dismiss()
                    if (status) {
                        Constants.loggedUser = user
                        Constants.showAlert(requireActivity(), message!!
                        ) { p0, p1 -> listener?.navigateToFragment(R.id.action_editProfile_to_profile) }
                    } else {
                        Constants.showAlert(requireActivity(), message!!)
                    }
                }
            } else {
                Constants.showAlert(requireActivity(), "Name input required!")
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}