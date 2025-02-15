package com.cookbook.app.viewmodel

import androidx.lifecycle.ViewModel
import com.cookbook.app.firebase.FirebaseRepository
import com.cookbook.app.repository.DatabaseRepository
import javax.inject.Inject

class RecipeViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val dataRepository:DatabaseRepository
) : ViewModel(){




}