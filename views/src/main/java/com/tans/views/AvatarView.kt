package com.tans.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.widget.ImageView
import com.tans.views.extensions.*

class AvatarView : ImageView {

    private lateinit var shape: Shape

    private lateinit var border: Border

    private var shapeDrawCache: ShapeDrawCache? = null

    private val borderPaint by lazy {
        createBorderPaint(border)
    }

    private val bitmapPaint by lazy {
        createBitmapPaint()
    }


    constructor(context: Context) : super(context) {
        initAttrs()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarView))
    }

    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int) : super(context, attrs, attrsStyle) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarView))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int, attrsStyleRes: Int) : super(context, attrs, attrsStyle, attrsStyleRes) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarView))
    }

    override fun onDraw(canvas: Canvas) {
        refreshBitmapPaint()
        if (shapeDrawCache == null) refreshShapeDrawCache()
        shapeDrawCache?.draw(canvas = canvas,
                bitmapPaint = bitmapPaint,
                borderPaint = borderPaint)
    }

    private fun initAttrs(typedArray: TypedArray? = null) {
        shape = Shape.values().find { it.code == typedArray?.getInt(R.styleable.AvatarView_shape, 0) } ?: Shape.Circle
        border = Border(width = typedArray?.getDimension(R.styleable.AvatarView_border_width, 0f)
                ?: 0f,
                color = typedArray?.getColor(R.styleable.AvatarView_border_color, Color.WHITE)
                        ?: Color.WHITE,
                radius = typedArray?.getDimension(R.styleable.AvatarView_border_radius, 0f) ?: 0f)

        typedArray?.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        refreshShapeDrawCache()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun refreshShapeDrawCache() {
        shapeDrawCache = ShapeDrawCache(with = measuredWidth.toFloat(), height = measuredHeight.toFloat(),
                border = border,
                shape = shape)
    }

    private fun refreshBitmapPaint() {
        val bitmap = if (drawable is BitmapDrawable) {
            (drawable as BitmapDrawable).bitmap
        } else {
            drawable.toBitmap()
        }
        bitmapPaint.shader = createBaseBitmapShader(bitmap = bitmap,
                scale = calculateScale(bitmap,
                        measuredWidth.toFloat(), measuredHeight.toFloat()))
    }

    private sealed class ShapeDraw {

        abstract fun draw(canvas: Canvas, bitmapPaint: Paint, borderPaint: Paint)

        class CircleDraw(val with: Float, val height: Float, val border: Border) : ShapeDraw() {

            val circle: Circle = calculateCircle(border, with, height)

            val borderCircle: Circle = calculateBorderCircle(circle, border)

            override fun draw(canvas: Canvas, bitmapPaint: Paint, borderPaint: Paint) {
                canvas.drawCircle(circle.x, circle.y, circle.radius, bitmapPaint)
                if (border.width > 0) {
                    canvas.drawCircle(borderCircle.x, borderCircle.y, borderCircle.radius, borderPaint)
                }
            }
        }

        class SexangleDraw(val with: Float, val height: Float, val border: Border) : ShapeDraw() {

            val baseCircle: Circle = calculateCircle(border, with, height)

            val sexangle: Sexangle = calculateSexangle(baseCircle, with, height)

            val borderSexangle: Sexangle = calculateBorderSexangle(baseCircle, border, with, height)

            val sexanglePath: Path = createSexanglePath(sexangle)

            val borderSexanglePath = createSexanglePath(borderSexangle)

            override fun draw(canvas: Canvas, bitmapPaint: Paint, borderPaint: Paint) {
                canvas.drawPath(sexanglePath, bitmapPaint)
                if (border.width > 0) {
                    canvas.drawPath(borderSexanglePath, borderPaint)
                }
            }

        }

    }

    private class ShapeDrawCache(with: Float, height: Float, border: Border, shape: Shape) {
        val shapeDraw: ShapeDraw = if (shape == Shape.Circle) {
            ShapeDraw.CircleDraw(with, height, border)
        } else {
            ShapeDraw.SexangleDraw(with, height, border)
        }

        fun draw(canvas: Canvas, bitmapPaint: Paint, borderPaint: Paint) {
            shapeDraw.draw(canvas, bitmapPaint, borderPaint)
        }
    }

    private data class Border(val width: Float,
                              @ColorInt val color: Int,
                              val radius: Float)

    companion object {

        private fun calculateBorderCircle(circle: Circle,
                                          border: Border)
                : Circle = circle.copy(radius = circle.radius + border.width / 2)

        private fun calculateBorderSexangle(baseCircle: Circle, border: Border, with: Float, height: Float)
                : Sexangle = calculateSexangle(baseCircle.copy(radius = baseCircle.radius + border.width / 2),
                width = with,
                height = height)

        private fun createBaseBitmapShader(bitmap: Bitmap, scale: Scale): BitmapShader =
                BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                    val matrix = Matrix()
                    matrix.setScale(scale.scale, scale.scale)
                    matrix.postTranslate(scale.offX, scale.offY)
                    setLocalMatrix(matrix)
                }

        private fun calculateScale(bitmap: Bitmap, with: Float, height: Float): Scale {
            val bWith = bitmap.width.toFloat()
            val bHeight = bitmap.height.toFloat()
            val scale = if (bWith / bHeight > with / height) {
                height / bHeight
            } else {
                with / bWith
            }
            val offX = (with - bWith * scale) / 2
            val offY = (height - bHeight * scale) / 2
            return Scale(scale = scale, offX = offX, offY = offY)
        }

        private fun createBitmapPaint(): Paint = Paint().apply {
            isAntiAlias = true
            //shader = bitmapShader
        }

        private fun createBorderPaint(border: Border): Paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = border.color
            strokeWidth = border.width
            pathEffect = CornerPathEffect(border.radius)
        }

        private fun calculateCircle(border: Border, with: Float, height: Float): Circle {
            val x = with / 2
            val y = height / 2
            val r = Math.min(x - border.width, y - border.width)
            return Circle(x, y, r)
        }
    }

}