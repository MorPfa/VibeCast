package app.vibecast.presentation


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
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
import app.vibecast.presentation.mainscreen.CurrentLocationViewModel
import app.vibecast.presentation.navigation.AccountViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint


const val TAG = "TestTag"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var searchView: SearchView
    private val currentLocationViewModel : CurrentLocationViewModel by viewModels()
    private val accountViewModel : AccountViewModel by viewModels()
    private val savedLocationViewModel: SavedLocationViewmodel by viewModels()
    private lateinit var imageList : List<ImageDto>
    private lateinit var currentImage : ImageDto
    private lateinit var currentLocation : LocationDto
    private var isCurrentLocationFragmentVisible: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_account, R.id.nav_pictures, R.id.nav_settings
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
        currentLocationViewModel.weather.observe(this){
            currentLocation = LocationDto(it.location.cityName, it.location.locationIndex)

        }



    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val search = menu.findItem(R.id.action_search)
        searchView = search.actionView as SearchView


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val action = CurrentLocationFragmentDirections.actionNavHomeToSavedLocationFragment()
                savedLocationViewModel.getSearchedLocationWeather(query)
                findNavController(R.id.nav_host_fragment_content_home).navigate(action)

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
        currentLocationViewModel.galleryImages.observe(this){
            imageList = it

        }
        currentLocationViewModel.image.observe(this){
            currentImage = it
        }

        return when (item.itemId) {
            R.id.action_save_image -> {
                if (item.isCheckable) {
                    if (!item.isChecked) {
                        item.isChecked = true
                        currentLocationViewModel.addImage(currentImage)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
                    } else {
                        item.isChecked = false
                        currentLocationViewModel.deleteImage(currentImage)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_unselected)
                    }
                }
                true
            }
            R.id.action_save_location -> {
                if (item.isCheckable) {
                    if (!item.isChecked) {
                        item.isChecked = true
                        accountViewModel.addLocation(currentLocationViewModel.weather.value!!.location)
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
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}