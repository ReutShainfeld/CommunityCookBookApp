package com.cookbook.app.repository


import com.cookbook.app.firebase.FirebaseRepository
import com.cookbook.app.room.AppDao
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val auth: FirebaseAuth,
                                             private val firebaseRepository: FirebaseRepository, private val appDao: AppDao) {


}