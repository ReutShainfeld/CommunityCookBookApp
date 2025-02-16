package com.cookbook.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.cookbook.app.model.Recipe
import com.cookbook.app.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val databaseRepository:DatabaseRepository
) : ViewModel(){

    fun getRecipeId(): String {
        return databaseRepository.getRecipeId()
    }

    fun addRecipe(context: Context,recipe: Recipe,callback: (Boolean, String?) -> Unit) {
        databaseRepository.addRecipe(context,recipe) { success, message ->
           callback(success,message)
        }
    }

    fun syncOfflineRecipes(context: Context) {
        databaseRepository.syncOfflineRecipes(context)
    }

}