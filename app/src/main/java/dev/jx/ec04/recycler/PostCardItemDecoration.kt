package dev.jx.ec04.recycler

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PostCardItemDecoration(
    private val horizontalSpacing: Int,
    private val bottomSpacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = horizontalSpacing
        outRect.left = horizontalSpacing
        outRect.bottom = bottomSpacing
    }
}