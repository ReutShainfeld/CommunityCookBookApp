package com.cookbook.app.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookbook.app.model.MealRecipe
import com.cookbook.app.model.Recipe
import com.cookbook.app.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val databaseRepository:DatabaseRepository
) : ViewModel(){

    private val _allRecipes = MutableLiveData<List<Recipe>>() // Backing field
    val allRecipes: LiveData<List<Recipe>> get() = _allRecipes // Expose LiveData

    private val _mealRecipes = MutableLiveData<List<MealRecipe>>()
    val mealRecipes: LiveData<List<MealRecipe>> = _mealRecipes

    fun fetchMealRecipes(query: String) {
        viewModelScope.launch {
            delay(2000)
            databaseRepository.getMealRecipes(query){ recipes ->
                CoroutineScope(Dispatchers.Main).launch {
                    _mealRecipes.postValue(recipes)
                }
            }
        }
    }


    fun fetchAllRecipes() {
        viewModelScope.launch {
            delay(2000)
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

    fun syncFireStoreRecipesWithRoom(context: Context,callback: (Boolean) -> Unit) {
        databaseRepository.syncFireStoreRecipesWithRoom(context){ success->
            callback(success)
        }
    }

}