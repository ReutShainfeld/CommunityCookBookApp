package com.cookbook.app.repository


import android.content.Context
import androidx.lifecycle.LiveData
import com.cookbook.app.firebase.FirebaseRepository
import com.cookbook.app.model.Recipe
import com.cookbook.app.room.AppDao
import com.cookbook.app.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val auth: FirebaseAuth,
                                             private val firebaseRepository: FirebaseRepository, private val appDao: AppDao) {


    fun getAllRecipes(callback: (List<Recipe>) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            callback(appDao.getAllRecipes())
        }
    }

    fun getRecipeId():String{
        return firebaseRepository.getRecipeId()
    }

    fun addRecipe(context: Context,recipe: Recipe, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)
                appDao.insertRecipe(recipe.copy(isSynced = false))

                if (NetworkUtils.isOnline(context)) {
                    firebaseRepository.addRecipeToFireStore(context,recipe) { success, message,recipe ->
                        if (success) {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (recipe != null){
                                   appDao.updateRecipeSyncStatus(
                                        recipe.recipeId
                                    )
                                    delay(1000)
                                    if (recipe.imageUrl != null){
                                       appDao.updateRecipeImage(recipe.recipeId,recipe.imageUrl!!)
                                    }
                                }
//                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(true, message)
//                                }
                            }
                        }
                        else{
                            callback(false, message)
                        }
                    }
                } else {
                    callback(true, "Saved locally, will sync when online")
                }
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    fun updateRecipe(context: Context,recipe: Recipe, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)
                appDao.updateRecipe(recipe)

                if (NetworkUtils.isOnline(context)) {
                    firebaseRepository.updateRecipeToFireStore(context,recipe) { success, message,recipe ->
                        if (success) {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (recipe != null){
                                    if (!recipe.isSynced){
                                        appDao.updateRecipeSyncStatus(
                                            recipe.recipeId
                                        )
                                    }
                                    delay(1000)
                                    if (recipe.imageUrl != null){
                                        appDao.updateRecipeImage(recipe.recipeId,recipe.imageUrl!!)
                                    }
                                }
//                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(true, message)
//                                }
                            }
                        }
                        else{
                            callback(false, message)
                        }
                    }
                } else {
                    callback(true, "updated locally, will sync when online")
                }
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    fun deleteRecipe(context: Context,recipeId: String, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)
                if (NetworkUtils.isOnline(context)) {
                    firebaseRepository.deleteRecipeFromFireStore(recipeId) { success, message ->
                        if (success) {
                            CoroutineScope(Dispatchers.IO).launch {
                                appDao.deleteRecipe(recipeId)
                                callback(success,message)
                            }
                        }
                        else{
                            callback(false, message)
                        }
                    }
                } else {
                    callback(true, "delete locally, will sync when online")
                }
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    fun syncOfflineRecipes(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            if (NetworkUtils.isOnline(context)) {
                val unsyncedRecipes = appDao.getUnsyncedRecipes()
                for (recipe in unsyncedRecipes) {
                    firebaseRepository.addRecipeToFireStore(context,recipe) { success, _,_ ->
                        if (success) {
                            CoroutineScope(Dispatchers.IO).launch {
                                appDao.updateRecipeSyncStatus(recipe.recipeId)
                            }
                        }
                    }
                }
            }
        }
    }
}