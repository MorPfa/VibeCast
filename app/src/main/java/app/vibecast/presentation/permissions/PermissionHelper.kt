package app.vibecast.presentation.permissions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    companion object {
        private const val REVOKE_PERMISSION_REQUEST_CODE = 999
    }
}
