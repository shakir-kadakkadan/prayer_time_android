package shakir.swalah


import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
//import com.google.gson.Gson
//import com.google.gson.JsonObject
//import okhttp3.MediaType
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction0


val TextView?.isEmpty: Boolean
    get() {
        return this?.text?.toString()?.trim()?.isEmpty() ?: true
    }


val TextView?.isValidEmail: Boolean
    get() {
        return Patterns.EMAIL_ADDRESS.matcher(this?.text?.toString()).matches()
    }


fun TextView?.showError(textRes: Int): Boolean {
    if (this != null) {
        setError(context.getString(textRes))
        requestFocus()
    }
    return false
}


val TextView?.isValidPhone: Boolean
    get() {
        return Patterns.PHONE.matcher(this?.text?.toString()).matches()
    }


val TextView?.hasText: Boolean
    get() {
        return this?.text?.toString()?.isNotBlank() == true
    }


/*fun ImageView?.loadUrl(url: String?, callback: Callback? = null, errorHolder: Int? = null) {
    if (this == null) return
    if ((url.isNullOrBlank() && errorHolder != null) || (getTag(R.string.image_error) == true && errorHolder != null)) {
        setImageResource(errorHolder)
    } else {
        val picas = Picasso.get().load(BASE_URL_IMAGE + url).noFade()

        picas.into(this, object : Callback {
            override fun onError(e: java.lang.Exception?) {
                setTag(R.string.isDissolvEffectDone, true)
                callback?.onError(e)
                setTag(R.string.image_error, true)
                if (errorHolder != null) setImageResource(errorHolder)
            }

            override fun onSuccess() {
                if (getTag(R.string.isDissolvEffectDone) != true) {
                    val fadeOut = AlphaAnimation(0f, 1f)
                    fadeOut.interpolator = AccelerateInterpolator()
                    fadeOut.duration = 750
                    startAnimation(fadeOut)
                }

                setTag(R.string.isDissolvEffectDone, true)
                callback?.onSuccess()
            }

        })
    }


}*/


/*
fun ImageView?.loadProfileImage(url: String?, callback: Callback? = null) {
    if (this == null) return
*/
/*    if (url == null || url.trim().isBlank() || this.getTag(R.string.image_error) == true) {
        *//*
*/
/* IMAGE Error*//*
*/
/*
        this.setTag(R.string.image_error, true)
        Picasso.get().load(BASE_URL_IMAGE + url).*//*
*/
/*placeholder(R.drawable.avatar).error(R.drawable.avatar).*//*
*/
/*into(this, object : Callback {
            override fun onSuccess() {
                if (getTag(R.string.imageLoaded) != true) {
                    setAlpha(0f)
                    animate().setDuration(250).alpha(1f).start();
                }

                setTag(R.string.imageLoaded, true)
                callback?.onSuccess()
            }

            override fun onError(e: java.lang.Exception?) {
                setTag(R.string.imageLoaded, true)
                callback?.onError(e)
                this@loadProfileImage.setTag(R.string.image_error, true)
            }

        })

    } else {
        Picasso.get().load(BASE_URL_IMAGE + url)*//*
*/
/*.error(R.drawable.avatar)*//*
*/
/*.into(this, object : Callback {
            override fun onSuccess() {
                if (getTag(R.string.imageLoaded) != true) {
                    setAlpha(0f)
                    animate().setDuration(250).alpha(1f).start();
                }

                setTag(R.string.imageLoaded, true)
                callback?.onSuccess()
            }

            override fun onError(e: java.lang.Exception?) {
                setTag(R.string.imageLoaded, true)
                callback?.onError(e)
                this@loadProfileImage.setTag(R.string.image_error, true)
            }

        })
    }*//*




    Picasso.get().load(BASE_URL_IMAGE + url).noFade()*/
/*.error(R.drawable.avatar)*//*
.into(this, object : Callback {
        override fun onSuccess() {
            if (getTag(R.string.isDissolvEffectDone) != true) {
                val fadeOut = AlphaAnimation(0f, 1f)
                fadeOut.interpolator = AccelerateInterpolator()
                fadeOut.duration = 750
                startAnimation(fadeOut)
            }

            setTag(R.string.isDissolvEffectDone, true)
            callback?.onSuccess()
        }

        override fun onError(e: java.lang.Exception?) {
            setTag(R.string.isDissolvEffectDone, true)
            callback?.onError(e)
            setTag(R.string.image_error, true)
        }

    })
}
*/

