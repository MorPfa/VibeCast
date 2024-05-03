package app.vibecast.presentation.screens.main_screen.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.vibecast.databinding.FragmentWebBinding
import app.vibecast.presentation.screens.main_screen.music.util.InfoType


private const val TYPE_PARAM = "infoType"


class WebFragment : Fragment() {
    private var type: InfoType? = null
    private var _binding: FragmentWebBinding? = null
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
        musicViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (type == InfoType.SONG) {
                webView.loadUrl(song.externalUrls.spotify, headers)
            } else {
                webView.loadUrl(song.artists[0].externalUrls.spotify, headers)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        musicViewModel.currentSong.removeObservers(viewLifecycleOwner)
        _binding = null
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