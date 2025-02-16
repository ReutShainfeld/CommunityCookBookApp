package com.cookbook.app.view.activities

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavArgs
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cookbook.app.R
import com.cookbook.app.databinding.ActivityMainBinding
import com.cookbook.app.interaces.OnFragmentChangeListener
import com.cookbook.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnFragmentChangeListener {

    private lateinit var context: Context
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    var mainMenu:Menu?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this
        authViewModel.fetUserDetails(authViewModel.getLoggedUserId())

        setUpToolbar()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment

        navController = navHostFragment.navController
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        binding.bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainMenu?.findItem(R.id.add)?.isVisible = destination.id == R.id.feedFragment
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
    }

    private fun setUpToolbar(){
        setSupportActionBar(binding.toolbar)
        binding.toolbar.apply {
            setTitleTextColor(ContextCompat.getColor(context,R.color.white))
        }
    }

    override fun navigateToFragment(fragmentId: Int, popUpId: Int,args: Bundle?) {
        navController.navigate(fragmentId,args)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        mainMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add){
            navigateToFragment(R.id.action_home_to_add_recipe)
        }
        else if (item.itemId == android.R.id.home){
            navController.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }
}