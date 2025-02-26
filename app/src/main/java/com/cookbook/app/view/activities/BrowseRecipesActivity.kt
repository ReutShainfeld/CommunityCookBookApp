package com.cookbook.app.view.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookbook.app.adapters.MealRecipeAdapter
import com.cookbook.app.databinding.ActivityRecipesBrowseBinding
import com.cookbook.app.model.MealRecipe
import com.cookbook.app.utils.Constants
import com.cookbook.app.viewmodel.RecipeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseRecipesActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRecipesBrowseBinding
    private lateinit var adapter: MealRecipeAdapter
    private val allRecipes = mutableListOf<MealRecipe>()
    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipesBrowseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MealRecipeAdapter(allRecipes) { recipe ->
            Toast.makeText(this, "Clicked on: ${recipe.title}", Toast.LENGTH_SHORT).show()
        }

        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recipesRecyclerView.adapter = adapter

        recipeViewModel.mealRecipes.observe(this) { recipes ->
            Constants.dismiss()
            if (recipes.isNotEmpty()) {
                allRecipes.clear()
                allRecipes.addAll(recipes)
            }
            adapter.notifyItemChanged(0,allRecipes.size) // Update RecyclerView
        }
        recipeViewModel.fetchMealRecipes("")
        binding.searchButton.setOnClickListener {
            Constants.startLoading(this)
            val query = binding.searchEditText.text.toString().trim()
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        recipeViewModel.fetchMealRecipes(query)
    }
}