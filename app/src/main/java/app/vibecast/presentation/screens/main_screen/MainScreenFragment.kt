package app.vibecast.presentation.screens.main_screen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.vibecast.R
import app.vibecast.databinding.FragmentMainScreenBinding
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel
import app.vibecast.presentation.screens.main_screen.music.TrackProgressBar
import com.google.android.material.snackbar.Snackbar
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Repeat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class MainScreenFragment : Fragment(), MusicViewModel.PlayerStateListener {
    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var actionBar: ActionBar
    private val mainViewModel: MainViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val musicViewModel: MusicViewModel by activityViewModels()
    private lateinit var playbackButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private lateinit var repeatButton: ImageButton
    private lateinit var trackProgressBar: TrackProgressBar


    private fun updatePlaybackBtn(playerState: PlayerState) {

        if (playerState.isPaused) {
            playbackButton.setImageResource(R.drawable.play_btn)
        } else {

            playbackButton.setImageResource(R.drawable.pause_btn)
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
                val spotifyUri = playerState.track.uri
                showInfoInSpotify(spotifyUri)

            }
            artistNameTextView.text = playerState.track.artist.name
            artistNameTextView.setOnClickListener {
                val spotifyUri = playerState.track.artist.uri
                showInfoInSpotify(spotifyUri)

            }

        }
    }


    private fun showInfoInSpotify(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val packageManager = context?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            // Open the Spotify app
            context?.startActivity(intent)
        } else {
//            // If Spotify app is not installed, open the Spotify web page instead
            findNavController().navigate(MainScreenFragmentDirections.navHomeToWeb())
        }
    }



//    private fun openUrlInBrowser(url: String) {
//
//
////        val intent = CustomTabsIntent.Builder().build()
////        val headers = HashMap<String, String>().apply {
////            put("Authorization", "Bearer ${musicViewModel.token.value}")
////        }
////        intent.intent.putExtra(Browser.EXTRA_HEADERS, headers)
////
////        val headersBundle = intent.intent.getBundleExtra(Browser.EXTRA_HEADERS)
////
////        Timber.tag("spotify").d("Headers: $headersBundle")
////        Timber.tag("spotify").d("Headers: ${intent.intent.extras}")
////        Timber.tag("spotify").d("Url $url")
////        Timber.tag("spotify").d("Url parsed ${Uri.parse(url)}")
////
////
////        intent.launchUrl(requireContext(), Uri.parse(url))
//
//    }


    private fun updateShuffleBtn(playerState: PlayerState) {
        shuffleButton.apply {
            if (playerState.playbackOptions.isShuffling) {
                shuffleButton.setImageResource(R.drawable.shuffle_enabled)
            } else {
                shuffleButton.setImageResource(R.drawable.shuffle_disabled)
            }
        }

    }

    private fun updateSeekbar(playerState: PlayerState) {
        // Update progressbar
        trackProgressBar.apply {
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
        repeatButton.apply {
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

    override fun onPlayerStateUpdated(playerState: PlayerState) {
        updateUI(playerState)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        _binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        musicViewModel.setPlayerStateListener(this)
        trackProgressBar =
            TrackProgressBar(binding.musicWidget.progressBar) { seekToPosition: Long ->
                musicViewModel.seekTo(seekToPosition)
            }
        playbackButton = binding.musicWidget.playPauseButton
        shuffleButton = binding.musicWidget.shuffleButton
        repeatButton = binding.musicWidget.repeatButton
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        actionBar.show()



        repeatButton.setOnClickListener {
            musicViewModel.setRepeatStatus()
        }
        playbackButton.setOnClickListener {
            try {
                musicViewModel.onPlayPauseButtonClicked()
            } catch (e: Exception) {
                val snackbar = Snackbar.make(
                    requireView(),
                    "Log into spotify to enable music",
                    Snackbar.LENGTH_SHORT
                )
                val snackbarView = snackbar.view
                val snackbarText =
                    snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackbarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                snackbarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                snackbar.show()
            }

        }
        shuffleButton.setOnClickListener {
            musicViewModel.setShuffleStatus()
        }
        binding.musicWidget.forwardButton.setOnClickListener {
            musicViewModel.onSkipNextButtonClicked()
        }
        binding.musicWidget.rewindButton.setOnClickListener {
            musicViewModel.onSkipPreviousButtonClicked()
        }

        val nextScreenButton = binding.nextScreenButton
        nextScreenButton.setOnClickListener {
            if (mainViewModel.locations.value?.size != 0) {
                mainViewModel.getSavedLocationWeather()
                val action =
                    MainScreenFragmentDirections.homeToSaved()
                findNavController().navigate(action)
            } else {
                val snackbar = Snackbar.make(
                    requireView(),
                    getString(R.string.none_saved_warning),
                    Snackbar.LENGTH_SHORT
                )
                val snackbarView = snackbar.view
                val snackbarText =
                    snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackbarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                snackbarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                snackbar.show()
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
                    val snackbar = Snackbar.make(
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
                    val snackbarView = snackbar.view
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
                    snackbar.show()
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
//                        musicViewModel.getPlaylist(weather!!)


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
        _binding = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


}