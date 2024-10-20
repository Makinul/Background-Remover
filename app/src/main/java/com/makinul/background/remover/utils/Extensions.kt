package com.makinul.background.remover.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A collection of extension functions and properties to simplify common operations on various Android UI elements and data types.
 */
object Extensions {

    /**
     * Sets the visibility of the [View] to [View.GONE].
     */
    fun View.gone() = run { visibility = View.GONE }

    /**
     * Sets the visibility of the [View] to [View.VISIBLE].
     */
    fun View.visible() = run { visibility = View.VISIBLE }

    /**
     * Sets the visibility of the [View] to [View.INVISIBLE].
     */
    fun View.invisible() = run { visibility = View.INVISIBLE }

    /**
     * Property extension to parse the text of an [EditText] as a [Double], or return 0.0 if parsing fails.
     */
    val EditText.doubleValue get() = this.text.toString().toDoubleOrNull() ?: 0.0

    /**
     * Property extension to parse the text of an [EditText] as an [Int], or return 0 if parsing fails.
     */
    val EditText.intValue get() = this.text.toString().toIntOrNull() ?: 0

    /**
     * Property extension to get the text of an [EditText] as a [String].
     */
    val EditText.value get() = this.text?.toString() ?: ""

    /**
     * Compares a [String] with another [String]. Returns true if the receiver is not empty and is contained within the other string.
     *
     * @param s The [String] to compare with.
     */
    fun String.compare(s: String): Boolean {
        return this.isNotEmpty() && s.contains(this)
    }

    /**
     * Starts an activity from an [Activity] with optional parameters for customization.
     *
     * @param cls The target [Activity] class.
     * @param finishThis If true, finish the current [Activity] after starting the new one.
     * @param block A lambda for customizing the [Intent] before starting the activity.
     */
    fun Activity.startActivity(
        cls: Class<*>,
        finishThis: Boolean = false,
        block: (Intent.() -> Unit)? = null
    ) {
        val intent = Intent(this, cls)
        block?.invoke(intent)
        startActivity(intent)
        if (finishThis) finish()
    }

    /**
     * Starts an activity from a [Fragment] with optional parameters for customization.
     *
     * @param cls The target [Activity] class.
     * @param finishThis If true, finish the current [Activity] after starting the new one.
     * @param block A lambda for customizing the [Intent] before starting the activity.
     */
    fun Fragment.startActivity(
        cls: Class<*>,
        finishThis: Boolean = false,
        block: (Intent.() -> Unit)? = null
    ) {
        requireActivity().startActivity(cls, finishThis, block)
    }

