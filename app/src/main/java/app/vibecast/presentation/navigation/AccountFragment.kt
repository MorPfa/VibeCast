package app.vibecast.presentation.navigation

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.R
import app.vibecast.databinding.FragmentAccountBinding
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import app.vibecast.presentation.weather.LocationModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val mainScreenViewModel: MainScreenViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageViewModel.setImageCountLiveData()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAccountBinding.inflate(inflater,container,false)
        val savedLocationsRv : RecyclerView = binding.savedLocations
        val adapter = LocationAdapter(requireContext())
        savedLocationsRv.adapter = adapter
        savedLocationsRv.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        savedLocationsRv.layoutManager = layoutManager

        adapter.setOnItemClickListener { position ->
            val clickedItem = adapter.currentList[position]
            showDeleteConfirmationDialog(clickedItem)
        }
        lifecycleScope.launch {
            imageViewModel.imageCount.observe(viewLifecycleOwner){
                binding.savedImageCount.text = getString(R.string.saved_image_count, it)
            }
        }
        mainScreenViewModel.locations.observe(viewLifecycleOwner) { locations ->
            adapter.submitList(locations)
        }

        return binding.root
    }


    private var alertDialog: AlertDialog? = null

    private fun showDeleteConfirmationDialog(location: LocationModel) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete_dialog, null)
        val cardView = customView.findViewById<CircularRevealCardView>(R.id.dialog_card)
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        cardView.radius = 36f
        cardView.clipToOutline = true

        val removeButton = customView.findViewById<MaterialButton>(R.id.removeBtn)
        val cancelButton = customView.findViewById<MaterialButton>(R.id.cancelBtn)


        removeButton.setOnClickListener {
            mainScreenViewModel.deleteLocation(location)
            mainScreenViewModel.resetIndex()
            alertDialog?.dismiss()
        }

        cancelButton.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialogBuilder.setView(customView)
        alertDialog = alertDialogBuilder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mainScreenViewModel.locations.removeObservers(viewLifecycleOwner)
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}