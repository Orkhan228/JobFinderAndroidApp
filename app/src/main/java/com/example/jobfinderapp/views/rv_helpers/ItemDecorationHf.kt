package com.example.jobfinderapp.views.rv_helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecorationHf(private val paddingMain: Int) : RecyclerView.ItemDecoration() {

    //Делаю отступы для RV

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.left = paddingMain
        outRect.right = paddingMain
        outRect.bottom = paddingMain
        outRect.top = paddingMain
    }
}