package app.vibecast.presentation.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.vibecast.databinding.FragmentSettingsBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
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
        binding.allowLocationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Switch is ON
                // Perform actions for ON state
            } else {
                // Switch is OFF
                // Perform actions for OFF state
            }
        }
        binding.celsiusFahrenheitSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Switch is ON
                // Perform actions for ON state
            } else {
                // Switch is OFF
                // Perform actions for OFF state
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
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