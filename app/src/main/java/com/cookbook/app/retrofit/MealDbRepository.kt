package com.cookbook.app.retrofit

import com.cookbook.app.model.MealRecipe

interface MealDbRepository {
    suspend fun fetchRecipes(category: String): List<MealRecipe>
    suspend fun getCachedRecipes(): List<MealRecipe>
}