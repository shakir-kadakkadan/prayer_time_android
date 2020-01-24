package shakir.swalah

import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_qibla.*
import shakir.swalah.compass.CompassSensorManager
import java.util.*


class QiblaActivity : MainActivityLocation() {

    override fun onLocationCallBack(
        location: Location,
        locality: String,
        near: Boolean,
        subLocality: String,
        countryName: String
    ) {
        updateCompassView(location, locality)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qibla)
        adjustWithSystemWindow(rootViewLL, topSpacer, true)
        locationButton.setOnClickListener {
           requestForGPSLocationWithRotationAnimation()
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
            rootViewLL.post {
                compassSensorManager = CompassSensorManager(this)

                val locality = sp.getString("locality", null)
                updateCompassView(Location("saved").apply {
                    latitude = sp.getDouble("latitude", INVALID_CORDINATE)
                    longitude = sp.getDouble("longitude", INVALID_CORDINATE)
                    altitude = sp.getDouble("altitude", 0.0)
                }, locality = locality)


                startLocationServiceInitialisation()
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }
    }


    private var compassSensorManager: CompassSensorManager? = null
    private var makkaLoc: Location? = null
    private var northPoleLoc: Location? = null


    private fun updateCompassView(myLocation: Location?, locality: String?) {

        try {
            locationTV.setText(if (locality.isNullOrBlank()) "My Location" else locality)
            if (myLocation != null && compassSensorManager != null) {

                //CatLogger.d(TAG, "upadteCompassView() called with: myLocation = [" + myLocation + "]");
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val fromLocation =
                        geocoder.getFromLocation(myLocation!!.latitude, myLocation!!.longitude, 1)
                    myLocationTextView!!.text = fromLocation[0].getAddressLine(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                dialFrame?.init(compassSensorManager, myLocation, northPoleLoc)
                needleFrame?.init(compassSensorManager, myLocation, makkaLoc)
                if (needleFrame?.visibility != View.VISIBLE) {
                    needleFrame?.visibility = View.VISIBLE
                }
                progress_bar?.visibility = View.GONE

                compassSensorManager?.onResume()
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
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
