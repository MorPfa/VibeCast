package app.vibecast.presentation.screens.main_screen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.vibecast.R
import app.vibecast.databinding.FragmentMainScreenBinding
import app.vibecast.domain.model.SongDto
import app.vibecast.presentation.screens.account_screen.AccountViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel
import app.vibecast.presentation.screens.main_screen.music.TrackProgressBar
import app.vibecast.presentation.screens.main_screen.music.util.InfoType
import com.google.android.material.snackbar.Snackbar
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Repeat
import com.spotify.sdk.android.auth.AuthorizationClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class MainScreenFragment : Fragment() {
    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var actionBar: ActionBar
    private val mainViewModel: MainViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    private var playbackButton: ImageButton? = null
    private var shuffleButton: ImageButton? = null
    private var repeatButton: ImageButton? = null
    private var saveSongButton: ImageView? = null
    private var trackProgressBar: TrackProgressBar? = null
    private lateinit var snackBar: Snackbar
    private var alertDialog: AlertDialog? = null


    private fun updateCapabilities(canPlayOnDemand: Boolean) {
        if (canPlayOnDemand) {
            binding.musicWidget.progressBar.isEnabled = true
            binding.musicWidget.forwardButton.isEnabled = true
            binding.musicWidget.rewindButton.isEnabled = true
        } else {
            binding.musicWidget.rewindButton.setOnClickListener {
                showSpotifyPremiumDialog()
            }
            binding.musicWidget.forwardButton.setOnClickListener {
                showSpotifyPremiumDialog()
            }
            binding.musicWidget.progressBar.isEnabled = false

            binding.musicWidget.progressBar.thumb =
                ContextCompat.getDrawable(requireContext(), R.drawable.thumb_disabled)
            binding.musicWidget.forwardButton.setImageResource(R.drawable.skip_next_disabled)
            binding.musicWidget.rewindButton.setImageResource(R.drawable.skip_previous_disabled)
        }
    }


    private fun updatePlaybackBtn(playerState: PlayerState) {
        if (playerState.isPaused) {
            playbackButton?.setImageResource(R.drawable.play_btn)
        } else {

            playbackButton?.setImageResource(R.drawable.pause_btn)
        }
    }


    private fun updateTrackCoverArt(playerState: PlayerState) {
        musicViewModel.spotifyAppRemote?.let {
            it.imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.X_SMALL)
            .setResultCallback { bitmap ->
                binding.musicWidget.albumArtImageView.setImageBitmap(bitmap)
            }
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
            findNavController().navigate(MainScreenFragmentDirections.navHomeToWeb(InfoType.SONG))
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
            findNavController().navigate(MainScreenFragmentDirections.navHomeToWeb(InfoType.ARTIST))
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
        trackProgressBar?.apply {
            if (playerState.playbackSpeed > 0) {
                unpause()
            } else {
                pause()
            }
            // Invalidate seekbar length and position
            binding.musicWidget.progressBar.max = playerState.track.duration.toInt()
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

    private fun showSpotifyPremiumDialog() {
        // Create the object of AlertDialog Builder class
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

    private fun showSpotifySnackBar() {
        snackBar = Snackbar.make(
            requireView(),
            "Error authenticating with spotify",
            Snackbar.LENGTH_SHORT
        )
        val snackBarView = snackBar.view
        val snackBarText =
            snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackBarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackBarView.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
        snackBar.show()
        Handler(Looper.getMainLooper()).postDelayed({ snackBar.dismiss() }, 4000)
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
            val snackBarText = "Spotify is not installed"
            val clickableText = "GET SPOTIFY FREE"


            val snackBar = Snackbar.make(
                requireView(),
                snackBarText,
                Snackbar.LENGTH_SHORT
            )

            snackBar.show()

            snackBar.setAction(clickableText) {
                AuthorizationClient.openDownloadSpotifyActivity(requireActivity())
                snackBar.dismiss()
            }
            snackBar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            return false
        } else {
            return true
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        _binding = FragmentMainScreenBinding.inflate(inflater, container, false)

        trackProgressBar =
            TrackProgressBar(binding.musicWidget.progressBar) { seekToPosition: Long ->
                musicViewModel.seekTo(seekToPosition)
            }


        musicViewModel.userCapabilitiesState.observe(viewLifecycleOwner) { canPlayOnDemand ->
            updateCapabilities(canPlayOnDemand)
        }
        musicViewModel.playerState.observe(viewLifecycleOwner) { playerState ->
            if(playerState != null){
                updateUI(playerState)
            }
        }


        playbackButton = binding.musicWidget.playPauseButton
        shuffleButton = binding.musicWidget.shuffleButton
        repeatButton = binding.musicWidget.repeatButton
        saveSongButton = binding.musicWidget.saveSongBtn

        saveSongButton?.setOnClickListener {
            val playerState = musicViewModel.playerState.value
            playerState?.let {
                musicViewModel.saveSong(
                    SongDto(
                        album = playerState.track.album.name,
                        name = playerState.track.name,
                        imageUri = playerState.track.imageUri,
                        url = "",
                        trackUri = playerState.track.uri,
                        previewUrl = null,
                        artist = playerState.track.artist.name,
                        artistUri = playerState.track.artist.uri,
                        albumUri = playerState.track.artist.uri,
                    )
                )
                accountViewModel.addSongToFirebase(
                    SongDto(
                        album = playerState.track.album.name,
                        name = playerState.track.name,
                        imageUri = playerState.track.imageUri,
                        url = "",
                        trackUri = playerState.track.uri,
                        previewUrl = null,
                        artist = playerState.track.artist.name,
                        artistUri = playerState.track.artist.uri,
                        albumUri = playerState.track.artist.uri,
                    )
                )
                snackBar = Snackbar.make(
                    requireView(),
                    "Added song to your library",
                    Snackbar.LENGTH_SHORT
                )
                val snackBarView = snackBar.view
                val snackBarText =
                    snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackBarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                snackBarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                snackBar.show()
                Handler(Looper.getMainLooper()).postDelayed({ snackBar.dismiss() }, 2000)
            }

        }

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

        actionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        actionBar.show()


        val nextScreenButton = binding.nextScreenButton
        nextScreenButton.setOnClickListener {
            if (mainViewModel.locations.value?.size != 0) {
                mainViewModel.getSavedLocationWeather()
                val action =
                    MainScreenFragmentDirections.homeToSaved()
                findNavController().navigate(action)
            } else {
                snackBar = Snackbar.make(
                    requireView(),
                    getString(R.string.none_saved_warning),
                    Snackbar.LENGTH_SHORT
                )
                val snackbarView = snackBar.view
                val snackbarText =
                    snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackbarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                snackbarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                snackBar.show()
            }
        }
        return binding.root
    }


    /**
     * Loads and sets new image as background image when location or weather conditions change
     */
    private fun observeImageData(city: String, weather: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                imageViewModel.loadImage(city, weather)
                    .collect { imageDto ->
                        imageDto!!.urls.regular.let { imageUrl ->
                            withContext(Dispatchers.Main) {
                                imageViewModel.setImageLiveData(imageDto)
                                imageViewModel.loadImageIntoImageView(
                                    imageUrl, binding.backgroundImageView
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    snackBar = Snackbar.make(
                        requireView(),
                        getString(R.string.error_loading_image),
                        Snackbar.LENGTH_SHORT
                    )
                    val image = imageViewModel.pickDefaultImage(weather)
                    binding.backgroundImageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            image
                        )
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            mainViewModel.currentWeather.distinctUntilChanged()
                .observe(viewLifecycleOwner) { weatherData ->

                    weatherData.weather.currentWeather?.let { currentWeather ->
                        val city = weatherData.location.cityName
                        val weather =
                            weatherData.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                        observeImageData(city, weather!!)
                        musicViewModel.token.observe(viewLifecycleOwner) {
                            if (it != null) {
                                musicViewModel.getPlaylist(weather)
                            }
                        }



                        binding.mainTempDisplay.text =
                            getString(R.string.center_temp, currentWeather.temperature)
                        //            Current hour values
                        binding.centerTempRow.leftWeather.text =
                            currentWeather.weatherConditions[0].mainDescription
                        binding.centerTempRow.leftTemp.text = currentWeather.temperature.toString()
                        binding.centerTempRow.leftTime.text =
                            weatherData.weather.hourlyWeather?.get(0)?.timestamp
                        //            Next hour values
                        binding.centerTempRow.centerWeather.text =
                            weatherData.weather.hourlyWeather?.get(1)?.weatherConditions?.get(0)?.mainDescription
                        binding.centerTempRow.centerTemp.text =
                            weatherData.weather.hourlyWeather?.get(1)?.temperature.toString()
                        binding.centerTempRow.centerTime.text =
                            weatherData.weather.hourlyWeather?.get(1)?.timestamp.toString()
                        //            2 hours from now values
                        binding.centerTempRow.rightWeather.text =
                            weatherData.weather.hourlyWeather?.get(2)?.weatherConditions?.get(0)?.mainDescription
                        binding.centerTempRow.rightTemp.text =
                            weatherData.weather.hourlyWeather?.get(2)?.temperature.toString()
                        binding.centerTempRow.rightTime.text =
                            weatherData.weather.hourlyWeather?.get(2)?.timestamp.toString()
                        binding.locationDisplay.text =
                            getString(
                                R.string.center_location_text,
                                weatherData.location.cityName,
                                weatherData.location.country
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
                                weatherData.weather.hourlyWeather?.get(0)?.chanceOfRain
                            )
                        binding.mainWeatherWidget.uvIndexTv.text =
                            getString(R.string.uv_index_value, currentWeather.uvi)
                        binding.mainWeatherWidget.humidtyTv.text =
                            getString(
                                R.string.humidity, currentWeather.humidity
                            )

                    }
                }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.currentWeather.removeObservers(viewLifecycleOwner)
        musicViewModel.currentSong.removeObservers(viewLifecycleOwner)
        musicViewModel.playerState.removeObservers(viewLifecycleOwner)
        imageViewModel.backgroundImage.removeObservers(viewLifecycleOwner)

        if (::snackBar.isInitialized) {
            snackBar.dismiss()
        }
        playbackButton?.setOnClickListener(null)
        shuffleButton?.setOnClickListener(null)
        repeatButton?.setOnClickListener(null)
        binding.musicWidget.forwardButton.setOnClickListener(null)
        binding.musicWidget.rewindButton.setOnClickListener(null)
        binding.nextScreenButton.setOnClickListener(null)
        _binding = null
        alertDialog = null
        playbackButton = null
        repeatButton = null
        shuffleButton = null
        playbackButton = null
        trackProgressBar = null
        saveSongButton = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


}