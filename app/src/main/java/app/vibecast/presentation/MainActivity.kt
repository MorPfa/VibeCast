package app.vibecast.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.vibecast.R
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.databinding.ActivityMainBinding
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.presentation.weather.WeatherViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CurrentLocationFragment.OnActionBarItemClickListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var searchView: SearchView
    private lateinit var fragmentViewModel : ViewModel
    private lateinit var currentLocationFragment: Fragment

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var  imageRepository: ImageRepository



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentLocationFragment = supportFragmentManager.findFragmentByTag("nav_home")!!
        fragmentViewModel = ViewModelProvider(currentLocationFragment)[WeatherViewModel::class.java]




        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_account, R.id.nav_pictures, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.custom_searchview, searchView)
        searchItem.actionView = customView

        // Set up the OnQueryTextListener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // This method is called when the user submits their search query
                performSearch(query)
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

    override fun onSaveImageClicked(image: ImageDto) {
        imageRepository.addImage(image)
    }

    override fun onSaveLocationClicked(location: LocationWithWeatherData) {
        locationRepository.addLocationWeather(location)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_image -> {
                // Handle menu item 1 click
                true
            }
            R.id.action_save_location -> {
                // Handle menu item 2 click
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //TODO make performSearch return a location with weather object

    private fun performSearch(query: String) {
        (fragmentViewModel as WeatherViewModel).loadWeather(query)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}