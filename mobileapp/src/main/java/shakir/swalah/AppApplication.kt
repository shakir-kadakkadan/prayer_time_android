package shakir.swalah

//import com.google.gson.GsonBuilder
//import io.reactivex.internal.functions.Functions
//import io.reactivex.plugins.RxJavaPlugins
//import okhttp3.Cache
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
//import retrofit2.converter.gson.GsonConverterFactory

import android.content.SharedPreferences
import android.os.PowerManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication

class AppApplication : MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()
        instance = this
        sp = Util.getMySharedPreference(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


        /*   try {

               ProviderInstaller.installIfNeeded(this)
           } catch (e: GooglePlayServicesRepairableException) {
               GoogleApiAvailability.getInstance()
                   .showErrorNotification(this, e.connectionStatusCode)
               e.printStackTrace()
               Crashlytics.logException(e)
           } catch (e: Exception) {
               e.printStackTrace()
               Crashlytics.logException(e)
           }
   */

//        val cacheSize = 10 * 1024 * 1024 // 10 MB
//        val cache = Cache(cacheDir, cacheSize.toLong())
//
//
//        val logging = HttpLoggingInterceptor()
//        if (BuildConfig.DEBUG || /*isMyTestDevice()*//*TODO : */true)
//            logging.level = HttpLoggingInterceptor.Level.BODY
//        else
//            logging.level = HttpLoggingInterceptor.Level.NONE
//
//        val httpClient = OkHttpClient.Builder()
//            .cache(cache)
//            .connectTimeout(60, TimeUnit.SECONDS)
//            .writeTimeout(60, TimeUnit.SECONDS)
//            .readTimeout(60, TimeUnit.SECONDS)
//
//        httpClient.addInterceptor(logging)
//
//
//        val gsonBuilder = GsonBuilder()
//        gsonBuilder.setLenient()
//        val gson = gsonBuilder.create()
//        var retrofitRx = Retrofit.Builder()
//             .baseUrl("https://example.com/")
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .client(httpClient.build())
//            .build()
//        restService = retrofitRx.create(WebServices::class.java)
//        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())


    }

    fun acquireScreenCpuWakeLock() {
        try {
            if (sCpuWakeLock != null) {
                return
            }
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            sCpuWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK
                        or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "TYLER"
            )
            if (sCpuWakeLock?.isHeld == true)
                sCpuWakeLock?.acquire()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releaseCpuLock() {
        try {
            if (sCpuWakeLock != null && sCpuWakeLock?.isHeld == true) {
                sCpuWakeLock?.release()
                sCpuWakeLock = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var sCpuWakeLock: PowerManager.WakeLock? = null


    companion object {
        lateinit var restService: WebServices
        lateinit var instance: AppApplication
        lateinit var sp: SharedPreferences

        fun getArabicNames(index: Int): String? {
            return when (index) {
                0 -> "الفجر"
                1 -> "الشروق"
                2 -> "الظهر"
                3 -> "العصر"
                4 -> "المغرب"
                5 -> "العشاء"
                else -> null
            }
        }


    }

}