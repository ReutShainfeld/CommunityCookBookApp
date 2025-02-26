package com.cookbook.app.retrofit

import com.cookbook.app.model.MealRecipe

interface MealDbRepository {
    suspend fun fetchRecipes(query: String,callback:(List<MealRecipe>) ->Unit)
    suspend fun getCachedRecipes(callback: (List<MealRecipe>) -> Unit)
}