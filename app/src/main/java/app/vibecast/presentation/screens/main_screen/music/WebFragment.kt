package app.vibecast.presentation.screens.main_screen.music

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.vibecast.R
import app.vibecast.databinding.FragmentWebBinding
import app.vibecast.presentation.screens.main_screen.music.util.InfoType
import com.google.android.material.snackbar.Snackbar


private const val TYPE_PARAM = "infoType"


class WebFragment : Fragment() {
    private var type: InfoType? = null
    private var _binding: FragmentWebBinding? = null
    private lateinit var snackBar: Snackbar
    private val binding get() = _binding!!
    private val musicViewModel: MusicViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getSerializable(TYPE_PARAM) as? InfoType
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        val webView = binding.webView
        val headers = HashMap<String, String>().apply {
            put("Authorization", "Bearer ${musicViewModel.token.value}")
        }
        musicViewModel.currentSong.observe(viewLifecycleOwner) { songState ->
            if(songState.song != null){
                if (type == InfoType.SONG) {
                    webView.loadUrl(songState.song.externalUrls.spotify, headers)
                } else {
                    webView.loadUrl(songState.song.artists[0].externalUrls.spotify, headers)
                }
            }
            else {
                snackBar = Snackbar.make(
                    requireView(),
                    songState.error!!,
                    Snackbar.LENGTH_SHORT
                )
                val snackBarView = snackBar.view
                val snackBarText =
                    snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackBarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                snackBarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                snackBar.show()
                Handler(Looper.getMainLooper()).postDelayed({ snackBar.dismiss() }, 4000)
            }

        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        musicViewModel.currentSong.removeObservers(viewLifecycleOwner)
        _binding = null
        if (::snackBar.isInitialized) {
            snackBar.dismiss()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(type: InfoType) =
            WebFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TYPE_PARAM, type)

                }
            }
    }
}