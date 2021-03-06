package com.tans.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import kotlin.math.max

class FreeScrollView : FrameLayout {

    private val scroller: OverScroller by lazy { OverScroller(context) }

    private val scrollListener: GestureScrollListener = { dX, dY ->
        println("DX: $dX, DY: $dY")
        val (rangeX, rangeY) = getScrollRange()
        scrollXY(dX = dX.toInt(),
                scrolledX = scrollX,
                rangeX = rangeX,
                dY = dY.toInt(),
                scrolledY = scrollY,
                rangeY = rangeY)
        true
    }

    private val flingListener: GestureFlingListener = { vX, vY ->
        println("VX: $vX, VY: $vY")
        val (rangeX, rangeY) = getScrollRange()
        scroller.fling(
                scrollX, scrollY,
                -vX.toInt(), -vY.toInt(),
                0, rangeX,
                0, rangeY
        )
        ViewCompat.postInvalidateOnAnimation(this)
        true
    }

    private val gestureDetector: GestureDetector by lazy { GestureDetector(context = context, gestureScrollListener = scrollListener, gestureFlingListener = flingListener) }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int) : super(context, attrs, attrsStyle)

    init {
        isClickable = true
        isFocusable = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (childCount == 1) {
            val actionMasked = event.actionMasked
            if ((actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN || actionMasked == MotionEvent.ACTION_MOVE) && !scroller.isFinished) {
                scroller.abortAnimation()
            }
            gestureDetector.onTouchEvent(event)
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun computeScroll() {
        if (!scroller.isFinished) {
            scroller.computeScrollOffset()
            val requestX = scroller.currX
            val requestY = scroller.currY
            val oldScrollX = scrollX
            val oldScrollY = scrollY
            val dX = requestX - oldScrollX
            val dY = requestY - oldScrollY
            val isFinished = scroller.isFinished
            val (rangeX, rangeY) = getScrollRange()
            if (dX != 0 || dY != 0) {
                scrollXY(
                        dX = -dX,
                        scrolledX = oldScrollX,
                        rangeX = rangeX,
                        dY = -dY,
                        scrolledY = oldScrollY,
                        rangeY = rangeY)
            }
            if (!isFinished) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return MarginLayoutParams(lp)
    }

    override fun measureChild(
            child: View?,
            parentWidthMeasureSpec: Int,
            parentHeightMeasureSpec: Int
    ) {
        measureChildWithMargins(child, parentWidthMeasureSpec, 0, parentHeightMeasureSpec, 0)
    }

    override fun measureChildWithMargins(
            child: View?,
            parentWidthMeasureSpec: Int,
            widthUsed: Int,
            parentHeightMeasureSpec: Int,
            heightUsed: Int
    ) {
        if (child != null) {
            val lp = child.layoutParams as MarginLayoutParams
            fun getChildMeasureSpec(parentSpec: Int, padding: Int, childDimension: Int, margin: Int): Int {
                return when (childDimension) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> {
                        ViewGroup.getChildMeasureSpec(parentSpec, padding + margin, childDimension)
                    }
                    ViewGroup.LayoutParams.WRAP_CONTENT -> {
                        MeasureSpec.makeMeasureSpec(margin, MeasureSpec.UNSPECIFIED)
                    }
                    else -> {
                        MeasureSpec.makeMeasureSpec(childDimension, MeasureSpec.EXACTLY)
                    }
                }
            }
            val childWidthSpec = getChildMeasureSpec(
                    parentSpec = parentWidthMeasureSpec,
                    padding = widthUsed + paddingStart + paddingEnd,
                    childDimension = lp.width,
                    margin = lp.marginStart + lp.marginEnd
            )
            val childHeightSpec = getChildMeasureSpec(
                    parentSpec = parentHeightMeasureSpec,
                    padding = heightUsed + paddingTop + paddingBottom,
                    childDimension = lp.height,
                    margin = lp.topMargin + lp.bottomMargin
            )
            child.measure(childWidthSpec, childHeightSpec)
        }
    }

    private fun scrollXY(
            dX: Int,
            scrolledX: Int,
            rangeX: Int,
            dY: Int,
            scrolledY: Int,
            rangeY: Int) {
        val newScrollX = -dX + scrolledX
        val newScrollY = -dY + scrolledY
        val newXFixed = when {
            newScrollX < 0 -> {
                0
            }
            newScrollX > rangeX -> {
                rangeX
            }
            else -> {
                newScrollX
            }
        }
        val newYFixed = when {
            newScrollY < 0 -> {
                0
            }
            newScrollY > rangeY -> {
                rangeY
            }
            else -> {
                newScrollY
            }
        }
        scrollTo(newXFixed, newYFixed)
    }

    private fun getScrollRange(): Pair<Int, Int> {
        val childCount = childCount
        return if (childCount > 1) {
            error("Max child count is 1.")
        } else {
            if (childCount <= 0) {
                0 to 0
            } else {
                val child = getChildAt(0)
                val lp = child.layoutParams as? MarginLayoutParams
                if (lp != null) {
                    val childWidth = child.width + lp.marginStart + lp.marginEnd
                    val childHeight = child.height + lp.topMargin + lp.bottomMargin
                    val meWidth = width - paddingStart - paddingEnd
                    val meHeight = height - paddingTop - paddingBottom
                    max(0, childWidth - meWidth) to max(0, childHeight - meHeight)
                } else {
                    0 to 0
                }
            }
        }
    }
}