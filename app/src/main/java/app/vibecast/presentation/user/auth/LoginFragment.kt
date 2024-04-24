package app.vibecast.presentation.user.auth

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.vibecast.R
import app.vibecast.databinding.FragmentLoginBinding
import app.vibecast.presentation.MainActivity
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.user.auth.util.LoginResult
import com.google.android.gms.common.SignInButton
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoginFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signInBtn: Button
    private lateinit var noAccountBtn: TextView
    private lateinit var forgotBtn: TextView
    private lateinit var googleSignInBtn: SignInButton
    private var forgotPasswordDialog: AlertDialog? = null
    private val imageViewModel: ImageViewModel by activityViewModels()


    private var listener: OnGoogleSignInClickListener? = null


    interface OnGoogleSignInClickListener {
        fun onGoogleSignInClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnGoogleSignInClickListener) {
            listener = context
        }
    }

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
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        emailInput = binding.emailInput
        passwordInput = binding.passwordInput
        signInBtn = binding.signInBtn
        noAccountBtn = binding.noAccount
        forgotBtn = binding.forgotPassword
        googleSignInBtn = binding.signInWithGoogle
        val bgImage = imageViewModel.pickDefaultBackground()
        binding.backgroundImageView.setImageResource(bgImage)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googleSignInBtn.setOnClickListener {
            listener?.onGoogleSignInClick()

        }
        noAccountBtn.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegistrationFragment()
            findNavController().navigate(action)
        }

        forgotBtn.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            val dialog =
                LayoutInflater.from(requireContext()).inflate(R.layout.forgot_password_dialog, null)
            val userEmail = dialog.findViewById<EditText>(R.id.editBox)
            val cancelBtn = dialog.findViewById<MaterialButton>(R.id.btnCancel)
            val resetBtn = dialog.findViewById<MaterialButton>(R.id.btnReset)

            resetBtn.setOnClickListener {
                compareEmail(userEmail)
                forgotPasswordDialog?.dismiss()
            }

            cancelBtn.setOnClickListener {
                forgotPasswordDialog?.dismiss()
            }

            alertDialogBuilder.setView(dialog)
            forgotPasswordDialog = alertDialogBuilder.create()
            forgotPasswordDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            forgotPasswordDialog?.show()
        }

        signInBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val result = validateInput(email, password)
            when (result) {
                LoginResult.INVALID_EMAIL -> {
                    Toast.makeText(
                        requireContext(),
                        "Invalid email", Toast.LENGTH_SHORT
                    ).show()
                }

                LoginResult.NO_PASSWORD -> {
                    Toast.makeText(
                        requireContext(),
                        "Please enter your password", Toast.LENGTH_SHORT
                    ).show()
                }

                LoginResult.SUCCESS -> {
                    signInWithEmail(email, password)
                }
            }
        }
    }


    private fun compareEmail(email: EditText) {
        if (email.text.toString().isEmpty()) {
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            return
        }
        auth.sendPasswordResetEmail(email.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Check your email", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Timber.tag("auth").d("signInWithEmail:success")
                    Toast.makeText(
                        requireContext(),
                        "Signed in successfully", Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().finish()

                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    Timber.tag("auth").w("signInWithEmail:failure ${task.exception}")
                    try {
                        throw task.exception!!
                    } catch (invalidCredentials: FirebaseAuthInvalidCredentialsException) {
                        // Handle incorrect password
                        Toast.makeText(
                            requireContext(),
                            "Email and password do not match",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        // Handle other exceptions
                        Toast.makeText(
                            requireContext(),
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }


    private fun validateInput(email: String, password: String): LoginResult {
        if (!isEmailValid(email)) {
            return LoginResult.INVALID_EMAIL
        }
        return if (!isPasswordEmpty(password)) {
            LoginResult.NO_PASSWORD
        } else {
            LoginResult.SUCCESS
        }
    }


    private fun isEmailValid(email: String): Boolean {
        if (email.isEmpty()) {
//            passwordInput.error = "Test"
            //TODO customize this
            return false
        }
        val validEmail = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return validEmail.matches(email)
    }

    private fun isPasswordEmpty(password: String): Boolean {
        return password.isNotEmpty()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}