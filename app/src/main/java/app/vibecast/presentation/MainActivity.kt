package app.vibecast.presentation


import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.vibecast.R
import app.vibecast.databinding.ActivityMainBinding
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.presentation.navigation.AccountViewModel
import app.vibecast.presentation.mainscreen.CurrentLocationViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


const val TAG = "TestTag"


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var searchView: SearchView
    private val currentLocationViewModel : CurrentLocationViewModel by viewModels()
    private val accountViewModel : AccountViewModel by viewModels()
    private lateinit var imageList : List<ImageDto>
    private lateinit var currentImage : ImageDto
    private lateinit var currentLocation : LocationDto

    @Inject
    lateinit var locationRepository: LocationRepository
    @Inject
    lateinit var  imageRepository: ImageRepository
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

        currentLocationViewModel.galleryImages.observe(this){
//            Log.d(TAG, it[0].id)
            imageList = it

        }
        currentLocationViewModel.image.observe(this){
            currentImage = it
        }
        currentLocationViewModel.weather.observe(this){
            currentLocation = LocationDto(it.location.cityName, it.location.locationIndex)

        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val search = menu.findItem(R.id.action_search)
        searchView = search.actionView as SearchView

//        Log.d(TAG, currentLocationViewModel.galleryImages.value.toString())
        Log.d(TAG, currentLocationViewModel.image.value.toString())


        // Set up the OnQueryTextListener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // This method is called when the user submits their search query

                return true // Return true to indicate that query change has been handled
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // This method is called when the text in the search view changes
                // used for real-time search suggestions or filtering
                // newText parameter represents the current query
                return true // Return true to indicate that query change has been handled
            }
        })

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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