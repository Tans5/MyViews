package com.tans.views

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager

import com.tans.views.R

class MaxHeightNestedScrollView : NestedScrollView {

    private var maxHeight: Int? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(context, attrs)
    }

    fun scrollToStart() {
        scrollTo(0, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(if (heightSize < maxHeight ?: 0) heightSize else maxHeight ?: 0, heightMode))
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightNestedScrollView)
        maxHeight = a.getDimension(R.styleable.MaxHeightNestedScrollView_maxHeight, 0f).toInt()
        if (maxHeight == 0) {
            maxHeight = (a.getFloat(R.styleable.MaxHeightNestedScrollView_maxHeightRatio, 1f) * getScreamHeight(context)).toInt()
        }
        a.recycle()
    }

    private fun getScreamHeight(context: Context): Int {
        val wm = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.height
    }
}
