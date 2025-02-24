package com.cookbook.app.retrofit

import com.cookbook.app.model.MealRecipe
import com.cookbook.app.room.AppDao
import javax.inject.Inject

class MealDbImpl @Inject constructor(private val apiService: MealDbService,private val appDao: AppDao):MealDbRepository{
    override suspend fun fetchRecipes(category: String): List<MealRecipe> {
        val recipes = apiService.getRecipesByCategory(category).map {
            MealRecipe(
                id = it.id,
                title = it.title,
                description = it.description,
                image = it.image,
                authorId = it.authorId
            )
        }
        appDao.insertMealRecipes(recipes)
        return recipes
    }

    override suspend fun getCachedRecipes(): List<MealRecipe> {
        return appDao.getAllMealRecipes()
    }

}