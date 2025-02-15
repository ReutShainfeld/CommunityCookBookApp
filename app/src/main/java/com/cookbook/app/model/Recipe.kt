package com.cookbook.app.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cookbook.app.utils.Constants
import java.io.Serializable

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey @ColumnInfo(name = "post_id") val postId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "ingredients") var ingredients: String,
    @ColumnInfo(name = "author_id") var authorId: String,
    @ColumnInfo(name = "image_url") var imageUrl: String?,
    @Embedded var location: RecipeLocation?,
    val timestamp: Long = System.currentTimeMillis(),
    @Embedded val user: User? = Constants.loggedUser,
): Serializable{
    constructor():this("","","","","","","",RecipeLocation(0.0,0.0,""))
}
