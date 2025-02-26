package com.cookbook.app.retrofit

import com.cookbook.app.model.MealDbResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbService {
    @GET("search.php")
    suspend fun getRecipesByCategory(@Query("s") query: String): MealDbResponse
}