package app.vibecast.presentation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.vibecast.R
import app.vibecast.databinding.ActivityMainBinding
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationDto
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import app.vibecast.presentation.navigation.AccountViewModel
import app.vibecast.presentation.permissions.LocationPermissionState
import app.vibecast.presentation.permissions.PermissionHelper
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint


const val TAG = "TestTag"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var searchView: SearchView
    private val accountViewModel : AccountViewModel by viewModels()
    private val viewModel : MainScreenViewModel by viewModels()
    private lateinit var currentImage : ImageDto
    private lateinit var currentLocation : LocationDto
    private var isCurrentLocationFragmentVisible: Boolean = true
    private lateinit var permissionHelper: PermissionHelper


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionHelper = PermissionHelper(this)
        handleLocationAndWeather()
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_account,
                R.id.nav_pictures,
                R.id.nav_settings,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        NavigationUI.setupWithNavController(binding.navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            isCurrentLocationFragmentVisible = destination.id == R.id.nav_home
            if (isCurrentLocationFragmentVisible) {
                invalidateOptionsMenu()
            }
        }


        viewModel.currentWeather.observe(this){
            currentLocation = LocationDto(it.location.cityName, it.location.country)

        }
        viewModel.image.observe(this){
            currentImage = it
        }

    }

    private fun handleLocationAndWeather() {
        // Check if location permission is granted
        if (permissionHelper.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Permission is granted, load weather data
            viewModel.updatePermissionState(LocationPermissionState.Granted)
            viewModel.loadCurrentLocationWeather()
        } else {
            // Permission not granted, request it
            permissionHelper.requestPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                "Location permission is required to get weather data for your current location.",
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, update state and load weather data
                    viewModel.updatePermissionState(LocationPermissionState.Granted)
                    viewModel.loadCurrentLocationWeather()
                } else {
                    // Permission denied, update state and handle accordingly
                    viewModel.updatePermissionState(LocationPermissionState.Denied)
                    viewModel.loadCurrentLocationWeather()
                    Toast.makeText(this,
                        getString(R.string.location_request_toast), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val search = menu.findItem(R.id.action_search)
        searchView = search.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.getSearchedLocationWeather(query)
                val currentDestination = findNavController(R.id.nav_host_fragment_content_home).currentDestination
                if (currentDestination?.id == R.id.nav_home) {
                    val action = MainScreenFragmentDirections.actionNavHomeToSearchResultFragment()
                    findNavController(R.id.nav_host_fragment_content_home).navigate(action)
                }
                else if(currentDestination?.id == R.id.nav_saved) {
                    val action = SavedLocationFragmentDirections.actionNavSavedToNavSearch()
                    findNavController(R.id.nav_host_fragment_content_home).navigate(action)
                }

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val saveImageItem = menu.findItem(R.id.action_save_image)
        if (isCurrentLocationFragmentVisible) {
            saveImageItem?.icon = ContextCompat.getDrawable(this, R.drawable.favorite_unselected)
        } else {
            saveImageItem?.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
        }

        return super.onPrepareOptionsMenu(menu)
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_image -> {
                if (item.isCheckable) {
                    if (!item.isChecked) {
                        item.isChecked = true
                        viewModel.addImage(currentImage)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
                    } else {
                        item.isChecked = false
                        viewModel.deleteImage(currentImage)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_unselected)
                    }
                }
                true
            }
            R.id.action_save_location -> {
                if (item.isCheckable) {
                    if (!item.isChecked) {
                        item.isChecked = true
                        accountViewModel.addLocation(viewModel.currentWeather.value!!.location)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.delete_location_icon)
                    } else {
                        item.isChecked = false
                        accountViewModel.deleteLocation(currentLocation)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.save_location_icon)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        if (navController.currentDestination?.id == R.id.nav_search) {
            viewModel.loadCurrentLocationWeather()
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}