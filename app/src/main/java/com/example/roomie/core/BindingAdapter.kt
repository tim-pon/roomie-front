package com.example.roomie.core

import android.view.View
import android.widget.CheckBox
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.example.roomie.R

@BindingAdapter("isVisible")
fun bindIsGone(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}