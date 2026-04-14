package com.example.jobfinderapp.views.rv_helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecReminderRv(private val paddingTop: Int, private val paddingBottom: Int, private val paddingSides: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.left = paddingSides
        outRect.right = paddingSides
        outRect.top = paddingTop
        outRect.bottom = paddingBottom
    }
}