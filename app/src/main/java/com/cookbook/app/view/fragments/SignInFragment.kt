package com.cookbook.app.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.cookbook.app.R
import com.cookbook.app.databinding.FragmentSignInBinding
import com.cookbook.app.interaces.OnFragmentChangeListener
import com.cookbook.app.utils.Constants
import com.cookbook.app.view.activities.MainActivity
import com.cookbook.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var listener: OnFragmentChangeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentChangeListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentChangeListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signinSignupText.setOnClickListener {
            listener?.navigateToFragment(R.id.action_signin_to_signup,R.id.signInFragment)
        }

        binding.loginBtn.setOnClickListener {

            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            Constants.startLoading(requireActivity())
            authViewModel.validateAndLogin(email,password){ status,message->
                Constants.dismiss()
                if (status){
                    startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }).apply {
                        requireActivity().finish()
                    }
                }
                else{
                    Constants.showAlert(requireActivity(),message!!)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}