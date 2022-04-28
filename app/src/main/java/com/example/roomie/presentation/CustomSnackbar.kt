package com.example.roomie.presentation

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.roomie.R
import com.google.android.material.snackbar.Snackbar

/**
 * custom snackbar as singelton
 */
object CustomSnackbar {

    private lateinit var snackbar: Snackbar

    /**
     * predefined constants for snackbar display time
     */
    enum class SnackbarTime {
        LONG, SHORT, INFINIE
    }

    /**
     * predefined constants for snackbar type
     */
    enum class SnackbarType {
        SUCCESS, WARNING, ERROR;
    }

    /**
     * predefined constants for snackbar lyout type
     * there are different layouts in our app where the snackbar hast to appear above the BottomNavigationView
     * and there are other layouts where there is no BottomNavigationView and the snackbar has to appear obove the bottom of the layout
     */
    enum class SnackbarLayoutType {
        WITH_BottomNavigationView, WITHOUT_BottomNavigationView;
    }

    /**
     * show the snackbar
     * @param view  to find a parent from to display itself obove it
     * @param message to display a text
     * @param time to determine the display duration
     * @param type to determine the type of the snackbar
     */
    fun showSnackbar(view: View, message: String, time: SnackbarTime, type: SnackbarType, layoutType: SnackbarLayoutType) {
        /**
         * Definition of the display duration for the snackbar
         */
        val snackbarTime = when (time) {
            SnackbarTime.LONG -> Snackbar.LENGTH_LONG
            SnackbarTime.SHORT -> Snackbar.LENGTH_SHORT
            SnackbarTime.INFINIE -> Snackbar.LENGTH_INDEFINITE
            else -> Snackbar.LENGTH_INDEFINITE
        }

        snackbar = if (layoutType == SnackbarLayoutType.WITH_BottomNavigationView) {
            Snackbar.make(view, "", snackbarTime).setAnchorView(R.id.nav_view)
        } else {
            Snackbar.make(view, "", snackbarTime)
        }

        /**
         * Set button to close snackbar
         */
        snackbar.setAction(
            R.string.snackbar_close,
            View.OnClickListener { // Call your action method here
                snackbar.dismiss()
            })

        val layout: Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbar.setActionTextColor(ContextCompat.getColor(layout.context, R.color.white))


        /**
         * Inflate our custom view
         */
        val snackView: View = View.inflate(layout.context, R.layout.item_snackbar_custom, null)
        // Configure the view
        val imageView = snackView.findViewById<View>(R.id.snackbar_Imageview) as ImageView
        val textViewTop = snackView.findViewById<View>(R.id.snackbar_Textview) as TextView

        /**
         * custom design depending on snackbar type
         */
        when (type) {
            SnackbarType.ERROR -> {
                snackView.setBackgroundColor(ContextCompat.getColor(layout.context, R.color.colorSnackbarError))
                imageView.setImageResource(R.drawable.ic_error)
                textViewTop.text = view.context.getString(R.string.snackbar_error_message, message)
            }
            SnackbarType.SUCCESS -> {
                snackView.setBackgroundColor(ContextCompat.getColor(layout.context, R.color.colorSnackbarSuccess))
                imageView.setImageResource(R.drawable.ic_success)
                textViewTop.text = view.context.getString(R.string.snackbar_success_message, message)
            }
            SnackbarType.WARNING -> {
                snackView.setBackgroundColor(ContextCompat.getColor(layout.context, R.color.colorSnackbarWarning))
                imageView.setImageResource(R.drawable.ic_warning)
                textViewTop.text = view.context.getString(R.string.snackbar_warning_message, message)
            }
            else -> {
                snackView.setBackgroundColor(ContextCompat.getColor(layout.context, R.color.colorSnackbarError))
                imageView.setImageResource(R.drawable.ic_error)
                textViewTop.text = view.context.getString(R.string.snackbar_error_message, message)
            }
        }
        /**
         * to cover the whole snackbar layout
         */
        layout.setPadding(0, 0, 0, 0)

        /**
         * Add the view to the Snackbar's layout
         */
        layout.addView(snackView, 0)

        snackbar.show()
    }

    /**
     * Default error messages for standard http errors
     */
    fun defaultError(view: View, code: Int? = -1) {
        val message = when (code) {
            // client
            401 -> view.context.getString(R.string.error_401)
            403 -> view.context.getString(R.string.error_403)
            404 -> view.context.getString(R.string.error_404)
            405 -> view.context.getString(R.string.error_405)
            408 -> view.context.getString(R.string.error_408)
            // server
            500 -> view.context.getString(R.string.error_500)
            503 -> view.context.getString(R.string.error_503)
            // other
            else -> view.context.getString(R.string.error_unknown)
        }
        showError(view, message)
    }

    /**
     * Custom error messages
     */
    fun showError(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.nav_view)
            .show()
    }

}