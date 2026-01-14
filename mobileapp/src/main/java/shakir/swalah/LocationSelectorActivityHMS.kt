package shakir.swalah

import android.location.Location
import android.os.Looper
import com.huawei.hms.location.FusedLocationProviderClient
import com.huawei.hms.location.LocationCallback
import com.huawei.hms.location.LocationRequest
import com.huawei.hms.location.LocationResult
import com.huawei.hms.location.LocationServices


abstract class LocationSelectorActivityHMS : BaseActivity() {

   var  function: (Location) -> Unit={

   }

    fun getHMSLocation() {

        try {
            var mLocationCallback: LocationCallback? = null
            val mLocationRequest: LocationRequest
            val mFusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            mLocationRequest = LocationRequest()
            mLocationRequest.setInterval(6000)
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            mLocationRequest.setNumUpdates(100)
            if (null == mLocationCallback) {
                // Set the location callback.
                mLocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        try {
                            if (locationResult != null) {
                                val locations: List<Location> = locationResult.getLocations()
                                function(locations[0])
                                //one time call
                                function={}
                                locations.forEach {
                                    println("locations ${it.latitude} ${it.longitude} ${it.altitude}")
                                }
                            }
                        } catch (e: Exception) {
                          e.printStackTrace()
                        }
                    }
                }
            }


            mFusedLocationProviderClient
                .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
           e.printStackTrace()
        }

    }
}