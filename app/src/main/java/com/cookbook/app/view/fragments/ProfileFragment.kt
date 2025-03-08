package com.cookbook.app.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.cookbook.app.R
import com.cookbook.app.databinding.FragmentProfileBinding
import com.cookbook.app.interaces.OnFragmentChangeListener
import com.cookbook.app.model.User
import com.cookbook.app.utils.Constants
import com.cookbook.app.utils.ExifTransformation
import com.cookbook.app.view.activities.AuthActivity
import com.cookbook.app.viewmodel.AuthViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var listener: OnFragmentChangeListener? = null
    private var user:User? = null

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
        binding =  FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileDetailWrapperLayout.visibility = View.GONE
        binding.loadingSpinner.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.loadingSpinner.visibility = View.GONE
            binding.profileDetailWrapperLayout.visibility = View.VISIBLE
            user = Constants.loggedUser
            if (user != null){
                Picasso.get().load(user!!.profileImageUrl)
//                    .transform(ExifTransformation(user!!.profileImageUrl!!))
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.placeholder)
                    .resize(200,200)
                    .centerCrop()
                    .into(binding.profileImage)
                binding.name.setText(user!!.name)
                binding.email.setText(user!!.email)
                Constants.dismiss()
            }
        },3000)

        binding.editBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("user", user) // Store User object in Bundle
            }

            listener?.navigateToFragment(R.id.action_profile_to_editProfile, args = bundle)
        }

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(context, AuthActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }
    }


}