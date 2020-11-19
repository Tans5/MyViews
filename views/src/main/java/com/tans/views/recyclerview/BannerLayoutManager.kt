package com.tans.views.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class BannerLayoutManager(context: Context) : androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false) {

    override fun measureChildWithMargins(child: View, widthUsed: Int, heightUsed: Int) {
        val lp = child.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
        val widthSpec = getChildMeasureSpec(this.width / 2,
                this.widthMode,
                this.paddingLeft + this.paddingRight + lp.leftMargin + lp.rightMargin + widthUsed,
                lp.width,
                this.canScrollHorizontally())
        val heightSpec = getChildMeasureSpec(this.height,
                this.heightMode,
                this.paddingTop + this.paddingBottom + lp.topMargin + lp.bottomMargin + heightUsed,
                lp.height,
                this.canScrollVertically())
        child.measure(widthSpec, heightSpec)
    }

}