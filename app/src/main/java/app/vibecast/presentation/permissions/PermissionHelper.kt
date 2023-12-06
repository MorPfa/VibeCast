package app.vibecast.presentation.permissions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionHelper(private val activity: Activity) {


    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }


    fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showRationaleDialog(rationale) { _, _ ->
                ActivityCompat.requestPermissions(
                   activity,
                    arrayOf(permission),
                    requestCode
                )
            }
        } else {
            ActivityCompat.requestPermissions(
               activity,
                arrayOf(permission),
                requestCode
            )
        }
    }


    private fun showRationaleDialog(message: String, onPositiveClick: (DialogInterface, Int) -> Unit) {

        AlertDialog.Builder(activity)
            .setTitle("Location permission Required")
            .setMessage(message)
            .setPositiveButton("OK", onPositiveClick)
            .setNegativeButton("Cancel", null)
            .show()
    }
}
