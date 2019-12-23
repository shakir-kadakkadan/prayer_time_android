package shakir.swalah

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import shakir.swalah.db.GeoCoded
import java.util.*
import kotlin.concurrent.thread

abstract class MainActivityLocation : BaseActivity() {


    fun startLocationServiceInitialisation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }


    public fun hasLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }


    public fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (hasLocationPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        onLocationServiceResult(location)
                    }
                }
            } else {
                val builder =
                    LocationSettingsRequest.Builder().addLocationRequest(requestNewLocationData())
                builder.setAlwaysShow(true)
                val result =
                    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                result.addOnSuccessListener {
                    Log.d("hgdhag", "addOnSuccessListener ${it.locationSettingsStates}")
                }
                result.addOnFailureListener {
                    it.printStackTrace()
                    Log.d("hgdhag", "addOnFailureListener ${it.message}")
                    if (it is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            it.startResolutionForResult(this, 1)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                            sendEx.printStackTrace()
                        }

                    }
                }


            }
        } else {
            requestPermissions()
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(): LocationRequest {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )

        return mLocationRequest
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            onLocationServiceResult(mLastLocation)
        }
    }


    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient


    private fun gotoLocationSettings() {
        Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }


    fun onLocationServiceResult(location: Location) {
        thread {
            val latitude = location.latitude
            val longitude = location.longitude
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = try {
                geocoder.getFromLocation(latitude, longitude, 1)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            var locality = addresses?.getOrNull(0)?.locality ?: ""
            var subLocality = addresses?.getOrNull(0)?.subLocality ?: ""
            var countryName = addresses?.getOrNull(0)?.countryName ?: ""
            var isNear = false

            try {
                val bundle = Bundle()
                bundle.putString("LocationType", "LocationService")
                bundle.putDouble("Latitude", latitude)
                bundle.putDouble("Longitude", longitude)
                bundle.putString("Locality", locality)
                bundle.putString("SubLocality", subLocality)
                bundle.putString("Country", countryName)
                firebaseAnalytics.logEvent("Location", bundle)
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }

            if (locality.isNotBlank()) {
                isCorrectLocalityFound = true
                appDatabase.GeoCodedDao().insertAll(
                    GeoCoded(
                        latitude = latitude,
                        longitude = longitude,
                        locality = locality,
                        subLocality = subLocality,
                        countryName = countryName
                    )
                )
                Log.d("hgdhag", "locality.isNotBlank() $locality")
            } else {

                var nearest: GeoCoded? = null
                var nearestMeter: Float? = null
                val floatArray = FloatArray(5)
                val km5 = (5 * 1000).toFloat()
                appDatabase.GeoCodedDao().getAll().forEach {
                    Location.distanceBetween(
                        latitude, longitude, it.latitude, it.longitude, floatArray
                    )
                    if (floatArray[0] < km5 && (nearestMeter == null || floatArray[0] < nearestMeter!!)) {
                        nearestMeter = floatArray[0]
                        nearest = it
                    }

                }

                if (nearest != null) {
                    isCorrectLocalityFound = false
                    locality = nearest?.locality ?: ""
                    subLocality = nearest?.subLocality ?: ""
                    countryName = nearest?.countryName ?: ""
                    isNear = true
                    Log.d("hgdhag", "nearest != null $locality")
                } else {

                    Log.d("hgdhag", "else ")
                    requestLocationRepeatLoop()

                }


            }

            sp.edit()
                .putDouble("latitude", latitude)
                .putDouble("longitude", longitude)
                .putDouble("altitude", location?.altitude)
                .putString("locality", locality)
                .putBoolean("isLocationSet", true)
                .commit()

            runOnUiThread {
                isIconRotationNeed = false
                onLocationCallBack(
                    location,
                    locality,
                    isNear,
                    subLocality, countryName
                )
            }

        }
    }

    abstract fun onLocationCallBack(
        location: Location,
        locality: String,
        near: Boolean,
        subLocality: String,
        countryName: String
    )


    /**
    isCorrectLocalityFound
    null: not found
    false: near found
    true: correct found
     */
    var isCorrectLocalityFound: Boolean? = null


    fun requestLocationRepeatLoop() {
        Log.d("hgdhag", "requestLocationRepeatLoop $waitingSeconds")
        try {
            runOnUiThread {
                Handler().postDelayed({
                    Log.d(
                        "hgdhag",
                        "postDelayed $isResumeState ${hasLocationPermissions()} ${isLocationEnabled()}  $isCorrectLocalityFound"
                    )
                    if (isResumeState && hasLocationPermissions() && isLocationEnabled() && isCorrectLocalityFound != true) {
                        Log.d("hgdhag", "postDelayed  if")
                        startLocationServiceInitialisation()
                        waitingSeconds++
                    }
                }, (waitingSeconds * 1000))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("hgdhag", "postDelayed  Exception ${e.message}")
        }
    }


    var isResumeState = false

    var waitingSeconds: Long = 1


    override fun onResume() {
        super.onResume()
        isResumeState = true
    }


    override fun onPause() {
        super.onPause()
        isResumeState = false
    }


    fun requestForGPSLocationWithRotationAnimation() {
        hideKeyboardView()
        isIconRotationNeed = true
        animateRotate()
        startLocationServiceInitialisation()
    }


    fun animateRotate() {
        if (isIconRotationNeed) {
            refresh?.let { refresh ->
                ObjectAnimator
                    .ofFloat(refresh, View.ROTATION, 0f, 360f)
                    .setDuration(600)
                    .apply {
                        repeatCount = 100
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                                if (!isIconRotationNeed) {
                                    this@apply.cancel()
                                }
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                if (!isIconRotationNeed) {
                                    this@apply.cancel()
                                }
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {

                            }

                        })
                    }

                    .start()
            }


            locationButton?.let { locationButton ->
                ObjectAnimator
                    .ofFloat(locationButton, View.ROTATION, 0f, 360f)
                    .setDuration(600)
                    .apply {
                        repeatCount = 100
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                                if (!isIconRotationNeed) {
                                    this@apply.cancel()
                                }
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                if (!isIconRotationNeed) {
                                    this@apply.cancel()
                                }
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {

                            }

                        })
                    }

                    .start()
            }


        }

    }

    var isIconRotationNeed = false


}