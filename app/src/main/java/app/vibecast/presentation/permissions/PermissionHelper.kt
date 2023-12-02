package app.vibecast.presentation.permissions

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionHelper(private val fragment: Fragment) {

    // Function to check if a permission is granted
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(fragment.requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request a permission
    fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.requireActivity(), permission)) {
            // Show a permission rationale if the user has previously denied the permission
            showRationaleDialog(rationale) { _, _ ->
                ActivityCompat.requestPermissions(
                    fragment.requireActivity(),
                    arrayOf(permission),
                    requestCode
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                fragment.requireActivity(),
                arrayOf(permission),
                requestCode
            )
        }
    }

    // Function to show a rationale dialog
    private fun showRationaleDialog(message: String, onPositiveClick: (DialogInterface, Int) -> Unit) {
        val context: Context = fragment.requireContext()
        AlertDialog.Builder(context)
            .setTitle("Location permission Required")
            .setMessage(message)
            .setPositiveButton("OK", onPositiveClick)
            .setNegativeButton("Cancel", null)
            .show()
    }
}
