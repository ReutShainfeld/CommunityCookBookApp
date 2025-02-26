package com.cookbook.app.model

import java.io.Serializable

    data class MealDbEntity(
        val idMeal: String,
        val strMeal: String,
        val strInstructions: String,
        val strMealThumb: String,
        val strCategory: String
    ):Serializable{
        constructor():this("","","","","")
    }
