package com.tans.views

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

class PopularKeyWordLayout : ViewGroup {

    private val childrenPositions = ArrayList<ChildViewPosition>()

    private val childrenViews = ArrayList<View>()

    private var parentMeasuredWidth = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in childrenPositions.indices) {
            getChildAt(i).layout(childrenPositions[i].left, childrenPositions[i].top,
                    childrenPositions[i].right, childrenPositions[i].bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        childrenViews.clear()
        childrenPositions.clear()
        parentMeasuredWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
            childrenViews.add(childView)
        }
        val size = calculateSize()
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(size.first, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(size.second, View.MeasureSpec.EXACTLY))
    }

    private fun calculateSize(): Pair<Int, Int> {
        var measureResult = calculateChildrenPosition(parentMeasuredWidth)
        if (measureResult.lines > 2) {
            var allChildrenLen = 0
            var crisisWidth = 0
            childrenPositions.clear()
            for (child in childrenViews) {
                val lp = child.getLayoutParams()
                var cMarginLeft = 0
                var cMarginRight = 0
                if (lp is LayoutParams) {
                    cMarginLeft = (lp as LayoutParams).leftMargin
                    cMarginRight = (lp as LayoutParams).rightMargin
                }
                allChildrenLen += child.measuredWidth + cMarginLeft + cMarginRight
            }

            for (child in childrenViews) {
                val lp = child.layoutParams
                var cMarginLeft = 0
                var cMarginRight = 0
                if (lp is LayoutParams) {
                    cMarginLeft = (lp as LayoutParams).leftMargin
                    cMarginRight = (lp as LayoutParams).rightMargin
                }
                crisisWidth += cMarginLeft + cMarginRight + child.getMeasuredWidth()
                if (crisisWidth * 2 >= allChildrenLen) {
                    break
                }
            }
            measureResult = calculateChildrenPosition(crisisWidth)
        }
        childrenPositions.addAll(measureResult.childrenPositions!!)
        return Pair(measureResult.calculatedWidth, measureResult.calculatedHeight)
    }

    private fun calculateChildrenPosition(maxLen: Int): MeasureResult {
        val measureResult = MeasureResult()
        var lines = 0
        var calculatedHeight = 0
        var lineWidth = 0
        val childrenPositions = ArrayList<ChildViewPosition>()
        for (child in childrenViews) {
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            var cMarginLeft = 0
            var cMarginRight = 0
            var cMarginTop = 0
            var cMarginBottom = 0
            if (child.getLayoutParams() is LayoutParams) {
                val lp = child.getLayoutParams() as LayoutParams
                cMarginLeft = lp.leftMargin
                cMarginRight = lp.rightMargin
                cMarginTop = lp.topMargin
                cMarginBottom = lp.bottomMargin
            }
            if (lineWidth + childWidth + cMarginLeft + cMarginRight <= maxLen) {
                val position = ChildViewPosition()
                position.left = cMarginLeft + lineWidth
                position.top = cMarginTop + calculatedHeight
                position.right = cMarginLeft + lineWidth + childWidth
                position.bottom = cMarginTop + calculatedHeight + childHeight
                childrenPositions.add(position)
                lineWidth += childWidth + cMarginLeft + cMarginRight
            } else {
                calculatedHeight += cMarginTop + cMarginBottom + childHeight
                lines++
                val position = ChildViewPosition()
                position.left = cMarginLeft
                position.top = cMarginTop + calculatedHeight
                position.right = cMarginLeft + childWidth
                position.bottom = cMarginTop + calculatedHeight + childHeight
                childrenPositions.add(position)
                lineWidth = childWidth + cMarginLeft + cMarginRight
            }
            if (child === getChildAt(childCount - 1)) {
                calculatedHeight += cMarginTop + cMarginBottom + childHeight
                lines++
            }
        }
        measureResult.lines = lines
        measureResult.calculatedWidth = maxLen
        measureResult.calculatedHeight = calculatedHeight
        measureResult.childrenPositions = childrenPositions
        return measureResult
    }

    internal inner class MeasureResult {
        var calculatedWidth: Int = 0
        var calculatedHeight: Int = 0
        var lines: Int = 0
        var childrenPositions: List<ChildViewPosition>? = null
    }

    internal inner class ChildViewPosition {
        var left: Int = 0
        var top: Int = 0
        var right: Int = 0
        var bottom: Int = 0
    }

    class LayoutParams : ViewGroup.MarginLayoutParams {

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {}

        constructor(width: Int, height: Int) : super(width, height) {}

        constructor(source: ViewGroup.MarginLayoutParams) : super(source) {}

        constructor(source: ViewGroup.LayoutParams) : super(source) {}
    }
}
