package app.vibecast.presentation


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import app.vibecast.BuildConfig
import app.vibecast.R
import app.vibecast.databinding.ActivityMainBinding
import app.vibecast.presentation.screens.main_screen.MainViewModel
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.permissions.LocationPermissionState
import app.vibecast.presentation.permissions.PermissionHelper
import app.vibecast.presentation.screens.account_screen.AccountViewModel
import app.vibecast.presentation.screens.main_screen.MainScreenFragmentDirections
import app.vibecast.presentation.screens.saved_screen.SavedLocationFragmentDirections
import app.vibecast.presentation.user.auth.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


const val TAG = "TestTag"

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var searchView: SearchView
    private val mainViewModel: MainViewModel by viewModels()
    private val imageViewModel: ImageViewModel by viewModels()
    private val musicViewModel: MusicViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()
    private var showIcons: Boolean = true
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>
    private val redirectUri = "vibecast://callback"
    private val clientId = BuildConfig.SPOTIFY_KEY
    private val REQUEST_CODE = 1337


    private fun authorizeClient() {
        val request =
            AuthorizationRequest.Builder(clientId, AuthorizationResponse.Type.TOKEN, redirectUri)
                .setShowDialog(true)
                .setScopes(arrayOf("user-read-email"))
                .setCampaign("your-campaign-token")
                .build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }


    @Deprecated("Deprecated but spotify requires this implementation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    musicViewModel.connectToSpotify(response.accessToken)
                    Timber.tag("Spotify").d("authorized")
                }

                AuthorizationResponse.Type.ERROR -> {
                    Timber.tag("Spotify").d("Error while authorizing")
                }

                else -> {
                    Timber.tag("Spotify").d("I have no idea what happened")
                }
            }
        }
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        permissionHelper = PermissionHelper(this)
        handleLocationAndWeather()
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navView.setNavigationItemSelectedListener(this)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_account,
                R.id.nav_pictures,
                R.id.nav_settings,
                R.id.nav_login
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        NavigationUI.setupWithNavController(binding.navView, navController)
        val navHeader = binding.navView.getHeaderView(0)
        val profileImageView = navHeader.findViewById<ImageView>(R.id.profileImageIcon)
        val userNameTv = navHeader.findViewById<TextView>(R.id.user_name_tv)
        val userEmailTv = navHeader.findViewById<TextView>(R.id.user_email_tv)
        userNameTv.text = "Test"
        userEmailTv.text = "Test"

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_account -> {
                    showIcons = false
                    invalidateOptionsMenu()
                }

                R.id.nav_pictures -> {
                    showIcons = false
                    invalidateOptionsMenu()
                }

                R.id.nav_settings -> {
                    showIcons = false
                    invalidateOptionsMenu()
                }

                R.id.nav_home -> {
                    showIcons = true
                    invalidateOptionsMenu()
                }

                R.id.nav_search -> {
                    showIcons = true
                    invalidateOptionsMenu()
                }

                R.id.nav_saved -> {
                    showIcons = true
                    invalidateOptionsMenu()
                }

                R.id.nav_login -> {
                    showIcons = false
                    invalidateOptionsMenu()
                }

                R.id.nav_registration -> {
                    showIcons = false
                    invalidateOptionsMenu()
                }
            }
        }
        loginLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle the result, for example, update UI or reload data
                }
            }

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_login -> {
                val intent = Intent(this, LoginActivity::class.java)
                loginLauncher.launch(intent)
                return true
            }
            // Handle other item clicks if needed
            else -> return false
        }
    }


    private fun handleLocationAndWeather() {
        // Check if location permission is granted
        if (permissionHelper.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Permission is granted, load weather data
            mainViewModel.updatePermissionState(LocationPermissionState.Granted)
            mainViewModel.checkPermissionState()
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
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, update state and load weather data
                    mainViewModel.updatePermissionState(LocationPermissionState.Granted)
                    mainViewModel.checkPermissionState()
                } else {
                    // Permission denied, update state and handle accordingly
                    mainViewModel.updatePermissionState(LocationPermissionState.Denied)
                    mainViewModel.checkPermissionState()
                    Toast.makeText(
                        this,
                        getString(R.string.location_request_toast), Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    /**
     * Sets up App bar and captures search queries
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val search = menu.findItem(R.id.action_search)
        searchView = search.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (isInputValid(query)) {
                    search.collapseActionView()
                    mainViewModel.getSearchedLocationWeather(query)
                    val currentDestination =
                        findNavController(R.id.nav_host_fragment_content_home).currentDestination
                    if (currentDestination?.id == R.id.nav_home) {
                        val action =
                            MainScreenFragmentDirections.homeToSearch()
                        findNavController(R.id.nav_host_fragment_content_home).navigate(action)
                    } else if (currentDestination?.id == R.id.nav_saved) {
                        val action =
                            SavedLocationFragmentDirections.savedToSearch()
                        findNavController(R.id.nav_host_fragment_content_home).navigate(action)
                    }
                    searchView.clearFocus()

                } else {
                    val snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.invalid_query_input),
                        Snackbar.LENGTH_SHORT
                    )

                    val snackbarView = snackbar.view
                    val snackbarText =
                        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    snackbarText.setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.white
                        )
                    )
                    snackbarView.background =
                        ContextCompat.getDrawable(this@MainActivity, R.drawable.snackbar_background)
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

    /**
     * Checks if input is anything other than letters or whitespace
     * Everything else is handled by the API
     */
    fun isInputValid(input: String): Boolean {
        val validCharactersWithSpaces = Regex("[a-zA-Z ]+")
        return input.matches(validCharactersWithSpaces)
    }


    fun getActionBarHeight(): Int {
        val typedValue = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            return (TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                resources.displayMetrics
            )) + 100
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


    /**
     * Captures clicks on either of the App bar buttons to save images or locations currently in view
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_image -> {
                if (imageViewModel.image.value != null) {
                    if (!item.isChecked) {
                        item.isChecked = true
                        imageViewModel.addImage(imageViewModel.image.value!!)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_selected)
                    } else {
                        item.isChecked = false
                        imageViewModel.deleteImage(imageViewModel.image.value!!)
                        item.icon = ContextCompat.getDrawable(this, R.drawable.favorite_unselected)
                    }
                }
                true
            }

            R.id.action_save_location -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    mainViewModel.addLocation(mainViewModel.currentLocation.value!!)
                    item.icon = ContextCompat.getDrawable(this, R.drawable.delete_location_icon)
                } else {
                    item.isChecked = false
                    mainViewModel.deleteLocation(mainViewModel.currentLocation.value!!)
                    item.icon = ContextCompat.getDrawable(this, R.drawable.save_location_icon)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        if (navController.currentDestination?.id == R.id.nav_search) {
            mainViewModel.checkPermissionState()
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onStart() {
        super.onStart()
//        authorizeClient()

    }

}