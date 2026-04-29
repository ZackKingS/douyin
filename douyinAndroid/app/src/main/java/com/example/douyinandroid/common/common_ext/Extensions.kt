package com.example.douyinandroid.common.common_ext

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

// Toast extension
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(message, duration)
}

// Date extensions
fun String.parseDate(pattern: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun Date.formatDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

fun Long.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

// String extensions
fun String?.orEmpty(default: String = ""): String {
    return this ?: default
}

fun String?.orBlank(default: String? = null): String? {
    return if (this.isNullOrBlank()) default else this
}

// Number extensions
fun Long.formatCount(): String {
    return when {
        this >= 1_0000_0000 -> String.format("%.1f亿", this / 1_0000_0000.0)
        this >= 1_0000 -> String.format("%.1f万", this / 1_0000.0)
        else -> this.toString()
    }
}

// View extensions
// (Already using ViewBinding, no need for view extensions)

// Intent extensions
// (Using ARouter for navigation)
