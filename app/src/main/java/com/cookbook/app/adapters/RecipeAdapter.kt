package com.cookbook.app.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.cookbook.app.R
import com.cookbook.app.databinding.ItemRecipeBinding
import com.cookbook.app.model.Recipe
import com.cookbook.app.utils.Constants
import com.cookbook.app.utils.ExifTransformation
import com.cookbook.app.utils.NetworkUtils
import com.squareup.picasso.Picasso

class RecipeAdapter(private val recipes: MutableList<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, recipe: Recipe)
        fun onItemEditClick(position: Int, recipe: Recipe)
        fun onItemDeleteClick(position: Int, recipe: Recipe)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding, mListener!!)
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe)
    }

    class RecipeViewHolder(
        private val binding: ItemRecipeBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            binding.apply {
                    Picasso.get().load(recipe.user?.profileImageUrl)
                        .transform(ExifTransformation(recipe.user?.profileImageUrl!!))
                        .placeholder(R.drawable.loader)
                        .error(R.drawable.placeholder)
                        .into(userImage)

                userName.text = recipe.user.name
                recipeDate.text = Constants.getTimeAgo(recipe.timestamp)
                if (recipe.imageUrl.isNullOrEmpty()) {
                    recipeImage.visibility = View.GONE
                } else {
                    recipeImage.visibility = View.VISIBLE
                    Picasso.get().load(recipe.imageUrl).transform(ExifTransformation(recipe.imageUrl!!))
                        .placeholder(R.drawable.loader)
                        .error(R.drawable.placeholder)
                        .into(recipeImage)
                }

                binding.root.setOnClickListener {
                    mListener.onItemClick(layoutPosition, recipe)
                }


                binding.recipeMoreOption.setOnClickListener { view ->
                    showPopupMenu(view, recipe)
                }

                recipeTitle.text = recipe.title
                recipeIngredients.text = recipe.ingredients
                recipeDescription.text = recipe.description
            }
        }

        private fun showPopupMenu(view: View, item: Recipe) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.recipe_pop_up_menu, popupMenu.menu)

            if (item.user?.userId != Constants.loggedUserId) {
                popupMenu.menu.findItem(R.id.menu_edit).isVisible = false
                popupMenu.menu.findItem(R.id.menu_delete).isVisible = false
            } else {
                popupMenu.menu.findItem(R.id.menu_edit).isVisible = true
                popupMenu.menu.findItem(R.id.menu_delete).isVisible = true
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_view -> {
                        mListener.onItemClick(layoutPosition, item)
                    }

                    R.id.menu_edit -> {
                        mListener.onItemEditClick(layoutPosition, item)
                    }

                    R.id.menu_delete -> {
                        mListener.onItemDeleteClick(layoutPosition, item)
                    }

                }
                true
            }

            popupMenu.show()
        }
    }


}
