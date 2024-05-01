package app.vibecast.presentation.user.auth

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.vibecast.R
import app.vibecast.databinding.FragmentLogoutBinding
import app.vibecast.presentation.MainActivity
import app.vibecast.presentation.screens.account_screen.AccountViewModel
import app.vibecast.presentation.screens.account_screen.util.ImageHandler
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LogoutFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentLogoutBinding
    private lateinit var logoutBtn: Button
    private lateinit var deleteBtn: Button
    private var deleteDialog: AlertDialog? = null
    private var logoutDialog: AlertDialog? = null
    private lateinit var auth: FirebaseAuth
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLogoutBinding.inflate(inflater, container, false)
        val bgImage = imageViewModel.pickDefaultBackground()
        binding.backgroundImageView.setImageResource(bgImage)
        logoutBtn = binding.logoutBtn
        deleteBtn = binding.deleteBtn
        logoutBtn.setOnClickListener {
            showLogoutDialog()

        }
        deleteBtn.setOnClickListener {
            showDeleteDialog()
        }
        return binding.root
    }


    private fun showLogoutDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.logout_dialog, null)

        val confirmBtn = dialogView.findViewById<Button>(R.id.confirmBtn)
        val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)

        cancelBtn.setOnClickListener {
            logoutDialog?.dismiss()
        }

        confirmBtn.setOnClickListener {
            auth.signOut()
            logoutDialog?.dismiss()
            accountViewModel.deleteLocalData()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        alertDialogBuilder.setView(dialogView)
        logoutDialog = alertDialogBuilder.create()
        logoutDialog?.setCancelable(true)
        logoutDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logoutDialog?.show()
    }

    private fun showDeleteDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.delete_account_dialog, null)

        val confirmBtn = dialogView.findViewById<Button>(R.id.confirmBtn)
        val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)



        cancelBtn.setOnClickListener {
            deleteDialog?.dismiss()
        }

        confirmBtn.setOnClickListener {
            val user = auth.currentUser!!
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.tag("auth").d("Account deleted")

                        accountViewModel.deleteUserData()
                        ImageHandler.deleteImageFromInternalStorage(requireContext())
                        auth.signOut()
                        deleteDialog?.dismiss()
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        val snackBar = Snackbar.make(
                            requireView(),
                            "Something went wrong",
                            Snackbar.LENGTH_SHORT
                        )
                        val snackBarView = snackBar.view
                        val snackBarText =
                            snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        snackBarText.gravity = Gravity.CENTER
                        snackBarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        snackBarView.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                        snackBar.show()
                    }
                }

        }
        alertDialogBuilder.setView(dialogView)
        deleteDialog = alertDialogBuilder.create()
        deleteDialog?.setCancelable(true)
        deleteDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteDialog?.show()

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LogoutFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}