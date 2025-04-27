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
import com.cookbook.app.utils.Constants
import com.cookbook.app.viewmodel.RecipeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {

    // ----------------------------------------------------
    // view binding
    // ----------------------------------------------------
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    // ----------------------------------------------------
    // VM & helpers
    // ----------------------------------------------------
    private val recipeViewModel: RecipeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private val recipesList = mutableListOf<Recipe>()

    private var listener: OnFragmentChangeListener? = null

    // ----------------------------------------------------
    // lifecycle
    // ----------------------------------------------------
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentChangeListener) listener = context
        else throw RuntimeException("$context must implement OnFragmentChangeListener")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        binding.loadingSpinner.visibility = View.VISIBLE

        // 1️⃣  Observe FIRST – we are on the main thread here.
        recipeViewModel.allRecipes.observe(viewLifecycleOwner) { updateUi(it) }

        // 2️⃣  Kick off Cloud ➜ Room sync, then fetch the list.
        recipeViewModel.syncFireStoreRecipesWithRoom(requireContext()) { _ ->
            recipeViewModel.fetchAllRecipes()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ----------------------------------------------------
    // UI helpers
    // ----------------------------------------------------
    private fun updateUi(recipes: List<Recipe>) {
        if (recipes.isNotEmpty()) {
            binding.loadingSpinner.visibility      = View.GONE
            binding.emptyRecipeView.visibility     = View.GONE
            binding.recipesRecyclerview.visibility = View.VISIBLE

            recipesList.clear()
            recipesList.addAll(recipes)
            recipeAdapter.updateRecipes(recipes)
        } else {
            binding.loadingSpinner.visibility      = View.GONE
            binding.recipesRecyclerview.visibility = View.GONE
            binding.emptyRecipeView.visibility     = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(mutableListOf())
        binding.recipesRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = recipeAdapter
        }

        recipeAdapter.setOnItemClickListener(object : RecipeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, recipe: Recipe) {
                findNavController().navigate(
                    R.id.action_home_to_recipe_detail,
                    Bundle().apply { putSerializable("recipe", recipe) })
            }

            override fun onItemEditClick(position: Int, recipe: Recipe) {
                findNavController().navigate(
                    R.id.action_home_to_edit_recipe,
                    Bundle().apply { putSerializable("recipe", recipe) })
            }

            override fun onItemDeleteClick(position: Int, recipe: Recipe) {
                confirmDelete(position, recipe)
            }
        })
    }

    // ----------------------------------------------------
    // delete flow
    // ----------------------------------------------------
    private fun confirmDelete(position: Int, recipe: Recipe) {
        AlertDialog.Builder(requireActivity())
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Delete") { d, _ ->
                d.dismiss()
                Constants.startLoading(requireActivity())

                recipeViewModel.deleteRecipe(requireActivity(), recipe.recipeId) { ok, msg ->
                    Constants.dismiss()
                    if (ok) {
                        recipesList.removeAt(position)
                        recipeAdapter.updateRecipes(recipesList)
                        if (recipesList.isEmpty()) {
                            binding.recipesRecyclerview.visibility = View.GONE
                            binding.emptyRecipeView.visibility      = View.VISIBLE
                        }
                    }
                    Constants.showAlert(requireActivity(), msg ?: "Unknown error")
                }
            }
            .setNeutralButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }
}
