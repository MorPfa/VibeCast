package app.vibecast.presentation.screens.account_screen

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.R
import app.vibecast.databinding.FragmentMusicBinding
import app.vibecast.databinding.FragmentMusicShuffleBinding
import app.vibecast.domain.model.SongDto
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel
import app.vibecast.presentation.screens.main_screen.music.util.InfoType
import app.vibecast.presentation.util.PlaylistAdapter
import com.google.android.material.button.MaterialButton
import com.spotify.protocol.types.Image
import kotlinx.coroutines.launch

private const val CAN_PLAY_ON_DEMAND = "canPlayOnDemand"

class MusicFragment : Fragment() {
    private var canPlayOnDemand: Boolean? = null
    private var alertDialog: AlertDialog? = null
    private var _playOnDemandBinding: FragmentMusicBinding? = null
    private val playOnDemandBinding get() = _playOnDemandBinding!!
    private var _shuffleBinding: FragmentMusicShuffleBinding? = null
    private val shuffleBinding get() = _shuffleBinding!!
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            canPlayOnDemand = it.getBoolean(CAN_PLAY_ON_DEMAND)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val imageLoader = ImageLoader(requireContext())
        return if (canPlayOnDemand == false) {
            _playOnDemandBinding = FragmentMusicBinding.inflate(inflater, container, false)
            playOnDemandBinding.playBtn.setOnClickListener {
                musicViewModel.onPlayPauseButtonClicked()
            }
            val savedSongsRv: RecyclerView = playOnDemandBinding.nonShuffleView
            val playlistAdapter = PlaylistAdapter(imageLoader, musicViewModel)
            savedSongsRv.adapter = playlistAdapter
            savedSongsRv.setHasFixedSize(true)
            lifecycleScope.launch {
                imageViewModel.backgroundImage.observe(viewLifecycleOwner) { image ->
                    if (image != null) {
                        imageLoader.loadUrlIntoImageView(
                            image,
                            playOnDemandBinding.backgroundImageView,
                            true, 0
                        )
                    } else {
                        val bgImage = imageViewModel.pickDefaultBackground()
                        playOnDemandBinding.backgroundImageView.setImageResource(bgImage)
                    }

                }

                musicViewModel.curPlaylistState.observe(viewLifecycleOwner) { playlistState ->
                    if (playlistState.error != null) {
                        Toast.makeText(
                            requireContext(),
                            "Couldnt get playlist details",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val playlist = playlistState.data
                        musicViewModel.setupSelectedPlaylist(playlist!!)
                        playOnDemandBinding.songInfoHeader.text = playlist.name
                        val items = playlist.tracks.items
                        playlistAdapter.submitList(items)
                    }
                }
                musicViewModel.playerState.observe(viewLifecycleOwner) { playerState ->
                    musicViewModel.spotifyAppRemote?.let {
                        it.imagesApi
                            .getImage(playerState.state.track.imageUri, Image.Dimension.X_SMALL)
                            .setResultCallback { bitmap ->
                                playOnDemandBinding.coverArt.setImageBitmap(bitmap)
                            }
                    }

                }
            }
            playlistAdapter.setOnItemClickListener { position ->
                val clickedItem = playlistAdapter.currentList[position]
               //TODO show info to allow deletion and opening in spotify app

            }
            playOnDemandBinding.root
        } else {
            _shuffleBinding = FragmentMusicShuffleBinding.inflate(inflater, container, false)
            shuffleBinding.playBtn.setOnClickListener {
                musicViewModel.onPlayPauseButtonClicked()
            }
            lifecycleScope.launch {
                imageViewModel.backgroundImage.observe(viewLifecycleOwner) { image ->
                    if (image != null) {
                        imageLoader.loadUrlIntoImageView(
                            image,
                            shuffleBinding.backgroundImageView,
                            true, 0
                        )
                    } else {
                        val bgImage = imageViewModel.pickDefaultBackground()
                        shuffleBinding.backgroundImageView.setImageResource(bgImage)
                    }

                }
                musicViewModel.curPlaylistState.observe(viewLifecycleOwner) { playlistState ->
                    if (playlistState.error != null) {
                        Toast.makeText(
                            requireContext(),
                            "Couldnt get playlist details",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val playlist = playlistState.data
                        musicViewModel.setupSelectedPlaylist(playlist!!)
                        shuffleBinding.songInfoHeader.text = playlist.name
//                        shuffleBinding.shuffleView.text = formatPlaylistInfo(playlist.tracks.items)
                    }
                }
                musicViewModel.playerState.observe(viewLifecycleOwner) { playerState ->
                    musicViewModel.spotifyAppRemote?.let {
                        it.imagesApi
                            .getImage(playerState.state.track.imageUri, Image.Dimension.X_SMALL)
                            .setResultCallback { bitmap ->
                                shuffleBinding.coverArt.setImageBitmap(bitmap)
                            }
                    }

                }
            }
            shuffleBinding.root
        }
    }

    private fun formatPlaylistInfo(songInfo: List<SongDto>): String {
        var result = ""
        if (songInfo.size > 5) {
            result += "${songInfo[0].name} ${songInfo[0].artist}"
            for (i in 1..4) {
                result += "  -  ${songInfo[i].name} ${songInfo[i].artist}"
            }
            result += " - and more"
        } else {
            songInfo.forEach { song ->
                result += "${song.name} ${song.artist} -"
            }
        }


        return result
    }

    private fun showSongCard(song: SongDto) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.music_card, null)
        val removeButton = customView.findViewById<MaterialButton>(R.id.removeBtn)
        val songUriText = customView.findViewById<TextView>(R.id.song_link_tv)
        val artistUriText = customView.findViewById<TextView>(R.id.artist_link_tv)
        val coverArt = customView.findViewById<ImageView>(R.id.covertArt)


        musicViewModel.assertAppRemoteConnected()
            .imagesApi
            .getImage(song.imageUri, Image.Dimension.LARGE)
            .setResultCallback { bitmap ->
                coverArt.setImageBitmap(bitmap)
            }

        songUriText.setOnClickListener {
            showSongInfoInSpotify(song)
            alertDialog?.dismiss()
        }

        artistUriText.setOnClickListener {
            showArtistInfoInSpotify(song)
            alertDialog?.dismiss()

        }

        removeButton.setOnClickListener {
            musicViewModel.deleteSong(song)
            alertDialog?.dismiss()
        }

        alertDialogBuilder.setView(customView)
        alertDialog = alertDialogBuilder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()
    }
    private fun showArtistInfoInSpotify(song: SongDto) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.artistUri))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val packageManager = context?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            // Open the Spotify app
            val action = AccountFragmentDirections.accountToWeb(InfoType.ARTIST)
            findNavController().navigate(action)
//            context?.startActivity(intent)
        } else {
//            // If Spotify app is not installed, open the Spotify web page instead

        }
    }
    private fun showSongInfoInSpotify(song: SongDto) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.trackUri))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val packageManager = context?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            // Open the Spotify app
            musicViewModel.getCurrentSong(song.name, song.artist)
            val action = AccountFragmentDirections.accountToWeb(InfoType.SONG)
            findNavController().navigate(action)
//            context?.startActivity(intent)
        } else {
//            // If Spotify app is not installed, open the Spotify web page instead

        }
    }
    companion object {
        @JvmStatic
        fun newInstance(canPlayOnDemand: Boolean) =
            MusicFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(CAN_PLAY_ON_DEMAND, canPlayOnDemand)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _playOnDemandBinding = null
        _shuffleBinding = null
        alertDialog = null
    }
}