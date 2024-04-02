package app.vibecast.presentation.user.auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import app.vibecast.R
import app.vibecast.databinding.FragmentLogoutBinding
import app.vibecast.presentation.MainActivity
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
    private lateinit var logoutBtn : Button
    private lateinit var deleteBtn : Button
    private lateinit var auth : FirebaseAuth
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
        logoutBtn = binding.logoutBtn
        deleteBtn = binding.deleteBtn
        logoutBtn.setOnClickListener{
            showLogoutDialog()

        }
        deleteBtn.setOnClickListener{
            showDeleteDialog()
        }
        return binding.root
    }


    private fun showLogoutDialog() {

        val dialogView: View = getLayoutInflater().inflate(R.layout.logout_dialog, null)

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)

        val confirmBtn = dialog.findViewById<Button>(R.id.confirmBtn)
        val cancelBtn = dialog.findViewById<Button>(R.id.cancelBtn)


        cancelBtn.setOnClickListener { // Dismiss the dialog when the button is clicked
            dialog.dismiss()
        }

        confirmBtn.setOnClickListener{
            auth.signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
        dialog.show()
    }

    private fun showDeleteDialog() {

        val dialogView: View = getLayoutInflater().inflate(R.layout.delete_account_dialog, null)

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)

        val confirmBtn = dialog.findViewById<Button>(R.id.confirmBtn)
        val cancelBtn = dialog.findViewById<Button>(R.id.cancelBtn)


        cancelBtn.setOnClickListener { // Dismiss the dialog when the button is clicked
            dialog.dismiss()
        }

        confirmBtn.setOnClickListener{
            val user = auth.currentUser!!
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                       Timber.tag("auth").d("Account deleted")
                    }
                }
            auth.signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
        dialog.show()
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