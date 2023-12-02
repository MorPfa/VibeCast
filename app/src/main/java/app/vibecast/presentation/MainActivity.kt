package app.vibecast.presentation


import android.os.Bundle
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
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.presentation.navigation.AccountViewModel
import app.vibecast.presentation.weather.CurrentLocationViewModel
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

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_image -> {
                currentLocationViewModel.addImage(currentLocationViewModel.image.value!!)
                item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
                true
            }
            R.id.action_save_location -> {
                accountViewModel.addLocation(currentLocationViewModel.weather.value!!.location)
                item.icon = ContextCompat.getDrawable(this, R.drawable.delete_location_icon)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        val search = menu.findItem(R.id.action_search)
        searchView = search.actionView as SearchView

        val saveImage = menu.findItem(R.id.action_save_image)
        if(currentLocationViewModel.galleryImages.value?.contains(currentLocationViewModel.image.value!!) == true){
            saveImage.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
        }
        else {
            // If you want to set a different icon when the condition is not met
            saveImage.icon = ContextCompat.getDrawable(this, R.drawable.favorite_unselected)
        }
        val saveLocation = menu.findItem(R.id.action_save_location)



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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}