package com.cookbook.app.firebase

import com.cookbook.app.firebase.FirebaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(private val firebaseFireStore: FirebaseFirestore) :
    FirebaseRepository {
}