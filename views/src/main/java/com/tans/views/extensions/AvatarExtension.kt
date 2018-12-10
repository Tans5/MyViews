package com.tans.views.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.Drawable

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

fun calculateSexangle(baseCircle: Circle,
                      width: Float,
                      height: Float): Sexangle {
    val r = baseCircle.radius
    val cosR = (Math.sqrt(3.toDouble()) * r / 2).toFloat()
    val center = Point(x = baseCircle.x, y = baseCircle.y)
    val point0 = Point(x = width / 2 - r / 2, y = height / 2 - cosR)
    val point1 = Point(x = width / 2 + r / 2, y = height / 2 - cosR)
    val point2 = Point(x = width / 2 + r, y = height / 2)
    val point3 = Point(x = width / 2 + r / 2, y = height / 2 + cosR)
    val point4 = Point(x = width / 2 - r / 2, y = height / 2 + cosR)
    val point5 = Point(x = width / 2 - r, y = height / 2)
    val points = listOf(point0, point1, point2, point3, point4, point5)
    return Sexangle(points, center)
}

fun createSexanglePath(sexangle: Sexangle): Path {
    val path = Path()
    val points = sexangle.points
    path.moveTo(points[0].x, points[0].y)
    path.lineTo(points[1].x, points[1].y)
    path.lineTo(points[2].x, points[2].y)
    path.lineTo(points[3].x, points[3].y)
    path.lineTo(points[4].x, points[4].y)
    path.lineTo(points[5].x, points[5].y)
    path.lineTo(points[0].x, points[0].y)
    path.close()
    return path
}

data class Point(val x: Float,
                 val y: Float)

data class Circle(val x: Float,
                  val y: Float,
                  val radius: Float)

data class Sexangle(val points: List<Point>,
                    val center: Point)

data class Scale(val scale: Float,
                 val offX: Float,
                 val offY: Float)

enum class Shape(val code: Int) {
    Circle(0),
    Sexangle(1);
}