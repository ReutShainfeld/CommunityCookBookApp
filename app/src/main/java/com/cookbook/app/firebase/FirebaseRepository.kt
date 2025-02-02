package com.cookbook.app.firebase

import android.net.Uri
import com.cookbook.app.model.User

interface FirebaseRepository {

    fun signUp(email: String, password: String,callback: (Boolean, String?) -> Unit)
    fun signIn(email: String, password: String,callback: (Boolean, String?) -> Unit)
    fun saveUserData(user: User, imageUri: Uri)
    fun updateUserData(user: User,imageUri:Uri?,callback: (Boolean, String?) -> Unit)
    fun checkUserAuth():Boolean
    fun getLoggedUserId():String
    fun fetchUserDetails(id:String)
    fun logout()

}