package com.tans.views.recyclerview

import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.view.View

class BannerSnapHelper : LinearSnapHelper() {

    private var orientationHelper: OrientationHelper? = null

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val start = orientationHelper?.startAfterPadding ?: 0
        val dLeft = orientationHelper?.getDecoratedStart(targetView) ?: 0
        return arrayOf(dLeft - start, 0).toIntArray()
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (orientationHelper == null) {
            orientationHelper = OrientationHelper.createHorizontalHelper(layoutManager)
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