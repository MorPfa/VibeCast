package app.vibecast.presentation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MediatorLiveData
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
import app.vibecast.presentation.navigation.ImageViewModel
import app.vibecast.presentation.permissions.LocationPermissionState
import app.vibecast.presentation.permissions.PermissionHelper
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


const val TAG = "TestTag"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var searchView: SearchView
    private val mainScreenViewModel : MainScreenViewModel by viewModels()
    private val imageViewModel : ImageViewModel by viewModels()
    private lateinit var currentImage : ImageDto
    private var showIcons : Boolean = true
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
            when(destination.id){
                R.id.nav_account -> {   showIcons = false
                                        invalidateOptionsMenu()
                }
                R.id.nav_pictures -> {  showIcons = false
                                        invalidateOptionsMenu()
                }
                R.id.nav_settings -> {  showIcons = false
                                        invalidateOptionsMenu()
                }
                R.id.nav_home -> {      showIcons = true
                                        invalidateOptionsMenu()
                }
                R.id.nav_search -> {  showIcons = true
                                        invalidateOptionsMenu()
                }
                R.id.nav_saved -> {  showIcons = true
                                        invalidateOptionsMenu()
                }
            }
        }

    }

    private fun handleLocationAndWeather() {
        // Check if location permission is granted
        if (permissionHelper.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Permission is granted, load weather data
            mainScreenViewModel.updatePermissionState(LocationPermissionState.Granted)
            mainScreenViewModel.loadCurrentLocationWeather()
        } else {
            // Permission not granted, request it
            permissionHelper.requestPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
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
                    mainScreenViewModel.updatePermissionState(LocationPermissionState.Granted)
                    mainScreenViewModel.loadCurrentLocationWeather()
                } else {
                    // Permission denied, update state and handle accordingly
                    mainScreenViewModel.updatePermissionState(LocationPermissionState.Denied)
                    mainScreenViewModel.loadCurrentLocationWeather()
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
                if (isInputValid(query)){
                    search.collapseActionView()
                    mainScreenViewModel.getSearchedLocationWeather(query)
                    val currentDestination = findNavController(R.id.nav_host_fragment_content_home).currentDestination
                    if (currentDestination?.id == R.id.nav_home) {
                        val action = MainScreenFragmentDirections.actionNavHomeToSearchResultFragment()
                        findNavController(R.id.nav_host_fragment_content_home).navigate(action)
                    }
                    else if (currentDestination?.id == R.id.nav_saved) {
                        val action =
                            SavedLocationFragmentDirections.actionNavSavedToNavSearch()
                        findNavController(R.id.nav_host_fragment_content_home).navigate(action)
                    }
                    searchView.clearFocus()

                }
                else {
                    val snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.invalid_query_input),
                        Snackbar.LENGTH_SHORT
                    )

                    val snackbarView = snackbar.view
                    val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    snackbarText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    snackbarView.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.snackbar_background)
                    val params = snackbarView.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    snackbarView.layoutParams = params
                    val actionBarHeight = getActionBarHeight()
                    params.setMargins(0, actionBarHeight, 0, 0)
                    snackbarView.layoutParams = params
                    if (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
                        params.gravity = Gravity.CENTER_HORIZONTAL
                    }
                    snackbar.show()
                }
                invalidateOptionsMenu()
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        }
        )
        return true
    }
    fun isInputValid(input: String): Boolean {
        val validCharactersWithSpaces = Regex("[a-zA-Z ]+")
        return input.matches(validCharactersWithSpaces)
    }


    fun getActionBarHeight(): Int {
        val typedValue = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            return (TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)) + 100
        }
        return 0
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val saveImageItem = menu.findItem(R.id.action_save_image)
        val saveLocationItem = menu.findItem(R.id.action_save_location)
        val searchItem = menu.findItem(R.id.action_search)
        if (showIcons) {
            saveImageItem?.isVisible = true
            saveLocationItem?.isVisible = true
            searchItem?.isVisible = true
            saveImageItem?.icon = ContextCompat.getDrawable(this, R.drawable.favorite_unselected)
        } else {
            saveImageItem?.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
            saveImageItem?.isVisible = false
            saveLocationItem?.isVisible = false
            searchItem?.isVisible = false
        }

        return super.onPrepareOptionsMenu(menu)
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_image -> {
                if (imageViewModel.image.value != null) {
                    if (!item.isChecked) {
                        item.isChecked = true
                        imageViewModel.addImage(currentImage)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
                    } else {
                        item.isChecked = false
                        imageViewModel.deleteImage(currentImage)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_unselected)
                    }
                }
                true
            }
            R.id.action_save_location -> {
                if (item.isCheckable) {
                    if (!item.isChecked) {
                        item.isChecked = true

                        mainScreenViewModel.addLocation(mainScreenViewModel.currentLocation.value!!)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.delete_location_icon)
                    } else {
                        item.isChecked = false
                        mainScreenViewModel.deleteLocation(mainScreenViewModel.currentLocation.value!!)
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
            mainScreenViewModel.loadCurrentLocationWeather()
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}