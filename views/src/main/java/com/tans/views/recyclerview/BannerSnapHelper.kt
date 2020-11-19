package com.tans.views.recyclerview

import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class BannerSnapHelper : androidx.recyclerview.widget.LinearSnapHelper() {

    private var orientationHelper: androidx.recyclerview.widget.OrientationHelper? = null

    override fun calculateDistanceToFinalSnap(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager, targetView: View): IntArray? {
        val start = orientationHelper?.startAfterPadding ?: 0
        val dLeft = orientationHelper?.getDecoratedStart(targetView) ?: 0
        return arrayOf(dLeft - start, 0).toIntArray()
    }

    override fun findSnapView(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager): View? {
        if (orientationHelper == null) {
            orientationHelper = androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper(layoutManager)
        }
        val start = orientationHelper?.startAfterPadding ?: 0
        val childCount = layoutManager.childCount
        var closestChild: View? = null
        var closestDst: Int = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val view = layoutManager.getChildAt(i)
            val toStartDst =
                    if (view != null) {
                        val dLeft = orientationHelper?.getDecoratedStart(view) ?: 0
                        Math.abs(start - dLeft)
                    } else {
                        0
                    }
            if (closestDst > toStartDst) {
                closestChild = view
                closestDst = toStartDst
            }
        }
        return closestChild
    }

}