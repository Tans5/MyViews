package com.tans.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import kotlin.math.max
import kotlin.math.min

class FreeScrollView : FrameLayout {

    private val scroller: OverScroller by lazy { OverScroller(context) }

    private val scrollListener: GestureScrollListener = { dX, dY ->
        val (rangeX, rangeY) = getScrollRange()
        scrollXY(dX = dX.toInt(),
                scrolledX = scrollX,
                rangeX = rangeX,
                dY = dY.toInt(),
                scrolledY = scrollY,
                rangeY = rangeY)
        println("DX: $dX, DY: $dY")
        true
    }

    private val flingListener: GestureFlingListener = { vX, vY ->
        println("VX: $vX, VY: $vY")
        scroller.fling(
                scrollX, scrollY,
                vX.toInt(), vY.toInt(),
                0, Int.MAX_VALUE,
                0, Int.MAX_VALUE
        )
        ViewCompat.postInvalidateOnAnimation(this)
        true
    }

    private val gestureDetector: GestureDetector by lazy { GestureDetector(context = context, gestureScrollListener = scrollListener, gestureFlingListener = flingListener) }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int) : super(context, attrs, attrsStyle)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (childCount == 1) {
            val actionMasked = event.actionMasked
            if ((actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN) && !scroller.isFinished) {
                scroller.abortAnimation()
            }
            gestureDetector.onTouchEvent(event)
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun computeScroll() {
        val isFinished = scroller.computeScrollOffset()
        val requestX = scroller.currX
        val requestY = scroller.currY
        val oldScrollX = scrollX
        val oldScrollY = scrollY
        val dX = requestX - oldScrollX
        val dY = requestY - oldScrollY
        val (rangeX, rangeY) = getScrollRange()
        scrollXY(
                dX = dX,
                scrolledX = oldScrollX,
                rangeX = rangeX,
                dY = dY,
                scrolledY = oldScrollY,
                rangeY = rangeY)
        val newScrollX = scrollX
        val newScrollY = scrollY
        if (newScrollX != oldScrollX || newScrollY != oldScrollY) {
            if (!isFinished) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else {
            scroller.abortAnimation()
        }
    }

    private fun scrollXY(
            dX: Int,
            scrolledX: Int,
            rangeX: Int,
            dY: Int,
            scrolledY: Int,
            rangeY: Int) {
        val newScrollX = dX + scrolledX
        val newScrollY = dY + scrolledY
        scrollTo(min(newScrollX, rangeX), min(newScrollY, rangeY))
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
                    val childWidth = lp.width + lp.marginStart + lp.marginEnd
                    val childHeight = lp.height + lp.topMargin + lp.bottomMargin
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