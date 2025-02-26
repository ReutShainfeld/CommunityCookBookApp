package com.cookbook.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cookbook.app.R
import com.cookbook.app.databinding.ItemMealRecipeBinding
import com.cookbook.app.model.MealRecipe
import com.squareup.picasso.Picasso

class MealRecipeAdapter(
    private val recipes: List<MealRecipe>,
    private val onClick: (MealRecipe) -> Unit
) : RecyclerView.Adapter<MealRecipeAdapter.MealRecipeViewHolder>() {

    inner class MealRecipeViewHolder(private val binding: ItemMealRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: MealRecipe) {
            binding.titleTextView.text = recipe.title
            binding.descriptionTextView.text = recipe.description
            Picasso.get().load(recipe.image)
                .placeholder(R.drawable.loader)
                .error(R.drawable.placeholder)
                .into(binding.imageView)
            binding.root.setOnClickListener { onClick(recipe) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealRecipeViewHolder {
        val binding = ItemMealRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealRecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size
}
