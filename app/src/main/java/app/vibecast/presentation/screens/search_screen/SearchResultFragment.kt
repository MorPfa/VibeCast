package app.vibecast.presentation.screens.search_screen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.vibecast.R
import app.vibecast.databinding.FragmentSearchResultBinding
import app.vibecast.presentation.screens.main_screen.MainViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel
import app.vibecast.presentation.screens.main_screen.music.TrackProgressBar
import app.vibecast.presentation.screens.main_screen.music.util.InfoType
import com.google.android.material.snackbar.Snackbar
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Repeat
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val musicViewModel: MusicViewModel by activityViewModels()
    private var playbackButton: ImageButton? = null
    private var shuffleButton: ImageButton? = null
    private var repeatButton: ImageButton? = null
    private var trackProgressBar: TrackProgressBar? = null
    private lateinit var snackBar: Snackbar
    private var alertDialog: AlertDialog? = null



    private fun updateCapabilities(canPlayOnDemand: Boolean) {
        if (canPlayOnDemand) {
            binding.musicWidget.progressBar.isEnabled = true
            binding.musicWidget.forwardButton.isEnabled = true
            binding.musicWidget.rewindButton.isEnabled = true
        } else {
            binding.musicWidget.rewindButton.setOnClickListener{
                showSpotifyPremiumDialog()
            }
            binding.musicWidget.forwardButton.setOnClickListener{
                showSpotifyPremiumDialog()
            }
            binding.musicWidget.progressBar.isEnabled = false

            binding.musicWidget.progressBar.thumb =
                ContextCompat.getDrawable(requireContext(), R.drawable.thumb_disabled)
            binding.musicWidget.forwardButton.setImageResource(R.drawable.skip_next_disabled)
            binding.musicWidget.rewindButton.setImageResource(R.drawable.skip_previous_disabled)
        }
    }
    private fun showSpotifyPremiumDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val customView =
            LayoutInflater.from(requireContext()).inflate(R.layout.spotify_premium_dialog, null)

        alertDialogBuilder.setView(customView)
        alertDialog = alertDialogBuilder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = alertDialog?.window?.attributes
        layoutParams?.y = -200
        alertDialog?.window?.attributes = layoutParams
        alertDialog?.show()
        Handler(Looper.getMainLooper()).postDelayed({ alertDialog?.dismiss() }, 3000)
    }

    private fun updatePlaybackBtn(playerState: PlayerState) {
        if (playerState.isPaused) {
            playbackButton?.setImageResource(R.drawable.play_btn)
        } else {

            playbackButton?.setImageResource(R.drawable.pause_btn)
        }
    }


    private fun updateTrackCoverArt(playerState: PlayerState) {
        // Get image from track
        musicViewModel.assertAppRemoteConnected()
            .imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.X_SMALL)
            .setResultCallback { bitmap ->
                binding.musicWidget.albumArtImageView.setImageBitmap(bitmap)
            }
    }

    private fun updateTrackInfo(playerState: PlayerState) {
        binding.musicWidget.apply {
            songTitleTextView.text = playerState.track.name
            songTitleTextView.setOnClickListener {
                showSongInfoInSpotify(playerState)

            }
            artistNameTextView.text = playerState.track.artist.name
            artistNameTextView.setOnClickListener {
                showArtistInfoInSpotify(playerState)

            }

        }
    }


    private fun showSongInfoInSpotify(playerState: PlayerState) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playerState.track.uri))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val packageManager = context?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            // Open the Spotify app
            context?.startActivity(intent)
        } else {
//            // If Spotify app is not installed, open the Spotify web page instead
            musicViewModel.getCurrentSong(playerState.track.name, playerState.track.artist.name)
            val action = SearchResultFragmentDirections.searchToWeb(InfoType.SONG)
            findNavController().navigate(action)
        }
    }

    private fun showArtistInfoInSpotify(playerState: PlayerState) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playerState.track.artist.uri))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val packageManager = context?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            // Open the Spotify app
            context?.startActivity(intent)
        } else {
//            // If Spotify app is not installed, open the Spotify web page instead
            musicViewModel.getCurrentSong(playerState.track.name, playerState.track.artist.name)
            val action = SearchResultFragmentDirections.searchToWeb(InfoType.ARTIST)
            findNavController().navigate(action)
        }
    }

    private fun updateShuffleBtn(playerState: PlayerState) {
        shuffleButton.apply {
            if (playerState.playbackOptions.isShuffling) {
                shuffleButton?.setImageResource(R.drawable.shuffle_enabled)
            } else {
                shuffleButton?.setImageResource(R.drawable.shuffle_disabled)
            }
        }

    }

    private fun updateSeekbar(playerState: PlayerState) {
        // Update progressbar
        trackProgressBar?.apply {
            if (playerState.playbackSpeed > 0) {
                unpause()
            } else {
                pause()
            }
            // Invalidate seekbar length and position
            binding.musicWidget.progressBar.max = playerState.track.duration.toInt()
            binding.musicWidget.progressBar.isEnabled = true
            setDuration(playerState.track.duration)
            update(playerState.playbackPosition)
        }
    }

    private fun updateRepeatBtn(playerState: PlayerState) {
        repeatButton?.apply {
            when (playerState.playbackOptions.repeatMode) {
                Repeat.ALL -> {
                    setImageResource(R.drawable.repeat_all_enabled)
                }
                Repeat.ONE -> {
                    setImageResource(R.drawable.repeat_one_enabled)
                }
                else -> {
                    setImageResource(R.drawable.repeat_disabled2)
                }
            }
        }
    }


    private fun updateUI(playerState: PlayerState) {
        updatePlaybackBtn(playerState)
        updateShuffleBtn(playerState)
        updateRepeatBtn(playerState)
        updateSeekbar(playerState)
        updateTrackCoverArt(playerState)
        updateTrackInfo(playerState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            mainViewModel.checkPermissionState()
            findNavController().navigate(R.id.nav_home)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        mainViewModel.searchedWeather.distinctUntilChanged()
            .observe(viewLifecycleOwner) { weatherState ->
                if(weatherState.combinedData != null) {

                    weatherState.combinedData.weather.currentWeather?.let { currentWeather ->
                        val city = weatherState.combinedData.location.cityName
                        val weather =
                            weatherState.combinedData.weather.currentWeather?.weatherConditions?.get(
                                0
                            )?.mainDescription
                        imageViewModel.loadImage(city, weather!!)
                        musicViewModel.token.observe(viewLifecycleOwner) {
                            if (it != null) {
                                musicViewModel.getPlaylist(weather)
                            }
                        }

                        binding.mainTemp.text =
                            getString(R.string.center_temp, currentWeather.temperature)
                        //            Current hour values
                        binding.centerTempRow.leftWeather.text =
                            currentWeather.weatherConditions[0].mainDescription
                        binding.centerTempRow.leftTemp.text = currentWeather.temperature.toString()
                        binding.centerTempRow.leftTime.text =
                            weatherState.combinedData.weather.hourlyWeather?.get(0)?.timestamp
                        //            Next hour values
                        binding.centerTempRow.centerWeather.text =
                            weatherState.combinedData.weather.hourlyWeather?.get(1)?.weatherConditions?.get(
                                0
                            )?.mainDescription
                        binding.centerTempRow.centerTemp.text =
                            weatherState.combinedData.weather.hourlyWeather?.get(1)?.temperature.toString()
                        binding.centerTempRow.centerTime.text =
                            weatherState.combinedData.weather.hourlyWeather?.get(1)?.timestamp.toString()
                        //            2 hours from now values
                        binding.centerTempRow.rightWeather.text =
                            weatherState.combinedData.weather.hourlyWeather?.get(2)?.weatherConditions?.get(
                                0
                            )?.mainDescription
                        binding.centerTempRow.rightTemp.text =
                            weatherState.combinedData.weather.hourlyWeather?.get(2)?.temperature.toString()
                        binding.centerTempRow.rightTime.text =
                            weatherState.combinedData.weather.hourlyWeather?.get(2)?.timestamp.toString()
                        binding.locationDisplay.text =
                            getString(
                                R.string.center_location_text,
                                weatherState.combinedData.location.cityName,
                                weatherState.combinedData.location.country
                            )
                        binding.mainWeatherWidget.feelsLikeTv.text =
                            getString(R.string.feels_like, currentWeather.feelsLike)
                        binding.mainWeatherWidget.windSpeedTv.text =
                            getString(R.string.wind_speed, currentWeather.windSpeed)
                        binding.mainWeatherWidget.visibilityValue.text =
                            getString(R.string.visibility, currentWeather.visibility)

                        binding.mainWeatherWidget.chanceOfRainTv.text =
                            getString(
                                R.string.chance_of_rain,
                                weatherState.combinedData.weather.hourlyWeather?.get(0)?.chanceOfRain
                            )
                        binding.mainWeatherWidget.uvIndexTv.text =
                            getString(R.string.uv_index_value, currentWeather.uvi)
                        binding.mainWeatherWidget.humidtyTv.text =
                            getString(
                                R.string.humidity, currentWeather.humidity
                            )
                    }
                }else {
                    snackBar = Snackbar.make(
                        requireView(),
                        "ERROR",
                        Snackbar.LENGTH_SHORT
                    )

                    val snackbarView = snackBar.view
                    val snackbarText =
                        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    snackbarText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    snackbarView.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                    snackBar.show()

                }
            }


        imageViewModel.currentImage.observe(viewLifecycleOwner) { imageState ->
            if (imageState.image != null) {
                imageViewModel.setImageLiveData(imageState.image)
                imageViewModel.loadImageIntoImageView(imageState.image.urls.regular, binding.backgroundImageView)
            }
            if (imageState.error != null) {
                val image = imageViewModel.pickDefaultImage(imageState.query!!)
                binding.backgroundImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        image
                    )
                )
                snackBar = Snackbar.make(
                    requireView(),
                    getString(R.string.error_loading_image),
                    Snackbar.LENGTH_SHORT
                )

                val snackbarView = snackBar.view
                val snackbarText =
                    snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackbarText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                snackbarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                snackBar.show()

            }
        }
        musicViewModel.playerState.observe(viewLifecycleOwner) { playerState ->
            if(playerState != null){
                updateUI(playerState)
            }
        }
        musicViewModel.userCapabilitiesState.observe(viewLifecycleOwner) { canPlayOnDemand ->
            updateCapabilities(canPlayOnDemand)
        }
        mainViewModel.emptySearchResponse.observe(viewLifecycleOwner) { noData ->
            if (noData) {
                snackBar = Snackbar.make(
                    requireView(),
                    getString(R.string.invalid_query_input),
                    Snackbar.LENGTH_SHORT
                )

                val snackBarView = snackBar.view
                val snackBarText =
                    snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackBarText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                snackBarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                val params = snackBarView.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP
                snackBarView.layoutParams = params
                val actionBarHeight = requireActivity().actionBar?.height
                params.setMargins(0, actionBarHeight ?: 200, 0, 0)
                snackBarView.layoutParams = params
                if (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
                    params.gravity = Gravity.CENTER_HORIZONTAL
                }
                snackBar.show()
            }
        }
        trackProgressBar =
            TrackProgressBar(binding.musicWidget.progressBar) { seekToPosition: Long ->
                musicViewModel.seekTo(seekToPosition)
            }
        playbackButton = binding.musicWidget.playPauseButton
        shuffleButton = binding.musicWidget.shuffleButton
        repeatButton = binding.musicWidget.repeatButton

        repeatButton?.setOnClickListener {
            if (isSpotifyInstalled()) {
                try {
                    musicViewModel.setRepeatStatus()
                } catch (e: Exception) {
                    showSpotifySnackBar()
                }
            }
        }
        playbackButton?.setOnClickListener {
            if (isSpotifyInstalled()) {
                try {
                    musicViewModel.onPlayPauseButtonClicked()
                } catch (e: Exception) {
                    showSpotifySnackBar()
                }
            }
        }
        shuffleButton?.setOnClickListener {
            if (isSpotifyInstalled()) {
                try {
                    musicViewModel.setShuffleStatus()
                } catch (e: Exception) {
                    showSpotifySnackBar()
                }
            }
        }
        binding.musicWidget.forwardButton.setOnClickListener {
            if (isSpotifyInstalled()) {
                try {
                    musicViewModel.onSkipNextButtonClicked()
                } catch (e: Exception) {
                    showSpotifySnackBar()
                }
            }
        }
        binding.musicWidget.rewindButton.setOnClickListener {
            if (isSpotifyInstalled()) {
                try {
                    musicViewModel.onSkipPreviousButtonClicked()
                } catch (e: Exception) {
                    showSpotifySnackBar()
                }
            }
        }
        return binding.root
    }

    private fun showSpotifySnackBar() {
        snackBar = Snackbar.make(
            requireView(),
            "Log into spotify to enable music",
            Snackbar.LENGTH_SHORT
        )
        val snackBarView = snackBar.view
        val snackBarText =
            snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackBarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackBarView.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
        snackBar.show()
    }


    private fun isSpotifyInstalled(): Boolean {
        val pm = requireContext().packageManager
        var isSpotifyInstalled: Boolean
        try {
            pm.getPackageInfo("com.spotify.music", 0)
            isSpotifyInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {
            isSpotifyInstalled = false
        }
        if (!isSpotifyInstalled) {
            snackBar = Snackbar.make(
                requireView(),
                "Please download spotify to enable music",
                Snackbar.LENGTH_SHORT
            )
            val snackBarView = snackBar.view
            val snackBarText =
                snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackBarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            snackBarView.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
            snackBar.show()
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    AuthorizationClient.openDownloadSpotifyActivity(requireActivity())
                }
            }, 2000)

            return false
        } else {
            return true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.resetIndex()
        mainViewModel.currentWeather.removeObservers(viewLifecycleOwner)
        musicViewModel.currentSong.removeObservers(viewLifecycleOwner)
        musicViewModel.playerState.removeObservers(viewLifecycleOwner)

        if (::snackBar.isInitialized) {
            snackBar.dismiss()
        }
        playbackButton?.setOnClickListener(null)
        shuffleButton?.setOnClickListener(null)
        repeatButton?.setOnClickListener(null)
        binding.musicWidget.forwardButton.setOnClickListener(null)
        binding.musicWidget.rewindButton.setOnClickListener(null)
        _binding = null
        alertDialog = null
        playbackButton = null
        repeatButton = null
        shuffleButton = null
        playbackButton = null
        trackProgressBar = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


}