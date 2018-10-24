package com.tans.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView

class AvatarCropView : ImageView {

    private lateinit var shape: Shape

    private  var lastEventX: Float = 0f

    private var lastEventY: Float = 0f

    private var lastEventPointerId: Int = -1

    private val gestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            imageMatrix = imageMatrix.apply {
                postScale(detector.scaleFactor, detector.scaleFactor, detector.focusX, detector.focusX)
            }
            invalidate()
            return true
        }
    }

    private val gestureDetector = ScaleGestureDetector(context, gestureListener)

    constructor(context: Context) : super(context) {
        initAttrs()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarCropView))
    }
    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int) : super(context, attrs, attrsStyle) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarCropView))
    }
    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int, attrsStyleRes: Int) : super(context, attrs, attrsStyle, attrsStyleRes) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.AvatarCropView))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = measuredWidth
        val height = measuredHeight
        val baseCircle = createBaseCircle(width, height)

        val sc = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawRect(createShadowRect(width, height), createShadowPaint())
        if (shape == Shape.Circle) {
            canvas.drawCircle(baseCircle.x, baseCircle.y, baseCircle.radius, createCropPaint())
            canvas.drawCircle(baseCircle.x, baseCircle.y, baseCircle.radius, createCropBorderPaint())
        } else {
            val sexanglePath = createSexanglePath(calculateSexangle(baseCircle))
            canvas.drawPath(sexanglePath, createCropPaint())
            canvas.drawPath(sexanglePath, createCropBorderPaint())
        }
        canvas.restoreToCount(sc)


    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = gestureDetector.onTouchEvent(event)
        val pointerId = event.getPointerId1()
        if (event.action == MotionEvent.ACTION_MOVE && result) {
            val dx = if (pointerId != lastEventPointerId) 0f else event.getX1() - lastEventX
            val dy = if (pointerId != lastEventPointerId) 0f else event.getY1() - lastEventY
            imageMatrix = imageMatrix.postTranslate1(dx, dy)
            invalidate()
        }
        lastEventX = event.getX1()
        lastEventY = event.getY1()
        lastEventPointerId = pointerId
        return result
    }

    fun cropBitmap(): Bitmap {
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val a = Math.min(width, height)
        val bitmap = Bitmap.createBitmap(a.toInt(), a.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.matrix = imageMatrix.postTranslate1((width - a) / 2, (height - a) / 2)
        canvas.drawColor(Color.TRANSPARENT)
        drawable.draw(canvas)
        return bitmap
    }

    private fun initAttrs(typedArray: TypedArray? = null) {
        shape = Shape.values().find { it.code == typedArray?.getInt(R.styleable.AvatarCropView_shape_crop, 0) } ?: Shape.Circle
        typedArray?.recycle()
        scaleType = ScaleType.MATRIX
    }

    private fun createBasePaint(): Paint = Paint().apply {
        isAntiAlias = true
    }

    private fun createShadowPaint(): Paint = createBasePaint().apply {
        setARGB(80, 50, 50, 50)
        style = Paint.Style.FILL
    }

    private fun createCropPaint(): Paint = createBasePaint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private fun createCropBorderPaint(): Paint = createBasePaint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private fun createBaseCircle(width: Int, height: Int): Circle =
            Circle(x = width.toFloat() / 2,
                    y = height.toFloat() / 2,
                    radius = (if (width > height) height.toFloat() / 2 else width.toFloat() / 2) - 5)

    private fun createShadowRect(width: Int, height: Int): Rect = Rect(0, 0 , width, height)

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

    enum class Shape(val code: Int) {
        Circle(0),
        Sexangle(1);
    }

    private data class Point(val x: Float,
                             val y: Float)

    private data class Circle(val x: Float,
                              val y: Float,
                              val radius: Float)

    private data class Sexangle(val points: List<Point>,
                                val center: Point)

    private fun Matrix.postTranslate1(dx: Float, dy: Float): Matrix = this.apply { postTranslate(dx, dy) }

    private fun MotionEvent.getX1() = this.getX(0)

    private fun MotionEvent.getY1() = this.getY(0)

    private fun MotionEvent.getPointerId1() = this.getPointerId(0)

}