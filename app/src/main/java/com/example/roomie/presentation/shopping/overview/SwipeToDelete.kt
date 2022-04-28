package com.example.roomie.presentation.shopping.overview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.R

class SwipeToDelete(
    context: Context,
    private val onSwipeCallback: (Int) -> Unit,
) : ItemTouchHelper.SimpleCallback(
    0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    lateinit var onSwipe2Callback: (Int) -> Unit
    constructor(
        context: Context,
        onSwipeCallback: (Int) -> Unit,
        onSwipe2Callback: (Int) -> Unit,
    ): this(context,onSwipeCallback) {
        this.onSwipe2Callback = onSwipe2Callback
    }

    private val delIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val delBackground = ColorDrawable(context.getColor(R.color.swipe_to_delete_background))

    private val transactionIcon = ContextCompat.getDrawable(context, R.drawable.botnav_item_finance_normal)
    private val transactionBackground = ColorDrawable(context.getColor(R.color.swipe_to_create_transaction_background))

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
        when (direction) {
            ItemTouchHelper.LEFT -> onSwipeCallback(viewHolder.adapterPosition)
            ItemTouchHelper.RIGHT -> onSwipe2Callback(viewHolder.adapterPosition)
            else -> {}
        }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20
        val iconMargin = (itemView.height - delIcon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - delIcon.intrinsicHeight) / 2
        val iconBottom = iconTop + delIcon.intrinsicHeight

        if (dX > 0) { // Swiping to the right
            val iconLeft: Int = itemView.left + iconMargin
            val iconRight: Int = itemView.left + iconMargin + transactionIcon!!.intrinsicWidth
            transactionIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            transactionBackground.setBounds(itemView.left, itemView.top,
                itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom)
            transactionBackground.draw(c)
            transactionIcon.draw(c)

        }
        if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - delIcon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            delIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            delBackground.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top, itemView.right, itemView.bottom)

            delBackground.draw(c)
            delIcon.draw(c)
        }
        else { // view is unSwiped
            delBackground.setBounds(0,0,0,0)
        }
    }
}