/*

fun JsonObject.addProperty(s: String, editText: TextView?) {
    addProperty(s, editText?.text?.toString()?.trim())

}
*/


fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
    return cal1 != null &&
            cal2 != null &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE)
}


fun Calendar.isTodayOrTomeorrow(): Boolean {
    return this != null && (isSameDay(this, Calendar.getInstance()) ||
            isSameDay(this, Calendar.getInstance().apply { add(Calendar.DATE, 1) }))
}


fun Calendar.isTodayOrTomeorrowOrDayAfterTomorrow(): Boolean {
    return this != null && (
            isSameDay(this, Calendar.getInstance())
                    ||
                    isSameDay(this, Calendar.getInstance().apply { add(Calendar.DATE, 1) })
                    ||
                    isSameDay(this, Calendar.getInstance().apply { add(Calendar.DATE, 2) })
            )
}


fun String?.convertApiDateToDisplayDate(): String {
    return if (this == null || this.isBlank() || this == "0000-00-00") {
        ""

    } else {
        try {

            SimpleDateFormat("d-MM-yyyy", Locale.ENGLISH).format(
                SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(
                    this
                )
            )
        } catch (e: Exception) {
            ""
        }
    }
}

fun String?.convertApiDateToCalendar(): Calendar? {
    return if (this == null || this.isBlank() || this == "0000-00-00") {
        null

    } else {
        try {

            Calendar.getInstance().apply {
                timeInMillis = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.ENGLISH
                ).parse(this@convertApiDateToCalendar).time
            }
        } catch (e: Exception) {
            null
        }
    }
}


fun String?.convertDisplayDateToApiDate(): String {
    return if (this == null || this.isBlank() || this == "0000-00-00") {
        ""

    } else {
        try {

            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
                SimpleDateFormat("d-MM-yyyy", Locale.ENGLISH).parse(
                    this
                )
            )
        } catch (e: Exception) {
            ""
        }
    }
}


fun Calendar?.convertCaledarToApiDate(): String {
    return if (this == null || this.timeInMillis < 1000) {
        ""

    } else {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(this.time)
        } catch (e: Exception) {
            ""
        }
    }
}

/*
fun TextInputLayout.markRequired() {
    hint = "$hint *"
}
*/


fun EditText?.onDone(
    kFunctionOnSearch: KFunction0<Unit>,
    reset: View? = null,
    kFunctionReset: KFunction0<Unit>? = null
) {
    this?.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus)
            reset?.visibility = View.VISIBLE
        else
            reset?.visibility = View.GONE

    }

    this?.setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH
            || actionId == EditorInfo.IME_ACTION_DONE ||
            actionId == EditorInfo.IME_ACTION_NEXT
            || event != null && event.getAction() == KeyEvent.ACTION_DOWN
            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
        ) {
            /*   this.hideKeyboardView()*/
            kFunctionOnSearch()
            //  this@onDone.clearFocus()

            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }

    reset?.setOnClickListener {
        if (kFunctionReset != null) {
            kFunctionReset()
        }
    }
}


public interface OnDone {
    public fun onDone()
}


fun EditText?.onDone(l: (View) -> Unit) {
    this?.setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH
            || actionId == EditorInfo.IME_ACTION_DONE ||
            actionId == EditorInfo.IME_ACTION_NEXT
            || event != null && event.getAction() == KeyEvent.ACTION_DOWN
            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
        ) {
            /*   this.hideKeyboardView()*/
            l.invoke(this)
            //  this@onDone.clearFocus()

            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }

}


fun View?.hideKeyboardView() {
    if (this == null || this.context == null) return
    val inputMethodManager =
        this.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

    if (inputMethodManager != null && inputMethodManager.isActive) {
        //inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }

}

fun Activity?.hideKeyboardView() {
    if (this != null && this.window != null && this.window.decorView != null) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.window.decorView.windowToken, 0)
    }
}


fun Fragment?.hideKeyboardView() {
    this?.activity?.window?.decorView?.let {
        val imm =
            this.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.activity!!.window.decorView.windowToken, 0)
    }


}


fun TextView.bold() {
    this.text = this.text.toString().bold()
}

