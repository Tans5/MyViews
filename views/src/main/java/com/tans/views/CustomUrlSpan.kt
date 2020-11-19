package com.tans.views

import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

object PressStateLinkMovementMethod : LinkMovementMethod() {
    enum class PressState { Pressed, Release }


    private var activePointerId: Int = -1
    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        if (buffer != null && event != null && widget != null) {
            val actionMasked = event.actionMasked
            val actionIndex = event.actionIndex

            var pressedLink: PressStateSpan? = null
            val allCanPressLinks = buffer.getSpans(0, buffer.length, PressStateSpan::class.java)
            when (actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    activePointerId = event.getPointerId(actionIndex)
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val off = getLinkOffset(event.getX(pointerIndex), event.getY(pointerIndex), widget)
                    pressedLink = buffer.getSpans(off, off, PressStateSpan::class.java).getOrNull(0)
                }

                MotionEvent.ACTION_MOVE -> {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val off = getLinkOffset(event.getX(pointerIndex), event.getY(pointerIndex), widget)
                    pressedLink = buffer.getSpans(off, off, PressStateSpan::class.java).getOrNull(0)
                }

                MotionEvent.ACTION_UP -> {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val off = getLinkOffset(event.getX(pointerIndex), event.getY(pointerIndex), widget)
                    val clickSpan = buffer.getSpans(off, off, ClickableSpan::class.java).getOrNull(0)
                    clickSpan?.onClick(widget)
                }

                MotionEvent.ACTION_CANCEL -> {
                    activePointerId = -1
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    activePointerId = event.getPointerId(actionIndex)
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val off = getLinkOffset(event.getX(pointerIndex), event.getY(pointerIndex), widget)
                    pressedLink = buffer.getSpans(off, off, PressStateSpan::class.java).getOrNull(0)
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    val currentId = event.getPointerId(actionIndex)
                    if (currentId == activePointerId) {
                        val newIndex = if (actionIndex == 0) 1 else 0
                        activePointerId = event.getPointerId(newIndex)
                    }
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val off = getLinkOffset(event.getX(pointerIndex), event.getY(pointerIndex), widget)
                    pressedLink = buffer.getSpans(off, off, PressStateSpan::class.java).getOrNull(0)
                }
            }
            allCanPressLinks.forEach {
                if (it == pressedLink) {
                    it.updatePressState(PressState.Pressed)
                } else {
                    it.updatePressState(PressState.Release)
                }
            }
            ViewCompat.postInvalidateOnAnimation(widget)
        }
        Touch.onTouchEvent(widget, buffer, event)
        return true
    }

    fun getLinkOffset(x: Float, y: Float, widget: TextView): Int {
        val fixedX = x - widget.totalPaddingLeft + widget.scrollX
        val fixedY = y - widget.totalPaddingTop + widget.scrollY
        val layout = widget.layout
        val line = layout.getLineForVertical(fixedY.toInt())
        return layout.getOffsetForHorizontal(line, fixedX)
    }

}

interface PressStateSpan {

    var pressState: PressStateLinkMovementMethod.PressState

    fun updatePressState(pressState: PressStateLinkMovementMethod.PressState) { this.pressState = pressState }

}

@Suppress("FunctionName")
fun PressStateSpan(defaultPressState: PressStateLinkMovementMethod.PressState = PressStateLinkMovementMethod.PressState.Release)
        : PressStateSpan = object : PressStateSpan {
    override var pressState: PressStateLinkMovementMethod.PressState = defaultPressState
}

fun String.toCustomUrlSpan(click: (view: View, url: String) -> Boolean = { _, _ -> false },
                           @ColorInt pressedColor: Int? = null,
                           isUnderlineText: Boolean = true,
                           isBold: Boolean = false): URLSpan = object : URLSpan(this), PressStateSpan by PressStateSpan() {

    override fun onClick(widget: View) {
        if (!click(widget, url)) { super.onClick(widget) }
    }

    override fun updateDrawState(ds: TextPaint) {
        if (pressedColor != null) {
            when (this.pressState) {
                PressStateLinkMovementMethod.PressState.Release -> {
                    super.updateDrawState(ds)
                }
                PressStateLinkMovementMethod.PressState.Pressed -> {
                    ds.color = ds.linkColor
                    ds.bgColor = pressedColor
                }
            }
        } else {
            super.updateDrawState(ds)
        }
        ds.isUnderlineText = isUnderlineText
        if (isBold)
            ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
}