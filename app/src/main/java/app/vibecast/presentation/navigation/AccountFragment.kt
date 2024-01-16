package app.vibecast.presentation.navigation

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentAccountBinding
import app.vibecast.domain.entity.LocationDto
import app.vibecast.presentation.TAG
import app.vibecast.presentation.mainscreen.MainScreenViewModel
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

    private var locationList : LinearLayout? = null
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
        locationList = binding.savedLocations

        lifecycleScope.launch {
            imageViewModel.imageCount.observe(viewLifecycleOwner){
                binding.savedImageCount.text = getString(R.string.saved_image_count, it)
            }
        }

        loadLocations()

        return binding.root
    }


    private fun loadLocations() {
        mainScreenViewModel.locations.observe(viewLifecycleOwner) { locations ->
            Log.d(TAG, "${locations.size} in Acc fragment")
            Log.d(TAG, "${mainScreenViewModel.locationIndex}")
            val currentItemCount = locationList?.childCount
            val max = locations.size
            if (max == 0 && currentItemCount == 0) {
                val item = createDefaultTv()
                locationList?.addView(item)
            } else {
                if (currentItemCount != null) {
                    for (i in currentItemCount until max) {
                        val item = createItemView(i, locations)
                        locationList?.addView(item)
                    }
                }
            }
        }
    }
    private fun createItemView(index: Int, locations: List<LocationDto>): View {
        val item = TextView(requireContext())
        val formattedLocation = locations.getOrNull(index)?.cityName
                                .plus(" - ")
                                .plus(locations.getOrNull(index)?.country)
        item.text = formattedLocation
        item.setTextColor(Color.WHITE)
        item.setBackgroundResource(R.drawable.rounded_black_background)
        item.gravity = Gravity.START
        item.textSize = 20f
        val paddingInDpVertical = 8
        val paddingInPxVertical = (paddingInDpVertical * resources.displayMetrics.density).toInt()
        val paddingInDpLeft = 12
        val paddingInPxLeft = (paddingInDpLeft * resources.displayMetrics.density).toInt()
        val paddingInDpRight = 12
        val paddingInPxRight = (paddingInDpRight * resources.displayMetrics.density).toInt()
        item.setPadding(paddingInPxLeft, paddingInPxVertical, paddingInPxRight, paddingInPxVertical)
        val marginInDp = 2
        val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
        item.layoutParams = layoutParams
        item.setOnLongClickListener {
            showDeleteConfirmationDialog(index, locations[index])
            true
        }

        return item
    }
    private var alertDialog: AlertDialog? = null

    private fun showDeleteConfirmationDialog(index: Int, location: LocationDto) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete_dialog, null)
        val cardView = customView.findViewById<CircularRevealCardView>(R.id.dialog_card)
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        cardView.radius = 36f
        cardView.clipToOutline = true

        val removeButton = customView.findViewById<MaterialButton>(R.id.removeBtn)
        val cancelButton = customView.findViewById<MaterialButton>(R.id.cancelBtn)


        removeButton.setOnClickListener {

            removeFixedItem(index, location)
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



    private fun removeFixedItem(index: Int, location: LocationDto) {
        if (index >= 0 && index < locationList!!.childCount) {
            locationList?.removeViewAt(index)
            mainScreenViewModel.deleteLocation(location)

        }
    }

    private fun createDefaultTv(): View {
        val item = TextView(requireContext())
        item.text = getString(R.string.none_saved)
        item.setTextColor(Color.WHITE)
        item.textSize = 18.0f
        item.gravity = 1
        return item
    }


    override fun onDestroyView() {
        super.onDestroyView()
        locationList = null
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