fun String?.bold(): SpannableString {
    return SpannableString(this).apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}


fun Context?.getDeviceID(): String? {
    return Settings.Secure.getString(this?.contentResolver, Settings.Secure.ANDROID_ID)
}


fun Fragment?.getDeviceID(): String? {
    return Settings.Secure.getString(this?.activity?.contentResolver, Settings.Secure.ANDROID_ID)
}


/*fun Context?.getReqBody(): JsonObject {
    return JsonObject().apply {
        if (this@getReqBody != null) {
            val user = SharedPreferenceUtils.getUser(this@getReqBody)
            addProperty("user_id", user?.customerId ?: "")
            addProperty("device_type", "Android")
            addProperty("customer_id", user?.customerId ?: "")
            addProperty("token", user?.token ?: "")
            addProperty("language_id", SharedPreferenceUtils.getLanguageId(this@getReqBody))
            addProperty("lang_id", SharedPreferenceUtils.getLanguageId(this@getReqBody))
            addProperty("country_id", "1")

        }

    }
}*/


/*fun Fragment?.getReqBody(): JsonObject {
    return this?.activity.getReqBody()
}*/

/*fun Context?.jsonRequestBody(vararg pairs: Pair<String, Any?>): JsonObject {
    return getReqBody().apply {
        pairs.forEach {
            val second = it.second
            addProperty(
                it.first,
                if (second == null)
                    null
                else if (second is TextView) {
                    second?.text?.toString()?.trim()
                } else {
                    second.toString()
                }
            )
        }
    }
}*/


/*
fun Fragment?.jsonRequestBody(vararg pairs: Pair<String, Any?>): JsonObject {
    return getReqBody().apply {
        pairs.forEach {
            val second = it.second
            addProperty(
                it.first,
                if (second == null)
                    null
                else if (second is TextView) {
                    second?.text?.toString()?.trim()
                } else {
                    second.toString()
                }
            )
        }
    }
}
*/


val Calendar.isLessThanOrEqual48HoursFromNow: Boolean
    get() {
        return timeInMillis - Calendar.getInstance().timeInMillis <= TimeUnit.HOURS.toMillis(48)
    }


val Calendar.isLessThanOrEqual24HoursFromNow: Boolean
    get() {
        return timeInMillis - Calendar.getInstance().timeInMillis <= TimeUnit.HOURS.toMillis(24)
    }


/*fun String?.toRequestBody(): RequestBody {
    if (this == null)
        return    "".toRequestBody("text/plain".toMediaType())
    else
        return toRequestBody("text/plain".toMediaType())
}


val JsonObject.toMultiPart: HashMap<String, RequestBody>
    get() {
        val partRequest = HashMap<String, RequestBody>()
        keySet().forEach {
            partRequest[it] = this.get(it).asString.toRequestBody()
        }
        return partRequest
    }*/


//fun HashMap<String, RequestBody>.addImage(
//    file: File?,
//    key: String = "image",
//    mime: String = "image/jpeg"
//): HashMap<String, RequestBody> {
//    if (file != null) {
//        val requestFile = RequestBody.create(mime.toMediaType(), file)
//        this["$key\"; filename=\"" + file.name + "\""] = requestFile
//    }
//    return this
//}


public fun Intent.putExtra(s: String, textView: TextView) {
    putExtra(s, textView.text.toString())
}


public fun Bundle.putString(s: String, textView: TextView) {
    putString(s, textView.text.toString())
}


//@Throws(Exception::class)
//fun JsonObject?.isStatusSuccess(): Boolean {
//    return this?.get("status")?.asString == "200"
//}


val RTL_EMBED = "\u202B"
val LTR_EMBED = "\u202A"

public fun String.ltrEmbed(): String {
    return LTR_EMBED + this + LTR_EMBED
}


@SuppressLint("SetTextI18n")
public fun TextView.ltrEmbed() {
    return this.setText(LTR_EMBED + this.text.toString() + LTR_EMBED)
}


public fun Activity?.isTrue(string: String): Boolean {
    return this?.intent?.getBooleanExtra(string, false) == true
}


public fun Intent?.isTrue(string: String): Boolean {
    return this?.getBooleanExtra(string, false) == true
}


public fun Bundle?.isTrue(string: String): Boolean {
    return this?.getBoolean(string, false) == true
}


