package shakir.swalah

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_location_selector.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import shakir.swalah.models.Cord
import java.util.*
import kotlin.math.absoluteValue

class LocationSelectorAvtivity : BaseActivity() {


    val arrayList = arrayListOf<Cord>()
    lateinit var adapter: LocListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selector)
        assets.open("worldcities.csv").bufferedReader().useLines {
            arrayList.clear()
            it.forEach {
                val splited = it.trim().split(',')
                arrayList.add(Cord(splited[0], splited[1].toDouble(), splited[2].toDouble()))
            }
        }
        if (sp.getBoolean("v2_set",false)==true){
            locationLastForDistanceCalculation=Location("").apply {
                latitude=sp.getDouble("v2_latitude",0.0)
                longitude=sp.getDouble("v2_longitude",0.0)
            }
        }
        adapter = LocListAdapter() {
            sp.edit().putString("v2_locality", it.name)
                .putDouble("v2_latitude", it.latitude)
                .putDouble("v2_longitude", it.longitude)
                .putBoolean("v2_set", true)
                .commit()
            onBackPressed()

        }
        recyclerView.adapter = adapter
        notifyRV()



        editText.doOnTextChanged { text, start, before, count ->
            notifyRV()
        }
        clear.setOnClickListener {
            editText.setText("")
        }

        requestLocationPermisiion()
    }


    fun notifyRV() {
        val key = editText.text.toString()
        if (key.isBlank() && locationLastForDistanceCalculation != null) {
            adapter.arrayList.clear()
            adapter.arrayList.addAll(arrayList.sortedBy {
                locationLastForDistanceCalculation!!.distanceTo(Location("").apply { latitude = it.latitude; longitude = it.longitude }).absoluteValue
            })
            adapter.notifyDataSetChanged()
            clear.isVisible = false
        } else {
            adapter.arrayList.clear()
            adapter.arrayList.addAll(arrayList.filter { it.name.startsWith(key, ignoreCase = true) }.plus(
                arrayList.filter { it.name.contains(key, ignoreCase = true) }
            ).distinct())
            adapter.notifyDataSetChanged()
            clear.isVisible = true
        }
    }


    var permissionAskMaxTimes = 3
    fun requestLocationPermisiion() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (--permissionAskMaxTimes) > 0 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("LocationServices requestPermissions")
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 15)

                return
            }

            println("LocationServices")
            requestLocationTurnOn()
            requestCurrentLoaction()

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    private val REQUEST_CHECK_SETTINGS: Int = 657

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CHECK_SETTINGS == requestCode && resultCode == Activity.RESULT_OK) {
            requestCurrentLoaction()
            Handler().postDelayed({
                requestCurrentLoaction()
            },2000)

        }

    }

    override fun onResume() {
        super.onResume()

    }

    var askLocationSettingsMax = 2

    fun requestLocationTurnOn() {
        if (askLocationSettingsMax > 0)
            try {

//            val locationRequest = LocationRequest.create()?.apply {
//                interval = 10000
//                fastestInterval = 5000
//                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            }

                val locationRequest = LocationRequest.create()?.apply {
                    if (/*regular_upadate*/false) {
                        interval = 250
                        fastestInterval = 500
                        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                    } else {
                        interval = 60 * 1000
                        fastestInterval = 60 * 1000
                        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                    }

                }

                println("requestLocationTurnOn")
                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
                val client: SettingsClient = LocationServices.getSettingsClient(this)
                val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
                task.addOnSuccessListener {
                    println("requestLocationTurnOn ${it.locationSettingsStates}")
                    requestCurrentLoaction()
                }
                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            /*  exception.startResolutionForResult(activity!!,
                                      REQUEST_CHECK_SETTINGS)*/

                            startIntentSenderForResult(
                                exception.getResolution().getIntentSender(),
                                REQUEST_CHECK_SETTINGS,
                                null,
                                0,
                                0,
                                0,
                                null
                            );

                            askLocationSettingsMax--
                            return@addOnFailureListener


                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error_red.
                        }


                    }

                }
            } catch (e: Exception) {
                e.report()
            }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 15) {
            requestLocationTurnOn()

        }
    }


    var locationLastForDistanceCalculation: Location? = null

    @SuppressLint("MissingPermission")
    fun requestCurrentLoaction() {
        try {
            LocationServices.getFusedLocationProviderClient(this)
                .getCurrentLocation(android.location.LocationRequest.QUALITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        GlobalScope.launch(Dispatchers.Main) {
                            try {
                                val localityName = fetchLocality(location)
                                locationTV.setText("Select Current Location\n$localityName")
                                locationTV.setOnClickListener {
                                    sp.edit().putString("v2_locality", localityName)
                                        .putDouble("v2_latitude", location.latitude)
                                        .putDouble("v2_longitude", location.longitude)
                                        .putBoolean("v2_set", true)
                                        .commit()
                                    onBackPressed()
                                }
                                runOnUiThread {
                                    notifyRV()
                                }
                                locationLastForDistanceCalculation = location
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }


                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun fetchLocality(location: Location): String {
        return withContext(Dispatchers.IO) {
            val latitude = location.latitude
            val longitude = location.longitude
            val geocoder = Geocoder(this@LocationSelectorAvtivity, Locale.getDefault())
            val addresses: List<Address>? = try {
                geocoder.getFromLocation(latitude, longitude, 1)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            var locality = addresses?.getOrNull(0)?.locality ?: ""
            var subLocality = addresses?.getOrNull(0)?.subLocality ?: ""
            var countryName = addresses?.getOrNull(0)?.countryName ?: ""


            return@withContext locality
        }
    }

    override fun onBackPressed() {

        if (sp.getBoolean("v2_set", false) == true) {
            if (intent.getBooleanExtra("comeBack", false) == true) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                finish()
            }
        }else{
            finish()
        }
    }


}