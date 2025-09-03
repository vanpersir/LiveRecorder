package com.ds.liverecorder.ui.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) = with(outRect) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column

        if (includeEdge) {
            left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
            right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                top = spacing
            }
            bottom = spacing // item bottom
        } else {
            left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
            right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f / spanCount) * spacing)
            if (position >= spanCount) {
                top = spacing // item top
            }
        }
    }
}