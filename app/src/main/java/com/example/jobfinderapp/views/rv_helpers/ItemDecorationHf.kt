package com.example.jobfinderapp.views.rv_helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecorationHf(private val paddingMain: Int, private val paddingSide: Int) : RecyclerView.ItemDecoration() {

    //Делаю отступы для RV

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.left = paddingSide
        outRect.right = paddingSide
        outRect.bottom = paddingMain
        outRect.top = paddingMain
    }
}