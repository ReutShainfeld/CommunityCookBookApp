package com.cookbook.app.retrofit

import com.cookbook.app.model.MealRecipe
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbService {
    @GET("filter.php")
    suspend fun getRecipesByCategory(@Query("c") category: String): List<MealRecipe>
}