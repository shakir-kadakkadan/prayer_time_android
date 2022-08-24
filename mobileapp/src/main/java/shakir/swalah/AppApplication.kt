package shakir.swalah

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.multidex.MultiDexApplication
import com.azan.types.PrayersType
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
//import com.google.gson.GsonBuilder
//import io.reactivex.internal.functions.Functions
//import io.reactivex.plugins.RxJavaPlugins
//import okhttp3.Cache
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
//import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

class AppApplication : MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()
        instance = this
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


    companion object {
        lateinit var restService: WebServices
        lateinit var instance: AppApplication

        fun getArabicNames(string: String?): String? {
            return when (string?.let { PrayersType.valueOf(it) }) {
                PrayersType.FAJR -> "الفجر"
                PrayersType.SUNRISE -> "الشروق"
                PrayersType.ZUHR -> "الظهر"
                PrayersType.ASR -> "العصر"
                PrayersType.MAGHRIB -> "المغرب"
                PrayersType.ISHA -> "العشاء"
                else -> null
            }
        }


    }
}