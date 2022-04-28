package com.example.roomie.presentation.home.activity

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.example.roomie.R
import com.example.roomie.domain.model.Activity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ActivityMessages {

    /**
     * gets the icon of the activity category (finance, shopping, flat)
     */
    fun getCategoryItem(activity: Activity, context: Context): Drawable? {
        return when (activity.activity) {
            "ADD_FLAT", "JOIN_FLAT" -> ResourcesCompat.getDrawable(context.resources,
                R.drawable.selector_home,
                context.theme)
            "ADD_LIST", "ADD_ARTICLE" -> ResourcesCompat.getDrawable(context.resources,
                R.drawable.selector_shopping,
                context.theme)
            "ADD_TRANSACTION", "PAY_TRANSACTIONS" -> ResourcesCompat.getDrawable(context.resources,
                R.drawable.selector_finance,
                context.theme)
            else -> ResourcesCompat.getDrawable(context.resources,
                R.drawable.ic_warning,
                context.theme)
        }
    }

    /**
     * get the text which describes the activity, including username of the user who started the
     * activity and a reference
     */
    fun getActivityItemDescription(activity: Activity, context: Context): String {
        val descrBeginning: String
        val descrEnd: String
        when (activity.activity) {
            "ADD_FLAT" -> {
                descrBeginning =
                    context.resources.getString(R.string.activity_add_flat_beginning)
                descrEnd = context.resources.getString(R.string.activity_add_flat_end)
            }
            "JOIN_FLAT" -> {
                descrBeginning =
                    context.resources.getString(R.string.activity_join_flat_beginning)
                descrEnd = context.resources.getString(R.string.activity_join_flat_end)
            }
            "ADD_LIST" -> {
                descrBeginning =
                    context.resources.getString(R.string.activity_add_list_beginning)
                descrEnd = context.resources.getString(R.string.activity_add_list_end)
            }
            "ADD_ARTICLE" -> {
                descrBeginning =
                    context.resources.getString(R.string.activity_add_article_beginning)
                descrEnd = context.resources.getString(R.string.activity_add_article_end)
            }
            "ADD_TRANSACTION" -> {
                descrBeginning =
                    context.resources.getString(R.string.activity_add_transaction_beginning)
                descrEnd = context.resources.getString(R.string.activity_add_transaction_end)
            }
            "PAY_TRANSACTIONS" -> {
                descrBeginning =
                    context.resources.getString(R.string.activity_pay_transactions_beginning)
                descrEnd = ""
            }
            else -> {
                descrBeginning = ""
                descrEnd = ""
            }
        }

        return "${activity.username} $descrBeginning ${activity.referenceName} $descrEnd"
    }

    /**
     * parses activity.createdOn to the system time zone and formats the time depending on the
     * systems language
     */
    fun getTimeForSystemTimeZone(activity: Activity, context: Context): String {
        val time = Instant.parse(activity.createdOn).atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern(context.resources
            .getString(R.string.activity_time_format))
        return time.format(formatter)
    }
}