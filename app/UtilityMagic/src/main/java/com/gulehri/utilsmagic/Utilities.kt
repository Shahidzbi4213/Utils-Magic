package com.gulehri.utilsmagic

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder
import com.gulehri.utilsmagic.databinding.ProgressDesignBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Created by Shahid Iqbal on 07,April,2022
 */
class Utilities() {


    /*Internet Checking Block*/
    object Internet {
        @RequiresApi(Build.VERSION_CODES.M)
        fun isConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        }
    }


    /*Random Password and Tokens*/
    object Random {
        /*This Method Generates Random Token of 64 AlphaNumeric Characters*/
        fun generateToken(): String {
            val token = UUID.randomUUID().toString().replace("-", "")
            return token + token.reversed()
        }
    }


    /*File Size and Extensions*/
    object FileUtility {
        /*This method will give file size*/
        fun fileSize(context: Context, uri: Uri): Long {
            return File(PathFinder.getFilePath(context, uri).toString()).length()
        }

        /*This method provides extension of file*/
        fun fileExtension(uri: Uri, context: Context): String {
            val resolver = context.contentResolver as ContentResolver
            val mime = MimeTypeMap.getSingleton()
            return mime.getExtensionFromMimeType(resolver.getType(uri)).toString()
        }
    }


    /*Progress Dialog*/
    object ProgressCard {

        /*This method create Progress Dialog */
        fun createProgressDialog(
            context: Context,
            title: String? = "Loading",
            message: String? = "Please Wait"
        ): AlertDialog {
            val binding = ProgressDesignBinding.inflate(LayoutInflater.from(context))
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setView(binding.root)
            builder.setCancelable(false)
            val dialog = builder.create()
            binding.pTitle.text = title
            binding.tvMessage.text = message
            return dialog
        }
    }

    /*This block will give list of all countries in the world*/
    object Countries {
        data class Country(val countryName: String, val countryCode: String)

        fun countriesList(): List<Country> {
            val temp = arrayListOf<Country>()
            Locale.getISOCountries().forEach {
                it?.let {
                    temp.add(Country(Locale("", it).displayCountry, it))
                }

            }
            return temp.sortedBy { it.countryName }
        }
    }

    /*This block will give you original file path from Uri*/
    object PathFinder {
        @SuppressLint("NewApi")
        fun getFilePath(context: Context, uri: Uri): String? {
            val selection: String?
            val selectionArgs: Array<String>?
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    val fullPath = getPathFromExtSD(split)
                    return if (fullPath !== "") {
                        fullPath
                    } else {
                        null
                    }
                } else if (isDownloadsDocument(uri)) {
                    context.contentResolver.query(
                        uri,
                        arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    ).use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            val fileName = cursor.getString(0)
                            val path = Environment.getExternalStorageDirectory()
                                .toString() + "/Download/" + fileName
                            if (!TextUtils.isEmpty(path)) {
                                return path
                            }
                        }
                    }
                    val id: String = DocumentsContract.getDocumentId(uri)
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                        )
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            return try {
                                val contentUri = ContentUris.withAppendedId(
                                    Uri.parse(contentUriPrefix),
                                    java.lang.Long.valueOf(id)
                                )
                                getDataColumn(context, contentUri, null, null)
                            } catch (e: NumberFormatException) {
                                //In Android 8 and Android P the id is not a number
                                uri.path!!.replaceFirst("^/document/raw:".toRegex(), "")
                                    .replaceFirst("^raw:".toRegex(), "")
                            }
                        }
                    }
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                    return getDataColumn(
                        context, contentUri, selection,
                        selectionArgs
                    )
                } else if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri, context)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri, context)
                } else {
                    getDataColumn(context, uri, null, null)
                }
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private fun getPathFromExtSD(pathData: Array<String>): String {
            val type = pathData[0]
            val relativePath = "/" + pathData[1]
            var fullPath = ""
            if ("primary".equals(type, ignoreCase = true)) {
                fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }
            fullPath = System.getenv("SECONDARY_STORAGE")!! + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
            fullPath = System.getenv("EXTERNAL_STORAGE")!! + relativePath
            return if (fileExists(fullPath)) {
                fullPath
            } else fullPath
        }

        @SuppressLint("Recycle")
        private fun getDriveFilePath(uri: Uri, context: Context): String {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.getLong(sizeIndex).toString()
            val file = File(context.cacheDir, name)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read: Int
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream!!.available()

                //int bufferSize = 1024;
                val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream.close()
                outputStream.close()
            } catch (e: Exception) {
                Log.e("Exception", e.message!!)
            }
            return file.path
        }

        private fun getDataColumn(
            context: Context, uri: Uri?,
            selection: String?, selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection,
                    selection, selectionArgs, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        private fun isGoogleDriveUri(uri: Uri): Boolean {
            return "com.google.android.apps.docs.storage" == uri.authority ||
                    "com.google.android.apps.docs.storage.legacy" == uri.authority
        }
    }

    /*This block will help you with keyboard overlapped problem*/
    object KeyboardAssist {
        class SoftInputAssist(activity: Activity) {
            private var rootView: View?
            private var contentContainer: ViewGroup?
            private var viewTreeObserver: ViewTreeObserver? = null
            private val listener = OnGlobalLayoutListener { possiblyResizeChildOfContent() }
            private val contentAreaOfWindowBounds: Rect = Rect()
            private val rootViewLayout: FrameLayout.LayoutParams
            private var usableHeightPrevious = 0
            fun onPause() {
                if (viewTreeObserver!!.isAlive) {
                    viewTreeObserver!!.removeOnGlobalLayoutListener(listener)
                }
            }

            fun onResume() {
                if (viewTreeObserver == null || !viewTreeObserver!!.isAlive) {
                    viewTreeObserver = rootView!!.viewTreeObserver
                }
                viewTreeObserver!!.addOnGlobalLayoutListener(listener)
            }

            fun onDestroy() {
                rootView = null
                contentContainer = null
                viewTreeObserver = null
            }

            private fun possiblyResizeChildOfContent() {
                contentContainer!!.getWindowVisibleDisplayFrame(contentAreaOfWindowBounds)
                val usableHeightNow: Int = contentAreaOfWindowBounds.height()
                if (usableHeightNow != usableHeightPrevious) {
                    rootViewLayout.height = usableHeightNow
                    rootView?.layout(
                        contentAreaOfWindowBounds.left,
                        contentAreaOfWindowBounds.top,
                        contentAreaOfWindowBounds.right,
                        contentAreaOfWindowBounds.bottom
                    )
                    rootView?.requestLayout()
                    usableHeightPrevious = usableHeightNow
                }
            }

            init {
                contentContainer = activity.findViewById<View>(R.id.content) as ViewGroup
                rootView = contentContainer!!.getChildAt(0)
                rootViewLayout = rootView?.layoutParams as FrameLayout.LayoutParams
            }
        }
    }

    /*This block give location information based on coordinates*/
    object AreaElement {
        data class AreaAddress(
            val addressLine: String?,
            val locality: String?,
            val countryName: String?,
            val postalCode: String?
        )

        fun getAddress(context: Context, latitude: Double, longitude: Double): List<AreaAddress> {
            val geocoder = Geocoder(context, Locale.getDefault())
            val temp = arrayListOf<AreaAddress>()
            try {

                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                temp.add(
                    AreaAddress(
                        addresses[0].getAddressLine(0),
                        addresses[0].locality,
                        addresses[0].countryName,
                        addresses[0].postalCode
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return temp
        }
    }

    /*Networking Retrofit*/
    object Network {

        fun getRetrofit(url: String): Retrofit {
            return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder().setLenient().create()
                    )
                )
                .build()
        }
    }

    /*This Block Provide Date Picker Dialog and set Date to field*/
    object DateDialog {

        @RequiresApi(Build.VERSION_CODES.O)
        fun datePicker(context: Context, view: View?, date: String?): DatePickerDialog {
            val listener =
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, monthOfYear)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val fieldOfDate = view as TextView
                    fieldOfDate.text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(c.time).toString()
                }
            val year: Int
            val month: Int
            val day: Int

            if (date != null && date.isNotEmpty()) {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
                val realDate = LocalDate.parse(date, formatter)
                year = realDate.year
                month = realDate.monthValue - 1
                day = realDate.dayOfMonth
            } else {
                val calender = Calendar.getInstance()
                year = calender.get(Calendar.YEAR)
                month = calender.get(Calendar.MONTH)
                day = calender.get(Calendar.DAY_OF_MONTH)
            }

            return DatePickerDialog(context, listener, year, month, day)
        }
    }

    /*Intents for Different Picker*/
    object Intenter {
        fun pickFile(): Intent {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/*"
            return intent

        }

        fun pickImage(): Intent {
            val intent = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "img/*"
            return intent

        }

        fun pickAudio(): Intent {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            return intent

        }

        fun pickVideo(): Intent {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/*"
            return intent

        }


    }

    /*Set Margins to view*/
    object Margins {
        fun View.setMargins(
            left: Float? = null,
            top: Float? = null,
            right: Float? = null,
            bottom: Float? = null
        ) {
            layoutParams<ViewGroup.MarginLayoutParams> {
                left?.run { leftMargin = dpToPx(this) }
                top?.run { topMargin = dpToPx(this) }
                right?.run { rightMargin = dpToPx(this) }
                bottom?.run { bottomMargin = dpToPx(this) }
            }
        }

        private inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
            if (layoutParams is T) block(layoutParams as T)
        }

        private fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
        private fun Context.dpToPx(dp: Float): Int =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
                .toInt()
    }

    /*Styling of Views*/
    object Customization {


        @RequiresApi(Build.VERSION_CODES.M)
        fun customizeText(
            view: View? = null,
            fontSize: Float? = null,
            textColor: String? = null,
            position: Int? = null,
            fontFamily: Int? = null,
            elevation: Float? = null,
            context: Context? = null,
            isRegular: Boolean = false,
            isBold: Boolean = false,
            isItalic: Boolean = false,
            isBoldItalic: Boolean = false,
            isRegularItalic: Boolean = false
        ) {
            view?.let { it ->

                elevation?.let { e -> it.elevation = e }
                fontSize?.let { size ->

                    if (it is EditText) it.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
                    if (it is TextView) it.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
                }

                textColor?.let { color ->
                    if (it is EditText) it.setTextColor(Color.parseColor(color))
                    if (it is TextView) it.setTextColor(Color.parseColor(color))

                }

                position?.let { gravity ->
                    if (it is EditText) it.gravity = gravity
                    if (it is TextView) it.gravity = gravity

                }

                if (it is TextView) {
                    when {
                        isRegular -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.Regular)
                        isBold -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.Bold)
                        isItalic -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.Italic)
                        isBoldItalic -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.BoldWithItalic)
                        isRegularItalic -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.NormalWithItalic)
                    }

                }
                if (it is EditText) {
                    when {
                        isRegular -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.Regular)
                        isBold -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.Bold)
                        isItalic -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.Italic)
                        isBoldItalic -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.BoldWithItalic)
                        isRegularItalic -> it.setTextAppearance(com.gulehri.utilsmagic.R.style.NormalWithItalic)
                    }
                }

                fontFamily?.let { family ->
                    if (it is EditText) it.typeface = ResourcesCompat.getFont(context!!, family)
                    if (it is TextView) it.typeface = ResourcesCompat.getFont(context!!, family)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun customizeButton(
            context: Context,
            view: Button? = null,
            backgroundColor: String? = null,
            textColor: String? = null,
            position: Int? = null,
            fontSize: Float? = null,
            fontWeight: Int? = null,
            rounded: Boolean = false,
            elevation: Float? = null,
            isRegular: Boolean = false,
            isBold: Boolean = false,
            isItalic: Boolean = false,
            isBoldItalic: Boolean = false,
            isRegularItalic: Boolean = false

        ) {
            view?.let { button ->

                elevation?.let { button.elevation = it }

                backgroundColor?.let {
                    button.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it))
                }

                position?.let {
                    button.gravity = position
                }

                textColor?.let {
                    button.setTextColor(Color.parseColor(it))
                }

                fontSize?.let {
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }

                fontWeight?.let {
                    button.setTypeface(button.typeface, it)
                }

                if (rounded)
                    button.background = ContextCompat.getDrawable(
                        context, com.gulehri.utilsmagic.R.drawable.round_view
                    )

                when {
                    isRegular -> button.setTextAppearance(com.gulehri.utilsmagic.R.style.Regular)
                    isBold -> button.setTextAppearance(com.gulehri.utilsmagic.R.style.Bold)
                    isItalic -> button.setTextAppearance(com.gulehri.utilsmagic.R.style.Italic)
                    isBoldItalic -> button.setTextAppearance(com.gulehri.utilsmagic.R.style.BoldWithItalic)
                    isRegularItalic -> button.setTextAppearance(com.gulehri.utilsmagic.R.style.NormalWithItalic)
                }

            }
        }

        fun customizeView(
            context: Context,
            view: View? = null,
            backgroundDrawable: Int? = null,
            backgroundColor: String? = null,
            elevation: Float? = null
        ) {
            view?.let { v ->
                elevation?.let { v.elevation = it }
                backgroundDrawable?.let {
                    v.background = ContextCompat.getDrawable(context, it)
                }
                backgroundColor?.let {
                    v.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it))
                }
            }
        }

        fun customizeImageView(
            context: Context,
            view: ImageView? = null,
            id: Int? = null,
            drawable: Drawable? = null,
            bitmap: Bitmap? = null,
            path: String? = null,
            rounded: Boolean = false,
            elevation: Float? = null
        ) {

            view?.let { imageView ->

                elevation?.let { imageView.elevation = it }

                if (rounded) {
                    id?.let {
                        Glide.with(context).load(id).apply(RequestOptions.circleCropTransform())
                            .fitCenter()
                            .into(imageView)
                    }
                    drawable?.let {
                        Glide.with(context).load(drawable)
                            .apply(RequestOptions.circleCropTransform())
                            .fitCenter()
                            .into(imageView)
                    }
                    bitmap?.let {
                        Glide.with(context).load(bitmap).apply(RequestOptions.circleCropTransform())
                            .fitCenter()
                            .into(imageView)
                    }
                    path?.let {
                        Glide.with(context).load(path).apply(RequestOptions.circleCropTransform())
                            .fitCenter()
                            .into(imageView)
                    }
                } else {
                    id?.let {
                        Glide.with(context).load(id)
                            .fitCenter()
                            .into(imageView)
                    }
                    drawable?.let {
                        Glide.with(context).load(drawable).fitCenter().into(imageView)
                    }
                    bitmap?.let {
                        Glide.with(context).load(bitmap)
                            .fitCenter()
                            .into(imageView)
                    }
                    path?.let {
                        Glide.with(context).load(path)
                            .fitCenter()
                            .into(imageView)
                    }
                }
            }

        }

        fun customizeCard(
            view: CardView? = null,
            backgroundColor: String? = null,
            rounded: Float? = null,
            compatPadding: Boolean = false,
            elevation: Float? = null
        ) {
            view?.let { card ->
                card.setCardBackgroundColor(Color.parseColor(backgroundColor))
                elevation?.let { card.cardElevation = it }
                card.useCompatPadding = compatPadding
                rounded?.let { card.radius = it }
            }
        }
    }
}

