package com.tans.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.ImageView
import com.tans.views.R

class AvatarView : ImageView {

    private lateinit var shape: Shape

    private lateinit var border: Border

    constructor(context: Context) : super(context) {
        initAttrs()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarView))
    }
    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int) : super(context, attrs, attrsStyle) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarView))
    }
    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int, attrsStyleRes: Int) : super(context, attrs, attrsStyle, attrsStyleRes) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarView))
    }

    override fun onDraw(canvas: Canvas) {
        val bitmap: Bitmap = if (drawable is BitmapDrawable) {
            (drawable as BitmapDrawable).bitmap
        } else {
            throw Throwable("Wrong drawable.")
        }
        val bitmapPaint = createBasePaint(createBaseBitmapShader(bitmap, calculateScale(bitmap)))
        val borderPaint = createBorderPaint(border)
        val circle = calculateCircle(border)
        if (shape == Shape.Circle) {
            canvas.drawCircle(circle.x, circle.y, circle.radius, bitmapPaint)
            if (border.width > 0) {
                val borderCircle = calculateBorderCircle(circle)
                canvas.drawCircle(borderCircle.x, borderCircle.y, borderCircle.radius, borderPaint)
            }

        } else {
            val sexangle = calculateSexangle(circle)
            val path = createSexanglePath(sexangle)
            bitmapPaint.pathEffect = CornerPathEffect(border.radius)
            canvas.drawPath(path, bitmapPaint)
            if (border.width > 0) {
                val borderSexangle = calculateSexangle(circle.copy(radius = circle.radius + border.width / 2))
                val borderPath = createSexanglePath(borderSexangle)
                canvas.drawPath(borderPath, borderPaint)
            }
        }
    }

    private fun initAttrs(typedArray: TypedArray? = null) {
        shape = Shape.values().find { it.code == typedArray?.getInt(R.styleable.AvatarView_shape, 0) } ?: Shape.Circle
        border = Border(width = typedArray?.getDimension(R.styleable.AvatarView_border_width, 0f) ?: 0f,
                color = typedArray?.getColor(R.styleable.AvatarView_border_color, Color.WHITE) ?: Color.WHITE,
                radius = typedArray?.getDimension(R.styleable.AvatarView_border_radius, 0f) ?: 0f)

        typedArray?.recycle()
    }

    private fun calculateBorderCircle(circle: Circle): Circle = circle.copy(radius = circle.radius + border.width / 2)

    private fun createBaseBitmapShader(bitmap: Bitmap, scale: Scale): BitmapShader =
            BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                val matrix = Matrix()
                matrix.setScale(scale.scale, scale.scale)
                matrix.postTranslate(scale.offX, scale.offY)
                setLocalMatrix(matrix)
            }

    private fun calculateScale(bitmap: Bitmap): Scale {
        val bWith = bitmap.width.toFloat()
        val bHeight = bitmap.height.toFloat()
        val mWith = measuredWidth.toFloat()
        val mHeight = measuredHeight.toFloat()
        val scale = if (bWith / bHeight > mWith / mHeight) {
            mHeight / bHeight
        } else {
            mWith / bWith
        }
        val offX = (mWith - bWith * scale) / 2
        val offY = (mHeight - bHeight * scale) / 2
        return Scale(scale = scale, offX = offX, offY = offY)
    }

    private fun createBasePaint(bitmapShader: BitmapShader): Paint = Paint().apply {
        isAntiAlias = true
        shader = bitmapShader
    }

    private fun createBorderPaint(border: Border): Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = border.color
        strokeWidth = border.width
        pathEffect = CornerPathEffect(border.radius)
    }

    private fun calculateCircle(border: Border): Circle {
        val x = measuredWidth.toFloat() / 2
        val y = measuredHeight.toFloat() / 2
        val r = Math.min(x - border.width, y - border.width)
        return Circle(x, y, r)
    }

    private fun calculateSexangle(baseCircle: Circle): Sexangle {
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
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

    private fun createSexanglePath(sexangle: Sexangle): Path {
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

    private data class Border(val width: Float,
                              @ColorInt val color: Int,
                              val radius: Float)

    private data class Circle(val x: Float,
                              val y: Float,
                              val radius: Float)

    private data class Scale(val scale: Float,
                             val offX: Float,
                             val offY: Float)

    private data class Point(val x: Float,
                             val y: Float)

    private data class Sexangle(val points: List<Point>,
                                val center: Point)

    enum class Shape(val code: Int) {
        Circle(0),
        Sexangle(1);
    }
}