package com.cookbook.app.view.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.cookbook.app.R
import com.cookbook.app.databinding.FragmentSignUpBinding
import com.cookbook.app.interaces.OnFragmentChangeListener
import com.cookbook.app.model.User
import com.cookbook.app.utils.Constants
import com.cookbook.app.view.activities.MainActivity
import com.cookbook.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var listener: OnFragmentChangeListener? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

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

        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupSigninText.setOnClickListener {
            listener?.navigateToFragment(R.id.action_signup_to_signin, R.id.signupFragment)
        }

        binding.signupBtn.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            Constants.startLoading(requireActivity())
            authViewModel.validateAndSignUp(
                selectedImageUri,
                name,
                email,
                password, confirmPassword
            ) { status, message ->
                Constants.dismiss()
                if (status) {
                    val user = User(name = name,email=email)
                    authViewModel.saveUserData(user,selectedImageUri)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Constants.showAlert(requireActivity(), message!!)
                }
            }
        }

        binding.uploadImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}