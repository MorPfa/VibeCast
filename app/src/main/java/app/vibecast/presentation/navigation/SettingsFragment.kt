package app.vibecast.presentation.navigation

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentSettingsBinding
import app.vibecast.presentation.DataStoreViewModel
import app.vibecast.presentation.permissions.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import app.vibecast.data.data_repository.repository.Unit


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"







@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var permissionHelper: PermissionHelper
    private val viewModel : DataStoreViewModel by activityViewModels()

    private val binding get() = _binding!!
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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        permissionHelper = PermissionHelper(requireActivity())



        val isLocationPermissionGranted = permissionHelper.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        binding.allowLocationSwitch.isChecked = isLocationPermissionGranted
        binding.allowLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch is ON / permission has been granted
                permissionHelper.requestPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    "Location permission is required to get weather data for your current location.",
                    LOCATION_PERMISSION_REQUEST_CODE
                )

            } else {
               // Switch is OFF / permission has not been granted
                permissionHelper.openAppSettings()
            }
        }
       return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes in the unit preference and update the UI
        viewModel.getUnit().onEach { unit ->
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
            viewModel.storeUnit(selectedUnit)
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

