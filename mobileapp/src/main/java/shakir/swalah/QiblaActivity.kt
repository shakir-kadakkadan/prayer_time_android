package shakir.swalah

import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import com.google.firebase.crashlytics.FirebaseCrashlytics

import shakir.swalah.compass.CompassSensorManager

import shakir.swalah.databinding.ActivityQiblaBinding
import java.util.*


class QiblaActivity : MainActivityLocation() {
    override fun onLoactionPermissionDialogDone() {

    }

    override fun onLocationCallBack(
        location: Location,
        locality: String,
        near: Boolean,
        subLocality: String,
        countryName: String
    ) {
        try {
            updateCompassView(location, locality)
        } catch (e: Exception) {
           e.report()
            toast(e.message)
        }
    }


    private lateinit var binding: ActivityQiblaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQiblaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)
        binding.locationButton.setOnClickListener {
            try {
                requestForGPSLocationWithRotationAnimation()
            } catch (e: Exception) {
                e.report()
                toast(e.message)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        try {

            makkaLoc = Location("service Provider")
            makkaLoc!!.latitude = 21.4225 //kaaba latitude setting
            makkaLoc!!.longitude = 39.8262 //kaaba longitude setting

            northPoleLoc = Location("service Provider")
            northPoleLoc!!.latitude = 90.0 //NorthPole latitude setting
            northPoleLoc!!.longitude = 0.0 //NorthPole longitude setting
            binding.rootViewLL.post {
                compassSensorManager = CompassSensorManager(this)

                val locality = sp.getString("Qibla_locality", null)
                updateCompassView(Location("saved").apply {
                    latitude = sp.getDouble("Qibla_latitude", INVALID_CORDINATE)
                    longitude = sp.getDouble("Qibla_longitude", INVALID_CORDINATE)
                    altitude = sp.getDouble("Qibla_altitude", 0.0)
                }, locality = locality)


                try {
                    startLocationServiceInitialisation()
                } catch (e: Exception) {
                    e.report()
                    toast(e.message)
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }


    private var compassSensorManager: CompassSensorManager? = null
    private var makkaLoc: Location? = null
    private var northPoleLoc: Location? = null


    private fun updateCompassView(myLocation: Location?, locality: String?) {

        try {
            binding.locationTV.setText(if (locality.isNullOrBlank()) "My Location" else locality)
            if (myLocation != null && compassSensorManager != null) {

                //CatLogger.d(TAG, "upadteCompassView() called with: myLocation = [" + myLocation + "]");
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val fromLocation =
                        geocoder.getFromLocation(myLocation!!.latitude, myLocation!!.longitude, 1)
                    binding.myLocationTextView!!.text = fromLocation?.getOrNull(0)?.getAddressLine(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                binding.dialFrame?.init(compassSensorManager, myLocation, northPoleLoc)
                binding.needleFrame?.init(compassSensorManager, myLocation, makkaLoc)
                if (binding.needleFrame?.visibility != View.VISIBLE) {
                    binding.needleFrame?.visibility = View.VISIBLE
                }
                binding.progressBar?.visibility = View.GONE

                compassSensorManager?.onResume()
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }

    }


    override fun onResume() {
        super.onResume()
        compassSensorManager?.onResume()
    }


    override fun onPause() {
        super.onPause()
        compassSensorManager?.onPause()
    }


}
