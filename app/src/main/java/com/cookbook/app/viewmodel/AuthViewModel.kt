package com.cookbook.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cookbook.app.firebase.FirebaseRepository
import com.cookbook.app.model.User
import com.cookbook.app.utils.ValidationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel(){

    fun validateAndLogin(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val isEmailValid = ValidationUtil.isEmailValid(email)
        val isPasswordValid = ValidationUtil.isPasswordValid(password)

        if (isEmailValid && isPasswordValid) {
            signIn(email, password, callback)
        } else {
            when {
                email.isBlank() -> callback(false,"Email field cannot be empty")
                password.isBlank() -> callback(false,"Password field cannot be empty")
                !isEmailValid -> callback(false,"Invalid email address")
                !isPasswordValid -> callback(false,"Password must be at least 6 characters")
                else -> callback(false,"")
            }
        }
    }

    fun validateAndSignUp(
        imageUri:Uri?,
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val isImageUri = imageUri == null
        val isNameEmpty = name.isBlank()
        val isEmailEmpty = email.isBlank()
        val isEmailValid = ValidationUtil.isEmailValid(email)
        val isPasswordEmpty = password.isBlank()
        val isPasswordValid = ValidationUtil.isPasswordValid(password)
        val isConfirmPasswordEmpty = confirmPassword.isBlank()
        val isBothPasswordsSame = password.lowercase() == confirmPassword.lowercase()

        if (
            !isImageUri &&
            !isNameEmpty &&
            !isEmailEmpty &&
            isEmailValid &&
            !isPasswordEmpty &&
            isPasswordValid &&
            !isConfirmPasswordEmpty &&
            isBothPasswordsSame
        ) {
            signUp(email, password, callback)
        } else {
            when {
                isImageUri -> callback(false,"Profile Image is required")
                isNameEmpty -> callback(false,"Name Field cannot be empty")
                isEmailEmpty -> callback(false,"Email field cannot be empty")
                !isEmailValid -> callback(false,"Invalid email address")
                isPasswordEmpty -> callback(false,"Password field cannot be empty")
                isConfirmPasswordEmpty -> callback(false,"Confirm Password field cannot be empty")
                !isPasswordValid -> callback(false,"Password must be at least 6 characters")
                !isBothPasswordsSame -> callback(false,"Password and confirm password are not same")
                else -> callback(false,"")
            }
        }
    }

    private fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseRepository.signUp(email, password, callback)
    }

    private fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseRepository.signIn(email, password, callback)
    }

    fun fetUserDetails(){
        firebaseRepository.fetchUserDetails(getLoggedUserId())
    }

    fun saveUserData(user: User, imageUri: Uri?) {
        firebaseRepository.saveUserData(user,imageUri!!)
    }

    fun updateUserData(user: User,imageUri: Uri?,callback: (Boolean, String?) -> Unit) {
        firebaseRepository.updateUserData(user,imageUri,callback)
    }

    fun checkUserAuth(): Boolean {
        return firebaseRepository.checkUserAuth()
    }

    private fun getLoggedUserId(): String {
        return firebaseRepository.getLoggedUserId()
    }

    fun logout(){
        firebaseRepository.logout()
    }


}