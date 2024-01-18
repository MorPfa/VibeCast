package app.vibecast.presentation


import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.vibecast.R
import app.vibecast.databinding.FragmentLoadingBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoadingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!
    private lateinit var actionBar: ActionBar

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
        actionBar.hide()


        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                navigateToNextScreen()
            }
        }, 2200)

    }
    private fun navigateToNextScreen() {
        if (isAdded) {
            findNavController().navigate(R.id.action_splashFragment_to_nav_home)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoadingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}