    /**
     * Displays a toast message in the context of the [Fragment].
     *
     * @param message The message to display.
     */
    fun Fragment.toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Displays a toast message in the context of the [Fragment] using a string resource.
     *
     * @param message The resource ID of the message to display.
     */
    fun Fragment.toast(@StringRes message: Int) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Displays a toast message in the context of the [Activity].
     *
     * @param message The message to display.
     */
    fun Activity.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Displays a toast message in the context of the [Activity] using a string resource.
     *
     * @param message The resource ID of the message to display.
     */
    fun Activity.toast(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Shows the soft keyboard for the [View].
     */
    fun View.showSoftKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * Hides the soft keyboard associated with the [View].
     */
    fun View.hideSoftKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Hides the soft keyboard associated with the [Activity].
     */
    fun Activity.hideSoftKeyboard() {
        val view = currentFocus ?: View(this)
        view.hideSoftKeyboard()
    }

    /**
     * Sets a custom error message and optionally requests focus and shows the soft input keyboard for a [TextInputEditText].
     *
     * @param errorMsg The error message to set.
     * @param requestFocus If true, request focus for the [TextInputEditText].
     * @param showSoftInput If true, show the soft input keyboard for the [TextInputEditText].
     */
    fun TextInputEditText.setCustomError(
        errorMsg: String,
        requestFocus: Boolean = false,
        showSoftInput: Boolean = false
    ) {
        this.error = errorMsg
        if (requestFocus) this.requestFocus()
        if (showSoftInput) this.showSoftKeyboard()
    }

    /**
     * Calculates the standard deviation (STDEV.S) for a list of [Double] values.
     *
     * @return The calculated standard deviation.
     */
    fun ArrayList<Double>.STDEV_S(): Double {
        var sumOfDifference = 0.0
        val mean = average()
        forEach {
            sumOfDifference += (it - mean).pow(2)
        }
        val n = size - 1
        return sqrt(sumOfDifference / n)
    }

    /**
     * Formats a [Double] using the specified decimal format string.
     *
     * @param format The decimal format string to use.
     * @return The formatted [Double] as a [String].
     */
    fun Double.formatDecimal(format: String): String {
        return DecimalFormat(format).format(this)
    }

    /**
     * Formats a [Double] with the default "#.##" decimal format string.
     *
     * @return The formatted [Double] as a [String].
     */
    fun Double.formatDecimal(): String {
        return DecimalFormat("#.##").format(this)
    }

    /**
     * Formats a [Double] with the default "#.##" decimal format string.
     *
     * @return The formatted [Double] as a [String].
     */
    fun Double.formatInt(): String {
        return DecimalFormat("#").format(this)
    }

    /**
     * Converts square millimeters to square meters.
     *
     * @return The converted value in square meters.
     */
    fun Double.squareMMToSquareM(): Double {
        return this * (10.0).pow(-6)
    }

    /**
     * Calculates the coefficient of variation (CV) for a value relative to an average.
     *
     * @param avg The average value.
     * @return The calculated coefficient of variation.
     */
    fun Double.cv(avg: Double): Double {
        return this / avg * 100
    }

    /**
     * Retrieves a formatted [String] resource with a single parameter.
     *
     * @param resourceId The resource ID of the formatted [String].
     * @param param The parameter to insert into the formatted [String].
     * @return The formatted [String].
     */
    fun ViewHolder.getString(@StringRes resourceId: Int, param: String): String {
        return this.itemView.context.getString(resourceId, param)
    }

    /**
     * Retrieves a formatted [String] resource with two parameters.
     *
     * @param resourceId The resource ID of the formatted [String].
     * @param param1 The first parameter to insert into the formatted [String].
     * @param param2 The second parameter to insert into the formatted [String].
     * @return The formatted [String].
     */
    fun ViewHolder.getString(@StringRes resourceId: Int, param1: String, param2: String): String {
        return this.itemView.context.getString(resourceId, param1, param2)
    }

    /**
     * Sets the text of a [TextView] using a formatted [String] resource with two [Double] values.
     *
     * @param resourceId The resource ID of the formatted [String].
     * @param value1 The first [Double] value to insert into the formatted [String].
     * @param value2 The second [Double] value to insert into the formatted [String].
     */
    fun TextView.text(@StringRes resourceId: Int, value1: Double, value2: Double) {
        this.text = context.getString(resourceId, value1.formatDecimal(), value2.formatDecimal())
    }

    /**
     * Sets the text of a [TextView] using a formatted [String] resource with two [String] values.
     *
     * @param resourceId The resource ID of the formatted [String].
     * @param value1 The first [String] value to insert into the formatted [String].
     * @param value2 The second [String] value to insert into the formatted [String].
     */
    fun TextView.text(@StringRes resourceId: Int, value1: String, value2: String) {
        this.text = context.getString(resourceId, value1, value2)
    }

    /**
     * Sets the text of a [TextView] using a formatted [String] resource with a single [Double] value.
     *
     * @param resourceId The resource ID of the formatted [String].
     * @param value The [Double] value to insert into the formatted [String].
     */
    fun TextView.text(@StringRes resourceId: Int, value: Double) {
        this.text = context.getString(resourceId, value.formatDecimal())
    }

    /**
     * Sets the text of a [TextView] using a formatted [String] resource with a single [String] value.
     *
     * @param resourceId The resource ID of the formatted [String].
     * @param value The [String] value to insert into the formatted [String].
     */
    fun TextView.text(@StringRes resourceId: Int, value: String) {
        this.text = context.getString(resourceId, value)
    }

    /**
     * Sets the text of a [TextView] using a simple [String] resource.
     *
     * @param resourceId The resource ID of the [String].
     */
    fun TextView.text(@StringRes resourceId: Int) {
        this.text = context.getString(resourceId)
    }

    fun Int.toAlphaColor(): Int {
        return Color.argb(
            128,
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    }
}
