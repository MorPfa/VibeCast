package app.vibecast.presentation.screens.main_screen.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.vibecast.databinding.FragmentWebBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class WebFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding : FragmentWebBinding
    private val musicViewModel: MusicViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWebBinding.inflate(inflater, container, false)
        val webView = binding.webView
        val url = musicViewModel.currentPlaylist.value!!
        val headers = HashMap<String, String>().apply {
            put("Authorization", "Bearer ${musicViewModel.token.value}")
        }
        webView.loadUrl(url, headers)
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WebFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}