public fun Fragment?.isTrue(string: String): Boolean {
    return this?.arguments?.getBoolean(string, false) == true
}


fun Context.getResearchStorageDir(): File {
    return File(filesDir, "docs").apply {
        if (!exists())
            mkdir()
        else if (!isDirectory) {
            delete()
            mkdir()
        }


    }


}


fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return ""
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(this)
        SimpleDateFormat("d-M-yyyy", Locale.ENGLISH).format(parsed.time)
    } catch (e: Exception) {
        this
    }
}


/*
fun JsonObject?.getAPIResponseMessage(): String {
    var message: String? = null
    if (this != null) {
        try {
            if (*/
/*AppApplication.isArabic*//*
false*/
/*todo*//*
) {
                if (this.has("message_arabic")) {
                    message = this.get("message_arabic").asString
                } else if (this.has("message_ar")) {
                    message = this.get("message_ar").asString
                } else if (this.has("message")) {
                    message = this.get("message").asString
                }

            } else {
                if (this.has("message")) {
                    message = this.get("message").asString
                } else if (this.has("message_ar")) {
                    message = this.get("message_ar").asString
                } else if (this.has("message_arabic")) {
                    message = this.get("message_arabic").asString
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    return if (message == null) "" else message
}


fun Context?.showAlertDialog(jsonObject: JsonObject?, callback: (() -> Unit)? = null): Boolean {
    if (this != null) {
        AlertDialog.Builder(this)
            .setMessage(jsonObject.getAPIResponseMessage())
            .setPositiveButton(R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
            .setOnDismissListener {
                it.dismiss()
                callback?.invoke()
            }
            .show()
    }
    return false
}
*/


/*
fun Fragment?.showAlertDialog(jsonObject: JsonObject?, callback: (() -> Unit)? = null): Boolean {
    this?.activity?.showAlertDialog(jsonObject, callback)
    return false
}
*/


