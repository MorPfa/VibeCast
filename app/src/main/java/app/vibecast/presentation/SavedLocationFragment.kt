package app.vibecast.presentation

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentSavedLocationBinding
import app.vibecast.domain.entity.LocationDto
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import app.vibecast.presentation.navigation.ImageViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SavedLocationFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentSavedLocationBinding? = null
    private val binding get() = _binding!!
    private val mainScreenViewModel: MainScreenViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        _binding = FragmentSavedLocationBinding.inflate(inflater,container,false)
        return binding.root
    }
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
                    binding.backgroundImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), image))
                    val snackbarView = snackbar.view
                    val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    snackbarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    snackbarView.background = ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                    snackbar.show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val mediatorLiveData = MediatorLiveData<Pair<List<LocationDto>, Int>>()
            mediatorLiveData.addSource(mainScreenViewModel.locations) { locations ->
                val locationIndex = mainScreenViewModel.locationIndex.value
                if (locationIndex != null) {
                    mediatorLiveData.value = Pair(locations, locationIndex)
                }
            }

            mediatorLiveData.addSource(mainScreenViewModel.locationIndex) { locationIndex ->
                val locations = mainScreenViewModel.locations.value
                if (locations != null) {
                    mediatorLiveData.value = Pair(locations, locationIndex)
                }
            }
            mediatorLiveData.observe(viewLifecycleOwner) { (locations, locationIndex) ->
                Log.d(TAG, "curr location index in saved fragment $locationIndex")
                Log.d(TAG, "location list size in saved fragment${locations.size}")
                Log.d(TAG, "current index city name in saved fragment ${locations[locationIndex].cityName}")
                val endOfList = locationIndex >= locations.size - 1
                val startOfList = (locationIndex == 0 && locations.isNotEmpty())
//                Log.d(TAG, endOfList.toString())
//                Log.d(TAG, startOfList.toString())

                if (endOfList){
                    binding.nextScreenButton.visibility = View.INVISIBLE

                }
                else {
                    binding.nextScreenButton.visibility = View.VISIBLE
                    binding.nextScreenButton.setOnClickListener {
                       requireActivity().invalidateOptionsMenu()
                        mainScreenViewModel.getSavedLocationWeather()
                        mainScreenViewModel.incrementIndex()
                    }
                }

                if (startOfList){
                    binding.prevScreenButton.visibility =  View.INVISIBLE
                }
                else {
                    binding.prevScreenButton.visibility =  View.VISIBLE
                    binding.prevScreenButton.setOnClickListener {
                        requireActivity().invalidateOptionsMenu()
                        mainScreenViewModel.getSavedLocationWeather()
                        mainScreenViewModel.decrementIndex()
                    }
                }


            }
        }

        lifecycleScope.launch {
            mainScreenViewModel.savedWeather.observe(viewLifecycleOwner) { weatherData ->
                weatherData.weather.currentWeather?.let { currentWeather ->
                    val city = weatherData.location.cityName
                    val weather =
                        weatherData.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                    observeImageData(city, weather!!)
                    binding.mainTemp.text =
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
                        getString(R.string.center_location_text, weatherData.location.cityName, weatherData.location.country)
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
        _binding = null
        mainScreenViewModel.resetIndex()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SavedLocationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}