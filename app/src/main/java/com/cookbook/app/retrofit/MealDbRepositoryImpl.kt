package com.cookbook.app.retrofit

import com.cookbook.app.model.MealRecipe
import com.cookbook.app.room.AppDao
import javax.inject.Inject

class MealDbRepositoryImpl @Inject constructor(
    private val apiService: MealDbService,
    private val appDao: AppDao
) : MealDbRepository {

    override suspend fun fetchRecipes(query: String, callback: (List<MealRecipe>) -> Unit) {
        // Make the network call
        val response = apiService.getRecipesByCategory(query)

        // Safely convert or fall back to emptyList() if meals is null
        val recipes = response.meals?.map {
            MealRecipe(
                id = it.idMeal,
                title = it.strMeal,
                description = it.strInstructions,
                image = it.strMealThumb,
                authorId = it.idMeal
            )
        } ?: emptyList()

        // Now invoke the callback with either the new recipes or empty list
        if (recipes.isNotEmpty()) {
            // Optionally insert them into DB here, or let the caller handle DB
            callback(recipes)
        } else {
            callback(emptyList())
        }
    }

    override suspend fun getCachedRecipes(callback: (List<MealRecipe>) -> Unit) {
        val cacheRecipes = appDao.getAllMealRecipes()
        if (cacheRecipes.isNotEmpty()) {
            callback(cacheRecipes)
        } else {
            callback(emptyList())
        }
    }
}