fun Context?.showAlertDialog(stringResID: Int, callback: (() -> Unit)? = null): Boolean {
    if (this != null) {
        AlertDialog.Builder(this)
            .setMessage(stringResID)
            .setPositiveButton(R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
            .setOnDismissListener {
                it.dismiss()
                callback?.invoke()
            }
            .show()
    }
    return false
}

fun Fragment?.showAlertDialog(stringResID: Int, callback: (() -> Unit)? = null): Boolean {
    this?.activity?.showAlertDialog(stringResID, callback)
    return false
}


fun Context?.showAlertDialog(string: String, callback: (() -> Unit)? = null): Boolean {
    if (this != null) {
        AlertDialog.Builder(this)
            .setMessage(string)
            .setPositiveButton(R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
            .setOnDismissListener {
                it.dismiss()
                callback?.invoke()
            }
            .show()
    }
    return false
}

fun Fragment?.showAlertDialog(string: String, callback: (() -> Unit)? = null): Boolean {
    this?.activity?.showAlertDialog(string, callback)
    return false
}

fun Context?.showAlertDialogRetry(callback: (() -> Unit)? = null) {
    if (this != null) {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(R.string.anErrorOccuredPleaseRetry)
            .setNegativeButton(R.string.cancel) { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .setPositiveButton(R.string.Retry) { dialogInterface, i ->
                dialogInterface.dismiss()
                callback?.invoke()
            }
            .show()
    }

}

fun Fragment?.showAlertDialogRetry(callback: (() -> Unit)? = null): Boolean {
    this?.activity?.showAlertDialogRetry(callback)
    return false
}


fun View.setOnClickListenerDummy(listener: (View) -> Unit) {
    listener(this)
}


fun <TYPE> Array<TYPE>.makeSingleSelection(
    default: View? = null,
    function: ((view: View?) -> Unit)? = null
) {
    if (isArrayOf<View>()) {
        default?.isSelected = true
        function?.invoke(default)
        forEach { it1 ->
            (it1 as View).setOnClickListener { it2 ->
                forEach { it3 ->
                    (it3 as View).isSelected = it1 == it3
                }
                function?.invoke(it1)
            }
        }
    }

}


fun Spinner?.getSelectedIndex(): Int {
    return this?.selectedItemPosition?.minus(1) ?: -1
}


/*fun Spinner?.populateWith(
    arrayList: List<String?>?,
    function: ((index: Int) -> Unit)? = null,
    textView: TextView? = null,
    parent: View? = null,
    initialSelectedIndex: Int ?= null
) {
    this?.post {
        this?.let { spinner ->

            var valueTextView = textView

            val adapter = ArrayAdaperSpinner(
                context, arrayList?.map { it -> it }).apply {

            }
            spinner.adapter = adapter
            spinner.prompt = context.getString(R.string.work_type)
            spinner.adapter = NothingSelectedSpinnerAdapter(
                adapter,
                R.layout.spinner_item_1,
                context,
                context.getString(R.string.work_type)
            )
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    var selectedWorkType = arrayList?.getOrNull(position - 1)
                    if (selectedWorkType?.isNotBlank() == true) {
                        valueTextView?.text = selectedWorkType
                        valueTextView?.visibility = View.VISIBLE
                        function?.invoke(position - 1)
                    }


                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }

            Log.d("GFGFGFGF","${spinner.getSelectedIndex()} ${spinner.selectedItemPosition}")


            if (spinner.getSelectedIndex()<0&&initialSelectedIndex!=null&&initialSelectedIndex >= 0 && initialSelectedIndex < arrayList?.size ?: 0) {
                spinner.post {
                    var selectedWorkType = arrayList?.getOrNull(initialSelectedIndex)
                    if (selectedWorkType?.isNotBlank() == true) {
                        valueTextView?.text = selectedWorkType
                        valueTextView?.visibility = View.VISIBLE
                        function?.invoke(initialSelectedIndex)
                        spinner.setSelection(initialSelectedIndex + 1)
                    }
                }
            }


            spinner.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            spinner.dropDownWidth = spinner.width
            if (isMyTestDevice()) {
                *//*  spinner.alpha = .5f
                  spinner.layoutParams.height = 3*//*
            }

            fun View.setClicker() {
                if (this !is AdapterView<*>) {
                    if (
                        (this is TextView)
                        ||

                        (this is LinearLayout)
                        ||

                        (this is FrameLayout)
                        ||

                        (this is RelativeLayout)
                        ||

                        (this is TextInputLayout)
                        ||

                        (this is TextInputEditText)

                    ) {
                        this?.setOnClickListener {
                            spinner.performClick()
                        }

                        if (this is EditText) {
                            setFocusable(false)
                            setFocusableInTouchMode(false)
                            valueTextView = this
                        }


                    }

                    if (this is ViewGroup) {
                        this?.forEach {
                            it?.setClicker()
                        }
                    }


                }


            }

            parent?.setClicker()


            if (parent == null) {
                (spinner.parent as View).setClicker()
            }

            if (spinner.visibility == View.VISIBLE) {
                spinner.visibility = View.INVISIBLE
                spinner.alpha = 0f
            }


        }
    }


}*/


/*
fun Intent.putExtra(s: String, json: JsonObject) {
    putExtra(s, Gson().toJson(json))
}
*/


fun Bundle.putData(value: Parcelable?): Bundle {
    putParcelable("__DATA", value)
    return this
}


fun Intent.putData(value: Parcelable?): Intent {
    putExtra("__DATA", value)
    return this
}


val image_transition_name = "image_transition_name"

fun Intent.putImageTransName(value: String?): Intent {
    putExtra(image_transition_name, value)
    return this
}


fun Activity.getImageTransName(): String? {
    return intent?.getStringExtra(image_transition_name)
}


fun Fragment.getImageTransName(): String {
    return arguments?.getString(image_transition_name) ?: ""
}


fun <T : Parcelable> Bundle.getData(): T? {
    return getParcelable<T>("__DATA")
}


fun <T : Parcelable> Fragment.getData(): T? {
    return arguments?.getParcelable<T>("__DATA")
}


fun <T : Parcelable> Activity.getData(): T? {
    return intent.getParcelableExtra<T>("__DATA")
}


fun copyInputStreamToFile(inputStream: InputStream, outputStream: FileOutputStream) {
    val buffer = ByteArray(8192)
    inputStream.use { input ->
        outputStream.use { fileOut ->

            while (true) {
                val length = input.read(buffer)
                if (length <= 0)
                    break
                fileOut.write(buffer, 0, length)
            }
            fileOut.flush()
            fileOut.close()
        }
    }
    inputStream.close()
}


/*var ImageView.imageUrl: String?
    get() {
        return null
    }
    set(s: String?) {

        Picasso
            .get()
            .load(IMAGE_BASE_URL + s)
            .placeholder(R.drawable.place_holder)
            .resize(this.context.resources.getDimensionPixelSize(R.dimen._50sdp), 0)
            .into(this)
    }*/


/*

@SuppressLint("CheckResult")
fun ImageView.load(
    string: String?,
    onSuccess: (() -> Unit)?,
    onError: (() -> Unit)?, placeHolder: Int = R.drawable.place_holder,
    anim: Boolean? = false

) {


    val animmmmm = ViewPropertyTransition.Animator { view ->
        view.setAlpha(0f);

        val fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeAnim.setDuration(1000);
        fadeAnim.start();
    }


    class DrawableAlwaysCrossFadeFactory : TransitionFactory<Drawable> {
        private val resourceTransition: DrawableCrossFadeTransition = DrawableCrossFadeTransition(
            1000,
            true
        ) //customize to your own needs or apply a builder pattern

        override fun build(
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Transition<Drawable> {
            return resourceTransition
        }
    }


    fun loadImage(fromCache: Boolean = true) {
        val glide = Glide
            .with(this)
            .applyDefaultRequestOptions(
                RequestOptions()
                    .placeholder(
                        placeHolder*/
/*
                GradientDrawable().apply {
                    orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    colors = intArrayOf(
                        Color.parseColor("#E2E2E2"),
                        Color.parseColor("#B3B3B3"),
                        Color.parseColor("#838383")
                    )
                }
                *//*

                    )

                    .dontTransform()
                    .onlyRetrieveFromCache(fromCache)
            )
            .load(if (string?.isNotBlank() == true) IMAGE_BASE_URL + string else IMAGE_BASE_URL + string)

            // .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onError?.invoke()
                    post {
                        loadImage(false)
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onSuccess?.invoke()
                    return false
                }

            });

        Log.d("SHAKIR", "anim $anim")

        */
/*       if (false) {
                   if (this is RoundedImageView) {
                       glide.transform(RoundedCorners(cornerRadius.toInt()))
                   }
       //            glide.skipMemoryCache(true)
       //            glide.diskCacheStrategy(DiskCacheStrategy.ALL)
                   glide.centerCrop()
                   glide.skipMemoryCache(!fromCache)
                   glide.transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
                   //glide.transition(GenericTransitionOptions.with(animmmmm))
               }*//*

        glide.into(this)
    }



    loadImage(onSuccess != null)


    */
/* Picasso
         .get()
         .load(IMAGE_BASE_URL + string)
         .placeholder(R.drawable.place_holder)
         .error(R.drawable.place_holder)
         .resize(this.context.resources.getDimensionPixelSize(R.dimen._200sdp), 0)
         .into(this, object : Callback {
             override fun onSuccess() {
                 onSuccess?.invoke()
             }

             override fun onError(e: java.lang.Exception?) {
                 onError?.invoke()
             }

         })*//*

}


fun ImageView.loadFlag(*/
/* _100sdp - no place holder - error only*//*

    string: String?

) {
    load(string)
    */
/* Picasso
         .get()
         .load(IMAGE_BASE_URL + string)
         .error(R.drawable.place_holder)
         .resize(this.context.resources.getDimensionPixelSize(R.dimen._100sdp), 0)
         .into(this)*//*

}


val PLACEHOLDER = "PLACEHOLDER"
*/

/*
fun ImageView.loadTrans(
    string: String?,
    transitionName: String?,
    anim: AtomicBoolean? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.transitionName = null
    }


    if (anim?.get() == true) {
        imageView.visibility = View.INVISIBLE
    }

    fun animateImage() {
        if (anim?.get() == true) {
            imageView.apply {

                fun onAnimationEnd() {
                    imageView.visibility = View.VISIBLE
                    imageView.alpha = 1f
                    imageView.post {
                        imageView.load(string)
                    }
                }

                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f)
                    .setDuration(750)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                            Log.d(
                                "SHAKIR",
                                "onAnimationRepeat() called with: animation = [" + animation + "]"
                            );
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            Log.d(
                                "SHAKIR",
                                "onAnimationEnd() called with: animation = [" + animation + "]"
                            );
                            onAnimationEnd()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            Log.d(
                                "SHAKIR",
                                "onAnimationCancel() called with: animation = [" + animation + "]"
                            );
                            onAnimationEnd()
                        }

                        override fun onAnimationStart(animation: Animator?) {
                            Log.d(
                                "SHAKIR",
                                "onAnimationStart() called with: animation = [" + animation + "]"
                            );

                        }

                    })
            }
            anim?.set(false)
        }
    }



    load(
        string,
        onSuccess = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.transitionName = transitionName
            }
            onSuccess?.invoke()
            if (anim?.get() == true) {
                animateImage()
            } else {
                imageView.visibility = View.VISIBLE
                imageView.alpha = 1f
            }


        },
        onError = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.transitionName = transitionName
            }
            onError?.invoke()
            visibility = View.VISIBLE
            anim?.set(false)
        }
        , anim = anim?.get()
    )

    */
/**********
 *
 *  arrayList.clear()
tunerAdapter = TunerAdapter()

var needImageTransition: AtomicBoolean? = AtomicBoolean(true)

alpha 0-1 animation and reload image for cache inside post

recyclerview cache size 25

 *
 *
 *
 *
 *
 *
 * ********//*



}


fun ImageView.load(
    string: String?,
    onSuccess: (() -> Unit)? = null
) {
    load(string, onSuccess, null)
}


fun ImageView.loadProfilePicInCircle(
    string: String?*/
/*,
    onSuccess: (() -> Unit)?,
    onError: (() -> Unit)?*//*

) {
    if (string.isNullOrBlank())
        setImageResource(R.drawable.user_icon)
    else
        load(string, null, null, R.drawable.user_icon)
    */
/* Picasso
         .get()
         .load(IMAGE_BASE_URL + string)
         .placeholder(R.drawable.user_icon)
         .error(R.drawable.user_icon)
         .resize(this.context.resources.getDimensionPixelSize(R.dimen._100sdp), 0)
         .into(this, object : Callback {
             override fun onSuccess() {
                 *//*
*/
/*  onSuccess?.invoke()*//*
*/
/*
                }

                override fun onError(e: java.lang.Exception?) {
                    setImageResource(R.drawable.user_icon)
                }

            })*//*

}
*/


fun logCat(any: Any? = null) {
    Log.d("SSSKKKRRR", "$any")
}

/*
fun EditText.textAlignmentAndFont(fontCopier: EditText? = null) {
    try {
        fontCopier?.typeface?.let { setTypeface(it) }
        if (SharedPreferenceUtils.getLanguageId(this.context) == ARABIC) {
            gravity = Gravity.RIGHT
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}*/


fun EditText?.showKeyBoard() {
    this?.let {
        it.requestFocus()
        val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }
}

fun showKeyBoard(editText: SearchView, context: Context) {
    editText.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}


fun String?.startWithWord(searchKW: String): Boolean {
    if (this == null) return false
    else {
        this.trim().let {
            if (it.startsWith(searchKW.trim(), true))
                return true
            it.split("\\s+".toRegex()).forEach {
                if (it.startsWith(searchKW.trim(), true))
                    return true
            }
        }

    }
    return false
}

/*fun RecyclerView?.addDivider(
    @DrawableRes
    resDrawable: Int = R.drawable.divider_10,
    orientation: Int = DividerItemDecoration.VERTICAL
) {
    if (this != null) {
        val drawable = ContextCompat.getDrawable(context, resDrawable)
        if (drawable != null) {
            addItemDecoration(DividerItemDecoration(context, orientation).apply {
                setDrawable(drawable)
            })
        }

    }
}*/



fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
    putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun SharedPreferences.getDouble(key: String, default: Double) =
    java.lang.Double.longBitsToDouble(
        getLong(
            key,
            java.lang.Double.doubleToRawLongBits(default)
        )
    )


fun Activity?.toast(s: Any?) {
    if (this != null) {
        this.runOnUiThread {
            try {
                Toast.makeText(this, "$s", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
               e.printStackTrace()
            }
        }

    }
}

fun Fragment?.toast(s: Any?) {
    this?.activity?.toast(s)
}


fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun pFlagMutable(flag: Int): Int {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return (PendingIntent.FLAG_MUTABLE or flag)
    } else {
        return flag
    }

}


fun Throwable?.report() {
    if (this != null) {
        try {
            Firebase.crashlytics.recordException(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        this.printStackTrace()
    }
}



































































