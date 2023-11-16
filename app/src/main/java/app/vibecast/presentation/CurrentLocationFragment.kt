package app.vibecast.presentation

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.vibecast.R
import app.vibecast.databinding.FragmentCurrentLocationBinding
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CurrentLocationFragment : Fragment() {

    private lateinit var binding : FragmentCurrentLocationBinding
    private lateinit var permissionHelper: PermissionHelper



    private var actionBarItemClickListener: OnActionBarItemClickListener? = null
    // TODO: Rename and change types of parameters
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

        //TODO decide on whether to use one combined viewmodel to load all data or or separate viewmodels
        binding = FragmentCurrentLocationBinding.inflate(inflater,container,false)
        val nextScreenButton = binding.nextScreenButtonRight
        nextScreenButton.setOnClickListener {
            // Call a function to update the fragment with new values

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
        // TODO: Rename and change types and number of parameters
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