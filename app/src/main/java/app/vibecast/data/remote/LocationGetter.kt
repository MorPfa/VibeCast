package app.vibecast.data.remote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationGetter@Inject constructor(
   @ApplicationContext private val context: Context) {

     val client = LocationServices.getFusedLocationProviderClient(context)

    fun isPermissionGranted() : Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }
}