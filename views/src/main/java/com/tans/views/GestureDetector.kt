package com.tans.views

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import kotlin.math.absoluteValue

typealias GestureScrollListener = (dX: Float, dY: Float) -> Boolean
typealias GestureFlingListener = (vX: Float, vY: Float) -> Boolean

class GestureDetector(
        private val context: Context,
        private val gestureScrollListener: GestureScrollListener? = null,
        private val gestureFlingListener: GestureFlingListener? = null) {

    private var velocityTracker: VelocityTracker? = null

    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var initX: Float = 0f
    private var initY: Float = 0f
    private var isDragged: Boolean = false
    private var activeScrollId: Int = -1

    private val viewConfiguration: ViewConfiguration by lazy { ViewConfiguration.get(context) }
    private val touchSlop: Float by lazy { viewConfiguration.scaledTouchSlop.toFloat() }
    private val minVelocity: Float by lazy { viewConfiguration.scaledMinimumFlingVelocity.toFloat() }
    private val maxVelocity: Float by lazy { viewConfiguration.scaledMaximumFlingVelocity.toFloat() }


    fun onTouchEvent(event: MotionEvent): Boolean {
        val activeIndex = event.actionIndex
        val actionMasked = event.actionMasked
        var handled = true
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            initVelocityTrackerOrReset()
        }

        val velocityTracker = this.velocityTracker
        val vEvent = MotionEvent.obtain(event)
        velocityTracker?.addMovement(vEvent)
        vEvent?.recycle()
        when (actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                lastX = event.getX(0)
                lastY = event.getY(0)
                initX = lastX
                initY = lastY
                isDragged = false
                activeScrollId = event.getPointerId(0)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                initVelocityTrackerOrReset()
                activeScrollId = event.getPointerId(activeIndex)
                lastX = event.getX(activeIndex)
                lastY = event.getY(activeIndex)
                initX = lastX
                initY = lastY
                isDragged = false
            }

            MotionEvent.ACTION_MOVE -> {
                val newX = event.getX(activeIndex)
                val newY = event.getY(activeIndex)
                if (gestureScrollListener != null) {
                    if (!isDragged) {
                        val dX = newX - initX
                        val dY = newY - initY
                        handled = if ((dX.absoluteValue > touchSlop || dY.absoluteValue > touchSlop)) {
                            isDragged = true
                            gestureScrollListener.invoke(dX, dY)
                        } else {
                            false
                        }
                    } else {
                        val dX = newX - lastX
                        val dY = newY - lastY
                        gestureScrollListener.invoke(dX, dY)
                    }
                }
                lastX = newX
                lastY = newY
            }

            MotionEvent.ACTION_POINTER_UP -> {
                recycleVelocityTracker()
                val newIndex = if (activeIndex == 0) {
                    1
                } else {
                    0
                }
                activeScrollId = event.findPointerIndex(newIndex)
                lastX = event.getX(newIndex)
                lastY = event.getY(newIndex)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (actionMasked == MotionEvent.ACTION_UP) {
                    velocityTracker?.computeCurrentVelocity(1000, maxVelocity)
                    val vX = velocityTracker?.getXVelocity(activeScrollId) ?: 0f
                    val vY = velocityTracker?.getYVelocity(activeScrollId) ?: 0f
                    handled = if (vX.absoluteValue > minVelocity || vY.absoluteValue > minVelocity) {
                        gestureFlingListener?.invoke(vX, vY) ?: false
                    } else {
                        false
                    }
                }
                recycleVelocityTracker()
            }
        }

        return handled
    }

    private fun initVelocityTrackerOrReset() {
        val velocityTracker = this.velocityTracker
        if (velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker.clear()
        }
    }

    private fun recycleVelocityTracker() {
        val velocityTracker = this.velocityTracker
        velocityTracker?.clear()
        this.velocityTracker = null
    }
}