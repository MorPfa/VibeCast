package app.vibecast.presentation

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentCurrentLocationBinding
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.presentation.weather.CurrentLocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class CurrentLocationFragment : Fragment() {

    private lateinit var binding : FragmentCurrentLocationBinding
    private lateinit var permissionHelper: PermissionHelper

    private val currentLocationViewModel: CurrentLocationViewModel by viewModels()
    private var actionBarItemClickListener: OnActionBarItemClickListener? = null

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        permissionHelper =  PermissionHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrentLocationBinding.inflate(inflater,container,false)
        val nextScreenButton = binding.nextScreenButtonRight
        nextScreenButton.setOnClickListener {
            binding.constraintLayout.setBackgroundResource(R.drawable.pexels_karl_solano_2884590)
        }
        return binding.root
    }


    private fun observeImageData(city : String, weather : String) {
        viewLifecycleOwner.lifecycleScope.launch {
            currentLocationViewModel.loadImage(city, weather)
                .flowOn(Dispatchers.IO)
                .collect { imageDto ->
                imageDto?.urls?.regular?.let { imageUrl ->
                    // Switch to Main dispatcher for UI-related operations
                    withContext(Dispatchers.Main) {
                        currentLocationViewModel.loadImageIntoImageView(imageUrl, binding.backgroundImageView)
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        val rationale = "We need this permission to provide location-based services."
        val requestCode = 1

        if (!permissionHelper.isPermissionGranted(permission)) {
            permissionHelper.requestPermission(permission, rationale, requestCode)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val weatherData = currentLocationViewModel.loadWeather("Seattle").first()
            val city = weatherData.cityName
            val weather = weatherData.currentWeather?.weatherConditions?.get(0)?.mainDescription
            observeImageData(city, weather!!)
            binding.mainTempDisplay.text =
                getString(R.string.center_temp, weatherData.currentWeather.temperature)
            //            Current hour values
            binding.centerTempRow.leftWeather.text =
                weatherData.currentWeather.weatherConditions.get(0).mainDescription
            binding.centerTempRow.leftTemp.text = weatherData.currentWeather.temperature.toString()
            binding.centerTempRow.leftTime.text = weatherData.hourlyWeather?.get(0)?.timestamp
            //            Next hour values
            binding.centerTempRow.centerWeather.text =
                weatherData.hourlyWeather?.get(1)?.weatherConditions?.get(0)?.mainDescription
            binding.centerTempRow.centerTemp.text = weatherData.hourlyWeather?.get(1)?.temperature.toString()
            binding.centerTempRow.centerTime.text = weatherData.hourlyWeather?.get(1)?.timestamp.toString()
            //            2 hours from now values
            binding.centerTempRow.rightWeather.text =
                weatherData.hourlyWeather?.get(2)?.weatherConditions?.get(0)?.mainDescription
            binding.centerTempRow.rightTemp.text = weatherData.hourlyWeather?.get(2)?.temperature.toString()
            binding.centerTempRow.rightTime.text = weatherData.hourlyWeather?.get(2)?.timestamp.toString()
            binding.locationDisplay.text = weatherData.cityName
            binding.mainWeatherWidget.feelsLikeTv.text =
                getString(R.string.feels_like_value, weatherData.currentWeather.feelsLike)
            binding.mainWeatherWidget.windSpeedTv.text =
                getString(R.string.wind_speed_value, weatherData.currentWeather.windSpeed)
            binding.mainWeatherWidget.visibilityValue.text = weatherData.currentWeather.visibility
            binding.mainWeatherWidget.chanceOfRainTv.text =
                getString(R.string.chance_of_rain_value,weatherData.hourlyWeather?.get(0)?.chanceOfRain)
            binding.mainWeatherWidget.uvIndexTv.text = weatherData.currentWeather.uvi.toString()
            binding.mainWeatherWidget.humidtyTv.text = getString(R.string.humidity,
                weatherData.currentWeather.humidity
            )
            binding.bottomHumidityDisplay.text = getString(R.string.humidity,
                weatherData.currentWeather.humidity
            )
            binding.bottomChanceOfRainDisplay.text =
                getString(R.string.chance_of_rain_value,weatherData.hourlyWeather?.get(0)?.chanceOfRain)
            binding.bottomUvIndexDisplay.text = weatherData.currentWeather.uvi.toString()
        }
    }


    // Call this method when you need to trigger an action from the activity
    private fun performSaveImage(image : ImageDto) {
        actionBarItemClickListener?.onSaveImageClicked(image)
    }

    // Call this method when you need to trigger another action from the activity
    private fun performSaveLocation(location : LocationWithWeatherDataDto) {
        actionBarItemClickListener?.onSaveLocationClicked(location)
    }

    interface OnActionBarItemClickListener {

        fun onSearch(query: String)
        fun onSaveImageClicked(image : ImageDto)
        fun onSaveLocationClicked(location : LocationWithWeatherDataDto)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CurrentLocationFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CurrentLocationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}