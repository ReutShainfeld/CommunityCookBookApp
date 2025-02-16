package com.cookbook.app.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.cookbook.app.utils.Constants
import java.io.Serializable

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "ingredients") var ingredients: String,
    @ColumnInfo(name = "author_id") var authorId: String,
    @ColumnInfo(name = "image_url") var imageUrl: String?,
    @Embedded var location: RecipeLocation?,
    val timestamp: Long = System.currentTimeMillis(),
    @Embedded val user: User? = Constants.loggedUser,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false
): Serializable{
    constructor():this("","","","","","","",RecipeLocation(0.0,0.0,""))
    @Ignore var imageUri:Uri?=null

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "recipe_id" to recipeId,
            "user_id" to userId,
            "title" to title,
            "description" to description,
            "ingredients" to ingredients,
            "author_id" to authorId,
            "image_url" to imageUrl,
            "location" to location?.toMap(), // Convert embedded object to Map
            "timestamp" to timestamp,
            "is_synced" to isSynced,
            "user" to user?.toMap() // Convert User object to Map
        )
    }
}
