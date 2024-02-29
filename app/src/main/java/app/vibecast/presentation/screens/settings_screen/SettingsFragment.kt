package app.vibecast.presentation.screens.settings_screen

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentSettingsBinding
import app.vibecast.domain.repository.implementation.Unit
import app.vibecast.domain.repository.implementation.WeatherCondition
import app.vibecast.presentation.permissions.PermissionHelper
import app.vibecast.presentation.screens.main_screen.MainScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var permissionHelper: PermissionHelper
    private val prefViewModel : PreferencesViewModel by activityViewModels()
    private val mainScreenViewModel: MainScreenViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        permissionHelper = PermissionHelper(requireActivity())
        val genres = resources.getStringArray(R.array.music_genres)
        val adapterItems = ArrayAdapter(requireContext(), R.layout.music_preference_item, genres)
        binding.foggyAutoTv.setAdapter(adapterItems)
        binding.rainyAutoTv.setAdapter(adapterItems)
        binding.sunnyAutoTv.setAdapter(adapterItems)
        binding.cloudyAutoTv.setAdapter(adapterItems)
        binding.snowyAutoTv.setAdapter(adapterItems)

        val weatherConditions = listOf(
            WeatherCondition.FOGGY,
            WeatherCondition.RAINY,
            WeatherCondition.SUNNY,
            WeatherCondition.SNOWY,
            WeatherCondition.CLOUDY
        )

        weatherConditions.forEach { condition ->
            prefViewModel.getMusicPreferences().onEach { musicPreferences ->
                    val castedMusicPreferences = musicPreferences as? Map<WeatherCondition, String>
                    val autoCompleteTextView = when (condition) {
                        WeatherCondition.FOGGY -> binding.foggyAutoTv
                        WeatherCondition.RAINY -> binding.rainyAutoTv
                        WeatherCondition.SUNNY -> binding.sunnyAutoTv
                        WeatherCondition.SNOWY -> binding.snowyAutoTv
                        WeatherCondition.CLOUDY -> binding.cloudyAutoTv
                    }

                autoCompleteTextView.setAdapter(adapterItems)
                val savedPreference = castedMusicPreferences?.get(condition)
                if (savedPreference != null) {
                    autoCompleteTextView.hint = savedPreference
                } else {
                    autoCompleteTextView.hint = ""
                }


                autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                        val selectedItem = adapterItems.getItem(position)
                        castedMusicPreferences?.let {
                            val musicPreference = mapOf(condition to selectedItem!!)
                            prefViewModel.savePreferences( musicPreference)
                        }
                    }
                autoCompleteTextView.post { autoCompleteTextView.performCompletion() }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }

        val isLocationPermissionGranted = permissionHelper.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        binding.allowLocationSwitch.isChecked = isLocationPermissionGranted
        binding.allowLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch is ON / permission has been granted
                permissionHelper.requestPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    LOCATION_PERMISSION_REQUEST_CODE
                )

            } else {
               // Switch is OFF / permission has not been granted
                permissionHelper.openAppSettings()
            }
        }
        binding.resetMusicPrefBtn.setOnClickListener {
            prefViewModel.clearPreferences(UserPreferences.MUSIC)
        }
       return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefViewModel.getUnitPreferences().onEach { unit ->
            unit?.let {
                when (it) {
                    Unit.IMPERIAL -> binding.radioImperial.isChecked = true
                    Unit.METRIC -> binding.radioMetric.isChecked = true
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.weatherUnitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedUnit = when (checkedId) {
                R.id.radioImperial -> Unit.IMPERIAL
                R.id.radioMetric -> Unit.METRIC
                else -> Unit.IMPERIAL // Default to imperial if none is selected
            }
            prefViewModel.savePreferences(selectedUnit)
            mainScreenViewModel.checkPermissionState()

        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

