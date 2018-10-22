package com.tans.views.others

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

fun Context.dp2px(dp: Float): Int {
    val density = resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

fun Context.px2dp(px: Float): Int {
    val density = resources.displayMetrics.density
    return (px / density + 0.5f).toInt()
}

fun Context.screenSize(): Point {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    wm.defaultDisplay.getSize(point)
    return point
}