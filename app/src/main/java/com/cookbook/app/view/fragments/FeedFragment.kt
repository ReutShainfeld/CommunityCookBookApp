package com.cookbook.app.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookbook.app.R
import com.cookbook.app.adapters.RecipeAdapter
import com.cookbook.app.databinding.FragmentFeedBinding
import com.cookbook.app.interaces.OnFragmentChangeListener
import com.cookbook.app.model.Recipe
import com.cookbook.app.viewmodel.RecipeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private var listener: OnFragmentChangeListener? = null

    private val recipeViewModel: RecipeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipesList = mutableListOf<Recipe>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentChangeListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentChangeListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.loadingSpinner.visibility = View.VISIBLE
        recipeViewModel.allRecipes.observe(requireActivity()) { recipes ->
            if (recipes.isNotEmpty()) {
                binding.loadingSpinner.visibility = View.GONE
                recipesList.clear()
                recipesList.addAll(recipes)
                binding.recipesRecyclerview.apply {
                    visibility = View.VISIBLE
                }
                binding.emptyRecipeView.apply {
                    visibility = View.GONE
                }
            } else {
                binding.loadingSpinner.visibility = View.GONE
                binding.recipesRecyclerview.apply {
                    visibility = View.GONE
                }
                binding.emptyRecipeView.apply {
                    visibility = View.VISIBLE
                }
            }
            recipeAdapter.updateRecipes(recipes) // Update RecyclerView
        }
        recipeViewModel.fetchAllRecipes()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(mutableListOf())
        binding.recipesRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }

        recipeAdapter.setOnItemClickListener(object : RecipeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, recipe: Recipe) {

            }

            override fun onItemEditClick(position: Int, recipe: Recipe) {
                val bundle = Bundle().apply {
                    putSerializable("recipe", recipe)
                }
                findNavController().navigate(R.id.action_home_to_edit_recipe, bundle)
            }

            override fun onItemDeleteClick(position: Int, recipe: Recipe) {
                val builder = AlertDialog.Builder(requireActivity())
                builder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton("Delete") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setNeutralButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }


        })
    }

}