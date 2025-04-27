package com.cookbook.app.repository

import android.content.Context
import com.cookbook.app.firebase.FirebaseRepository
import com.cookbook.app.model.MealRecipe
import com.cookbook.app.model.Recipe
import com.cookbook.app.retrofit.MealDbRepository
import com.cookbook.app.room.AppDao
import com.cookbook.app.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseRepository: FirebaseRepository,
    private val appDao: AppDao,
    private val mealDbRepository: MealDbRepository
) {

    /* ──────────────────────────────────────────────────────────── */
    /*  MEAL-DB HELPERS (unchanged)                                 */
    /* ──────────────────────────────────────────────────────────── */

    suspend fun getMealRecipes(query: String, callback: (List<MealRecipe>) -> Unit) {
        val cache = mutableListOf<MealRecipe>().apply { addAll(appDao.getAllMealRecipes()) }

        if (query.isEmpty()) callback(cache)      // return cached immediately

        CoroutineScope(Dispatchers.IO).launch {
            try {
                mealDbRepository.fetchRecipes(query) { fromApi ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (fromApi.isNotEmpty()) {
                            appDao.deleteAllMealRecipes()
                            appDao.insertMealRecipes(fromApi)
                            cache.apply { clear(); addAll(appDao.getAllMealRecipes()) }
                        }
                        callback(if (cache.isNotEmpty()) cache else emptyList())
                    }
                }
            } catch (_: Exception) {
                callback(if (cache.isNotEmpty()) cache else emptyList())
            }
        }
    }

    /* ──────────────────────────────────────────────────────────── */
    /*  LOCAL READ                                                  */
    /* ──────────────────────────────────────────────────────────── */

    fun getAllRecipes(callback: (List<Recipe>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            callback(appDao.getAllRecipes())      // returns *everything* in Room
        }
    }

    fun getRecipeId(): String = firebaseRepository.getRecipeId()

    /* ──────────────────────────────────────────────────────────── */
    /*  CRUD                                                        */
    /* ──────────────────────────────────────────────────────────── */

    fun addRecipe(
        context: Context,
        recipe: Recipe,
        callback: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)
                appDao.insertRecipe(recipe.copy(isSynced = false))

                if (NetworkUtils.isOnline(context)) {
                    firebaseRepository.addRecipeToFireStore(context, recipe) { ok, msg, updated ->
                        if (ok) {
                            CoroutineScope(Dispatchers.IO).launch {
                                updated?.let {
                                    appDao.updateRecipeSyncStatus(it.recipeId)
                                    delay(1000)
                                    it.imageUrl?.let { url ->
                                        appDao.updateRecipeImage(it.recipeId, url)
                                    }
                                }
                                callback(true, msg)
                            }
                        } else callback(false, msg)
                    }
                } else {
                    callback(true, "Saved locally, will sync when online")
                }
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    fun updateRecipe(
        context: Context,
        recipe: Recipe,
        callback: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)
                appDao.updateRecipe(recipe)

                if (NetworkUtils.isOnline(context)) {
                    firebaseRepository.updateRecipeToFireStore(context, recipe) { ok, msg, updated ->
                        if (ok) {
                            CoroutineScope(Dispatchers.IO).launch {
                                updated?.let {
                                    if (!it.isSynced) appDao.updateRecipeSyncStatus(it.recipeId)
                                    delay(1000)
                                    it.imageUrl?.let { url ->
                                        appDao.updateRecipeImage(it.recipeId, url)
                                    }
                                }
                                callback(true, msg)
                            }
                        } else callback(false, msg)
                    }
                } else {
                    callback(true, "Updated locally, will sync when online")
                }
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    fun deleteRecipe(
        context: Context,
        recipeId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)
                if (NetworkUtils.isOnline(context)) {
                    firebaseRepository.deleteRecipeFromFireStore(recipeId) { ok, msg ->
                        if (ok) {
                            CoroutineScope(Dispatchers.IO).launch {
                                appDao.deleteRecipe(recipeId)
                                callback(true, msg)
                            }
                        } else callback(false, msg)
                    }
                } else {
                    callback(true, "Deleted locally, will sync when online")
                }
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    /* ──────────────────────────────────────────────────────────── */
    /*  SYNC HELPERS                                                */
    /* ──────────────────────────────────────────────────────────── */

    /** Push locally-created recipes that have not yet hit Firestore */
    fun syncOfflineRecipes(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!NetworkUtils.isOnline(context)) return@launch
            appDao.getUnsyncedRecipes().forEach { recipe ->
                firebaseRepository.addRecipeToFireStore(context, recipe) { ok, _, _ ->
                    if (ok) CoroutineScope(Dispatchers.IO).launch {
                        appDao.updateRecipeSyncStatus(recipe.recipeId)
                    }
                }
            }
        }
    }

    /**
     * Pull **every** document in the RECIPES collection (no user filter),
     * insert them into Room (duplicates replaced), then invoke [callback].
     */
    fun syncFireStoreRecipesWithRoom(context: Context, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!NetworkUtils.isOnline(context)) {
                callback(true); return@launch
            }

            firebaseRepository.getAllRecipeFromFireStore { ok, list ->
                if (!ok || list == null) {
                    callback(false); return@getAllRecipeFromFireStore
                }

                //  ★ move insert to IO so Room is never touched on the UI thread
                CoroutineScope(Dispatchers.IO).launch {
                    appDao.insertRecipes(list)            // OnConflictStrategy.REPLACE
                    callback(true)
                }
            }
        }
    }
}
