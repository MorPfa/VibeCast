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
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import app.vibecast.R
import app.vibecast.databinding.FragmentSettingsBinding
import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.repository.music.WeatherCondition
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.presentation.permissions.PermissionHelper
import app.vibecast.presentation.screens.main_screen.MainViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs


@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var permissionHelper: PermissionHelper
    private val prefViewModel : PreferencesViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private lateinit var imageSelector : ViewPager2


    private val binding get() = _binding!!


    private fun getImagePosition(items: List<ImageDto>, attribute: String?): Int {
        if(attribute == null) return 0

        for ((index, item) in items.withIndex()) {

            if (item.urls.regular == attribute) {
                return index
            }
        }
        return -1
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        permissionHelper = PermissionHelper(requireActivity())
        imageSelector = binding.bgImageSelector
        val imageLoader = ImageLoader(requireContext())


        val adapter = ImageSelectorAdapter(imageLoader,this,  imageViewModel)
        imageSelector.adapter = adapter
        imageSelector.offscreenPageLimit = 3
        imageSelector.clipToPadding = false
        imageSelector.clipChildren = false
        setUpTransformer()



        imageViewModel.galleryImages.observe(viewLifecycleOwner){images ->
            adapter.submitList(images)
            imageSelector.post {
                imageSelector.setCurrentItem(getImagePosition( images,
                    imageViewModel.backgroundImage?.value
                ), false)
            }
        }

        val genres = resources.getStringArray(R.array.music_genres)
        val adapterItems = ArrayAdapter(requireContext(), R.layout.music_preference_item, genres)
        binding.foggyAutoTv.setAdapter(adapterItems)
        binding.rainyAutoTv.setAdapter(adapterItems)
        binding.sunnyAutoTv.setAdapter(adapterItems)
        binding.cloudyAutoTv.setAdapter(adapterItems)
        binding.snowyAutoTv.setAdapter(adapterItems)
        binding.stormyAutoTv.setAdapter(adapterItems)

        val weatherConditions = listOf(
            WeatherCondition.FOGGY,
            WeatherCondition.RAINY,
            WeatherCondition.SUNNY,
            WeatherCondition.SNOWY,
            WeatherCondition.CLOUDY,
            WeatherCondition.STORMY
        )

        lifecycleScope.launch {

            imageViewModel.backgroundImage?.observe(viewLifecycleOwner) { image ->
                if(image != null){
                    imageLoader.loadUrlIntoImageView(
                        image,
                        binding.backgroundImageView,
                        true, 0
                    )
                } else {
                    val bgImage = imageViewModel.pickDefaultBackground()
                    binding.backgroundImageView.setImageResource(bgImage)
                }
            }
        }

        binding.resetBgImageBtn.setOnClickListener {
            imageViewModel.resetBackgroundImage()

        }

        weatherConditions.forEach { condition ->
            prefViewModel.getMusicPreferences().onEach { musicPreferences ->
                    val castedMusicPreferences = musicPreferences as? Map<WeatherCondition, String>
                    val autoCompleteTextView = when (condition) {
                        WeatherCondition.FOGGY -> binding.foggyAutoTv
                        WeatherCondition.RAINY -> binding.rainyAutoTv
                        WeatherCondition.SUNNY -> binding.sunnyAutoTv
                        WeatherCondition.SNOWY -> binding.snowyAutoTv
                        WeatherCondition.CLOUDY -> binding.cloudyAutoTv
                        WeatherCondition.STORMY -> binding.stormyAutoTv
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


    private fun setUpTransformer(){
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        imageSelector.setPageTransformer(transformer)
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
            mainViewModel.checkPermissionState()

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

