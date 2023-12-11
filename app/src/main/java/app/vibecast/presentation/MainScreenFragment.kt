package app.vibecast.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.vibecast.R
import app.vibecast.databinding.FragmentMainScreenBinding

import app.vibecast.presentation.mainscreen.MainScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class MainScreenFragment : Fragment() {
    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var actionBar: ActionBar
    private val viewModel : MainScreenViewModel by activityViewModels()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)


        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainScreenBinding.inflate(inflater,container,false)
        actionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        actionBar.show()
        val nextScreenButton = binding.nextScreenButton
        nextScreenButton.setOnClickListener {
            if (viewModel.locations.value?.size != 0){
                viewModel.getSavedLocationWeather()
                val action = MainScreenFragmentDirections.actionNavHomeToSavedLocationFragment()
                findNavController().navigate(action)
            }
            else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.none_saved_warning),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                    showToast("Error loading image")
                }
            }
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.currentWeather.observe(viewLifecycleOwner) { weatherData ->
                    weatherData.weather.currentWeather?.let { currentWeather ->
                        val city = weatherData.location.cityName
                        val weather =
                            weatherData.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                        observeImageData(city, weather!!)
                        Log.d(TAG, city)
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
        viewModel.currentWeather.removeObservers(viewLifecycleOwner)
        _binding = null
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
            MainScreenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}