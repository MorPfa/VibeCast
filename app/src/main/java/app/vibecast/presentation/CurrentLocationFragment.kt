package app.vibecast.presentation

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentCurrentLocationBinding
import app.vibecast.presentation.mainscreen.CurrentLocationViewModel
import app.vibecast.presentation.permissions.PermissionHelper
import app.vibecast.presentation.weather.WeatherModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class CurrentLocationFragment : Fragment() {

    private lateinit var binding : FragmentCurrentLocationBinding
    private lateinit var permissionHelper: PermissionHelper
    private val viewModel: CurrentLocationViewModel by activityViewModels()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        permissionHelper =  PermissionHelper(this)
        val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        val rationale = "We need this permission to provide location-based services."
        val requestCode = 1
        if (!permissionHelper.isPermissionGranted(permission)) {
            permissionHelper.requestPermission(permission, rationale, requestCode)
        }
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



    private fun observeImageData(city: String, weather: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.loadImage(city, weather)
                .collect { imageDto ->
                    imageDto!!.urls.regular.let { imageUrl ->
                        // Switch to Main dispatcher for UI-related operations
                        withContext(Dispatchers.Main) {
                            viewModel.setImageLiveData(imageDto) // Set the LiveData value
                            viewModel.loadImageIntoImageView(
                                imageUrl, binding.backgroundImageView
                            )
                        }
                    }
                }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.weather.observe(viewLifecycleOwner) { weatherData ->
                weatherData.weather.currentWeather?.let { currentWeather ->
                        val city = weatherData.weather.cityName
                        val weather =
                            weatherData.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                        observeImageData(city, weather!!)
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
                        binding.locationDisplay.text = weatherData.weather.cityName
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