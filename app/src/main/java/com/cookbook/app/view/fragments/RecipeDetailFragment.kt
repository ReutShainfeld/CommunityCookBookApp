package com.cookbook.app.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cookbook.app.R
import com.cookbook.app.databinding.FragmentRecipeDetailBinding
import com.cookbook.app.model.Recipe
import com.cookbook.app.utils.Constants
import com.cookbook.app.utils.ExifTransformation
import com.cookbook.app.viewmodel.RecipeViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeDetailFragment : Fragment() {

    private lateinit var binding:FragmentRecipeDetailBinding
    private var recipe:Recipe?=null
    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipe = arguments?.getSerializable("recipe") as? Recipe

        recipe?.let {
            Picasso.get().load(it.user?.profileImageUrl).transform(ExifTransformation(it.user?.profileImageUrl!!)).placeholder(R.drawable.placeholder).into(binding.userImage)
            binding.userName.text = it.user.name
            binding.recipeDate.text = Constants.getTimeAgo(it.timestamp)
            if (it.imageUrl.isNullOrEmpty()){
                binding.recipeImage.visibility = View.GONE
            }
            else{
                binding.recipeImage.visibility = View.VISIBLE
                Picasso.get().load(it.imageUrl).transform(ExifTransformation(it.imageUrl as String)).placeholder(R.drawable.placeholder).into(binding.recipeImage)
            }

            binding.recipeTitle.text = it.title
            binding.recipeDescription.text = it.description
            binding.recipeIngredients.text = it.ingredients

            binding.recipeMoreOption.setOnClickListener {v ->
                showPopupMenu(v, it)
            }
        }
    }

    private fun showPopupMenu(view: View, item: Recipe) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.recipe_pop_up_menu, popupMenu.menu)

        popupMenu.menu.findItem(R.id.menu_view).isVisible = false
        if (item.user?.userId != Constants.loggedUserId) {
            popupMenu.menu.findItem(R.id.menu_edit).isVisible = false
            popupMenu.menu.findItem(R.id.menu_delete).isVisible = false
        }
        else{
            popupMenu.menu.findItem(R.id.menu_edit).isVisible = true
            popupMenu.menu.findItem(R.id.menu_delete).isVisible = true
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    val bundle = Bundle().apply {
                        putSerializable("recipe", item)
                    }
                    findNavController().navigate(R.id.action_detail_to_edit_recipe, bundle)
                }
                R.id.menu_delete -> {
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete?")
                        .setPositiveButton("Delete"){dialog,which->
                            dialog.dismiss()
                        }
                        .setNeutralButton("Cancel"){dialog,which->
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                }

            }
            true
        }

        popupMenu.show()
    }
}