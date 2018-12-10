package jp.co.rakuten.golf.gora2.ui.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import com.tans.views.R
import com.tans.views.extensions.*

class AvatarCropView : ImageView {

    private lateinit var shape: Shape

    private var cacheData: CacheData? = null

    private val eventHelper = EventHelper()

    private val gestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            imageMatrix = imageMatrix.apply {
                postScale(detector.scaleFactor, detector.scaleFactor, detector.focusX, detector.focusX)
            }
            invalidate()
            return true
        }
    })

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

    data class CacheData(
            val width: Int, val height: Int,
            val shadowRect: Rect,
            val shapeDraw: ShapeDraw
    ) {
        fun onDraw(canvas: Canvas) {
            val sc = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            canvas.drawRect(shadowRect, shadowPaint)
            shapeDraw.onDraw(canvas)
            canvas.restoreToCount(sc)
        }

        companion object {
            val shadowPaint = createShadowPaint()

            fun create(width: Int, height: Int, shapeDraw: ShapeDraw): CacheData = CacheData(
                    width = width, height = height,
                    shadowRect = createShadowRect(width, height),
                    shapeDraw = shapeDraw)
        }
    }

    sealed class ShapeDraw {
        abstract fun onDraw(canvas: Canvas)

        class CircleShape(val circle: Circle) : ShapeDraw() {
            override fun onDraw(canvas: Canvas) {
                canvas.drawCircle(circle.x, circle.y, circle.radius, cropPaint)
                canvas.drawCircle(circle.x, circle.y, circle.radius, cropBorderPaint)
            }
        }

        class SexangleShape(val sexanglePath: Path) : ShapeDraw() {
            override fun onDraw(canvas: Canvas) {
                canvas.drawPath(sexanglePath, cropPaint)
                canvas.drawPath(sexanglePath, cropBorderPaint)
            }

        }

        companion object {
            val cropPaint = createCropPaint()
            val cropBorderPaint = createCropBorderPaint()

            fun circle(width: Int, height: Int): CircleShape =
                    CircleShape(createBaseCircle(width, height))

            fun sexangle(width: Int, height: Int): SexangleShape =
                    SexangleShape(createSexanglePath(calculateSexangle(
                            baseCircle = createBaseCircle(width, height),
                            width = width.toFloat(), height = height.toFloat())))
        }
    }

    private fun refreshCacheData(width: Int, height: Int) {
        cacheData = CacheData.create(width, height,
                when (shape) {
                    Shape.Circle -> ShapeDraw.circle(width, height)
                    Shape.Sexangle -> ShapeDraw.sexangle(width, height)
                })
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refreshCacheData(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cacheData?.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        eventHelper.eventUpdate(event) { dx, dy ->
            imageMatrix = imageMatrix.postTranslate1(dx, dy)
            invalidate()
        }
        return gestureDetector.onTouchEvent(event)
    }

    fun cropBitmap(): Bitmap {
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val a = Math.min(width, height)
        val bitmap = Bitmap.createBitmap(a.toInt(), a.toInt(), Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.matrix = imageMatrix.postTranslate1(-(width - a) / 2, -(height - a) / 2)
        canvas.drawColor(Color.TRANSPARENT)
        drawable.draw(canvas)
        return bitmap
    }

    fun fitCenterDisplay() {
        post {
            val bitmap: Bitmap = if (drawable is BitmapDrawable) {
                (drawable as BitmapDrawable).bitmap
            } else {
                drawable.toBitmap()
            }
            val scale = calculateFitCenterScale(bitmap)
            val matrix = Matrix()
            matrix.setScale(scale.scale, scale.scale)
            matrix.postTranslate(scale.offX, scale.offY)
            imageMatrix = matrix
            invalidate()
        }
    }

    private fun initAttrs(typedArray: TypedArray? = null) {
        shape = Shape.values().find { it.code == typedArray?.getInt(R.styleable.AvatarCropView_shape_crop, 0) } ?: Shape.Circle
        typedArray?.recycle()
        scaleType = ScaleType.MATRIX
        refreshCacheData(measuredWidth, measuredHeight)
    }

    private fun calculateFitCenterScale(bitmap: Bitmap): Scale {
        val bWith = bitmap.width.toFloat()
        val bHeight = bitmap.height.toFloat()
        val mWith = measuredWidth.toFloat()
        val mHeight = measuredHeight.toFloat()
        val scale = if (bWith / bHeight > mWith / mHeight) {
            mWith / bWith
        } else {
            mHeight / bHeight
        }
        val offX = (mWith - bWith * scale) / 2
        val offY = (mHeight - bHeight * scale) / 2
        return Scale(scale = scale, offX = offX, offY = offY)
    }

    private inner class EventHelper {

        private  var lastEventX: Float = 0f

        private var lastEventY: Float = 0f

        private var lastEventPointerId: Int = -1

        inline fun eventUpdate(event: MotionEvent, f: (Float, Float) -> Unit) {
            val pointerId = event.getPointerId1()
            if (event.action == MotionEvent.ACTION_MOVE) {
                val dx = if (lastEventPointerId != pointerId) 0f else event.getX1() - lastEventX
                val dy = if (lastEventPointerId != pointerId) 0f else event.getY1() - lastEventY
                f(dx, dy)
            }
            lastEventPointerId = pointerId
            lastEventX = event.getX1()
            lastEventY = event.getY1()
        }

        private fun MotionEvent.getX1() = this.getX(0)

        private fun MotionEvent.getY1() = this.getY(0)

        private fun MotionEvent.getPointerId1() = this.getPointerId(0)
    }

    companion object {
        private fun Matrix.postTranslate1(dx: Float, dy: Float): Matrix = this.apply { postTranslate(dx, dy) }

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
    }

}