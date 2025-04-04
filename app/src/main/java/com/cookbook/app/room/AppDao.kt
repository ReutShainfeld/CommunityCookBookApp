package com.cookbook.app.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cookbook.app.model.MealRecipe
import com.cookbook.app.model.Recipe

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes WHERE is_synced = 0")
    suspend fun getUnsyncedRecipes(): List<Recipe>

    @Query("UPDATE recipes SET is_synced = 1 WHERE recipe_id = :recipeId")
    suspend fun updateRecipeSyncStatus(recipeId: String)

    @Query("UPDATE recipes SET image_url = :url WHERE recipe_id = :recipeId")
    suspend fun updateRecipeImage(recipeId: String,url:String)

    @Query("SELECT * FROM recipes ORDER BY timestamp desc")
    fun getAllRecipes(): List<Recipe>

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE recipe_id = :recipeId")
    suspend fun deleteRecipe(recipeId: String)

    @Query("SELECT * FROM meal_recipes")
    suspend fun getAllMealRecipes(): List<MealRecipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealRecipes(recipes: List<MealRecipe>)

    @Query("DELETE FROM meal_recipes")
    suspend fun deleteAllMealRecipes()

}