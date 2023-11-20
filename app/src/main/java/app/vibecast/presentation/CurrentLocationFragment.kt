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
import app.vibecast.presentation.weather.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class CurrentLocationFragment : Fragment() {

    private lateinit var binding : FragmentCurrentLocationBinding
    private lateinit var permissionHelper: PermissionHelper

    private val weatherViewModel: WeatherViewModel by viewModels()
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        val rationale = "We need this permission to provide location-based services."
        val requestCode = 1

        if (!permissionHelper.isPermissionGranted(permission)) {
            permissionHelper.requestPermission(permission, rationale, requestCode)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val weatherDto = weatherViewModel.loadWeather("Seattle").first()
            binding.mainTempDisplay.text = weatherDto.currentWeather?.temperature.toString()
            //            Current hour values
            binding.centerTempRow.leftWeather.text =
                weatherDto.currentWeather?.weatherConditions?.get(0)?.mainDescription
            binding.centerTempRow.leftTemp.text = weatherDto.currentWeather?.temperature.toString()
            binding.centerTempRow.leftTime.text = weatherDto.currentWeather?.timestamp.toString()
            //            Next hour values
            binding.centerTempRow.centerWeather.text =
                weatherDto.hourlyWeather?.get(0)?.weatherConditions?.get(0)?.mainDescription
            binding.centerTempRow.centerTemp.text = weatherDto.hourlyWeather?.get(0)?.temperature.toString()
            binding.centerTempRow.centerTime.text = weatherDto.hourlyWeather?.get(0)?.timestamp.toString()
            //            2 hours from now values
            binding.centerTempRow.rightWeather.text =
                weatherDto.hourlyWeather?.get(1)?.weatherConditions?.get(0)?.mainDescription
            binding.centerTempRow.rightTemp.text = weatherDto.hourlyWeather?.get(0)?.temperature.toString()
            binding.centerTempRow.rightTime.text = weatherDto.hourlyWeather?.get(0)?.timestamp.toString()
            binding.locationDisplay.text = weatherDto.cityName
            //TODO add country to city
            binding.mainWeatherWidget.feelsLikeTv.text = weatherDto.currentWeather?.feelsLike.toString()
            binding.mainWeatherWidget.windSpeedTv.text = weatherDto.currentWeather?.windSpeed.toString()
            binding.mainWeatherWidget.visibilityValue.text = weatherDto.currentWeather?.visibility.toString()
            binding.mainWeatherWidget.chanceOfRainTv.text = weatherDto.hourlyWeather?.get(0)?.chanceOfRain.toString()
            binding.mainWeatherWidget.uvIndexTv.text = weatherDto.currentWeather?.uvi.toString()
            binding.mainWeatherWidget.humidtyTv.text = weatherDto.currentWeather?.humidity.toString()
            binding.bottomHumidityDisplay.text = weatherDto.currentWeather?.humidity.toString()
            binding.bottomChanceOfRainDisplay.text =  weatherDto.hourlyWeather?.get(0)?.chanceOfRain.toString()
            binding.bottomUvIndexDisplay.text = weatherDto.currentWeather?.uvi.toString()
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