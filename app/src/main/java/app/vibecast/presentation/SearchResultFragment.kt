package app.vibecast.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.vibecast.R
import app.vibecast.databinding.FragmentSearchResultBinding
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class SearchResultFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding : FragmentSearchResultBinding
    private val viewModel : MainScreenViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.loadCurrentLocationWeather()
            findNavController().navigate(R.id.nav_home)
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun observeImageData(city: String, weather: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                viewModel.loadImage(city, weather)
                    .collect { imageDto ->
                        imageDto!!.urls.regular.let { imageUrl ->
                            withContext(Dispatchers.Main) {
                                viewModel.setImageLiveData(imageDto)
                                viewModel.loadImageIntoImageView(
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
                    val image = viewModel.pickDefaultImage(weather)
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
        viewModel.currentWeather.observe(viewLifecycleOwner) { weatherData ->
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}