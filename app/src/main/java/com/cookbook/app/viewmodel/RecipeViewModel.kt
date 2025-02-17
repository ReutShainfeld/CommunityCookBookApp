package com.cookbook.app.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookbook.app.model.Recipe
import com.cookbook.app.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val databaseRepository:DatabaseRepository
) : ViewModel(){

    private val _allRecipes = MutableLiveData<List<Recipe>>() // Backing field
    val allRecipes: LiveData<List<Recipe>> get() = _allRecipes // Expose LiveData


    fun fetchAllRecipes() {
        viewModelScope.launch {
            databaseRepository.getAllRecipes(){recipes->
                _allRecipes.postValue(recipes) // Update LiveData
            }

        }
    }

    fun getRecipeId(): String {
        return databaseRepository.getRecipeId()
    }

    fun addRecipe(context: Context,recipe: Recipe,callback: (Boolean, String?) -> Unit) {
        databaseRepository.addRecipe(context,recipe) { success, message ->
            CoroutineScope(Dispatchers.Main).launch {
                callback(success,message)
            }
        }
    }

    fun updateRecipe(context: Context,recipe: Recipe,callback: (Boolean, String?) -> Unit) {
        databaseRepository.updateRecipe(context,recipe) { success, message ->
            CoroutineScope(Dispatchers.Main).launch {
                callback(success,message)
            }
        }
    }

    fun deleteRecipe(context: Context,recipeId: String,callback: (Boolean, String?) -> Unit) {
        databaseRepository.deleteRecipe(context,recipeId) { success, message ->
            CoroutineScope(Dispatchers.Main).launch {
                callback(success,message)
            }
        }
    }

    fun syncOfflineRecipes(context: Context) {
        databaseRepository.syncOfflineRecipes(context)
    }

}