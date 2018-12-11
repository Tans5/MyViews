package com.tans.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View

class ColumnDiagramView : View {

    // From 0 to 1.
    private var animatorProgress: Float = 1f

    private val animator: ValueAnimator by lazy { initAnimator() }

    private val textPaint: Paint by lazy { createTextPaint(attrs.textSize) }

    private val footPaint: Paint by lazy { createFootRectPaint() }

    private var footRect: Rect? = null

    private var columnDiagramPaints: List<Paint>? = null

    private var columnDiagramRects: List<Rect>? = null

    private var columnBgPaints: List<Paint>? = null

    private var columnBgRects: List<Rect>? = null

    private lateinit var attrs: Attrs

    var columnData: List<ColumnData>? = null
        set(value) {
            field = value
            refreshRectsAndPaints()
        }

    constructor(context: Context) : super(context) {
        initAttrs()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ColumnDiagramView))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ColumnDiagramView))
    }

    private fun initAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = ANIMATOR_DURATION
        animator.addUpdateListener { animator ->
            animatorProgress = animator.animatedValue as? Float ?: 1f
            invalidate()
        }
        return animator
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val columnDiagramRects = this.columnDiagramRects
        val columnDiagramPaints = this.columnDiagramPaints
        val columnData = this.columnData
        val columnBgRects = this.columnBgRects
        val columnBgPaints = this.columnBgPaints
        val footRect = this.footRect

        if (footRect != null) {
            canvas?.drawRect(footRect, footPaint)
        }

        if (columnBgPaints != null &&
                columnBgRects != null &&
                columnData != null &&
                footRect != null) {
            columnBgPaints.withIndex()
                    .forEach { (index, paint) ->
                        val rect = columnBgRects[index]
                        canvas?.drawRect(rect, paint)
                        val (textX, textY) = calculateTitleTextPosition(paint = textPaint,
                                topRect = rect,
                                footRect = footRect,
                                text = columnData[index].title)
                        canvas?.drawText(columnData[index].title,
                                textX, textY,
                                textPaint)
                    }
        }

        if (columnDiagramPaints != null &&
                columnDiagramRects != null &&
                columnData != null) {
            columnDiagramPaints.withIndex()
                    .forEach { (index, paint) ->
                        val rect = columnDiagramRects[index].withProgress(progress = animatorProgress)
                        canvas?.drawRect(rect, paint)
                    }
        }
    }

    fun startAnimator() {
        if (animator.isRunning) {
            animator.cancel()
        }
        animator.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refreshFooterRect()
        refreshRectsAndPaints()
    }

    private fun initAttrs(typedArray: TypedArray? = null) {
        val columnWidth = typedArray?.getDimension(R.styleable.ColumnDiagramView_diagram_width, DEFAULT_COLUMN_WIDTH) ?: DEFAULT_COLUMN_WIDTH
        val footHeight = typedArray?.getDimension(R.styleable.ColumnDiagramView_foot_height, DEFAULT_FOOT_HEIGHT) ?: DEFAULT_FOOT_HEIGHT
        val textSize = typedArray?.getDimension(R.styleable.ColumnDiagramView_text_size, DEFAULT_TEXT_SIZE) ?: DEFAULT_TEXT_SIZE
        typedArray?.recycle()
        attrs = Attrs(textSize = textSize,
                columnWidth = columnWidth,
                footHeight = footHeight)
    }

    private fun refreshRectsAndPaints() {
        val columnData = this.columnData
        if (columnData == null) {
            return
        } else {
            val viewHeight = measuredHeight
            val viewWidth = measuredWidth
            val maxNum = columnData.maxBy { column -> column.num }?.num ?: 0
            val averageWidth: Int = (viewWidth / columnData.size)
            val maxColumnHeight = ((viewHeight - attrs.footHeight) * MAX_COLUMN_HEIGHT_RATE).toInt()

            columnBgRects = columnData.withIndex()
                    .map { (index, _) ->
                        createColumnDiagramRect(width = averageWidth, height = (viewHeight - attrs.footHeight).toInt(),
                                viewWidth = viewWidth, viewHeight = viewHeight,
                                marginLeft = (averageWidth * index), marginBottom = attrs.footHeight.toInt())
                    }

            columnBgPaints = columnBgRects?.withIndex()
                    ?.map { (index, rect) ->
                        val shader: Shader = if (index % 2 == 0) {
                            rect.createShader(startColor = Color.rgb(86, 85, 85),
                                    endColor = Color.rgb(0, 0, 0))
                        } else {
                            rect.createShader(startColor = Color.rgb(75, 72, 72),
                                    endColor = Color.rgb(0, 0, 0))
                        }
                        createColumnDiagramPaint(shader)
                    }

            if (averageWidth > attrs.columnWidth && maxColumnHeight > 0) {
                columnDiagramRects = columnData.withIndex()
                        .map { (index, column) ->
                            val height = (column.num.toFloat()) / (maxNum.toFloat()) * maxColumnHeight
                            val marginLeft = (averageWidth * index) + (averageWidth - attrs.columnWidth) / 2
                            createColumnDiagramRect(width = attrs.columnWidth.toInt(), height = height.toInt(),
                                    viewWidth = viewWidth, viewHeight = viewHeight,
                                    marginLeft = marginLeft.toInt(), marginBottom = attrs.footHeight.toInt())
                        }
                columnDiagramPaints = columnDiagramRects?.map { rect ->
                    createColumnDiagramPaint(rect.createShader(startColor = Color.rgb(247, 107, 28),
                            endColor = Color.rgb(251, 218, 97)))
                }
            }

            invalidate()
        }
    }

    private fun refreshFooterRect() {
        footRect = calculateFootRect(viewWidth = measuredWidth, viewHeight = measuredHeight,
                footHeight = attrs.footHeight.toInt())
    }

    companion object {

        private const val DEFAULT_COLUMN_WIDTH: Float = 30f

        private const val DEFAULT_FOOT_HEIGHT: Float = 100f

        private const val DEFAULT_TEXT_SIZE: Float = 22f

        private const val MAX_COLUMN_HEIGHT_RATE = 0.85

        private const val TEXT_MARGIN = 50

        private const val ANIMATOR_DURATION = 2000L

        private fun createColumnDiagramPaint(shader: Shader): Paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            this.shader = shader
        }

        private fun createTextPaint(textSize1: Float): Paint = Paint().apply {
            isAntiAlias = true
            textSize = textSize1
            color = Color.WHITE
        }

        private fun createFootRectPaint(): Paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        private fun createColumnDiagramRect(width: Int, height: Int,
                                            viewWidth: Int, viewHeight: Int,
                                            marginLeft: Int, marginBottom: Int)
                : Rect = Rect(marginLeft, viewHeight - height - marginBottom,
                width + marginLeft, viewHeight - marginBottom)

        private fun Rect.withProgress(progress: Float)
                : Rect = Rect(left, (bottom - (bottom - top) * progress).toInt(),
                right, bottom)

        private fun Rect.createShader(@ColorInt startColor: Int, @ColorInt endColor: Int): Shader {
            val startPoint = arrayOf((left + right) / 2, bottom)
            val endPoint = arrayOf((left + right) / 2, top)
            return LinearGradient(startPoint[0].toFloat(), startPoint[1].toFloat(),
                    endPoint[0].toFloat(), endPoint[1].toFloat(),
                    startColor, endColor,
                    Shader.TileMode.CLAMP)
        }

        private fun calculateTitleTextPosition(paint: Paint, topRect: Rect, footRect: Rect, text: String): Pair<Float, Float> {
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            val x = topRect.centerX() - textWidth / 2
            val y = footRect.centerY() + textHeight / 2
            return x.toFloat() to y.toFloat()
        }

        private fun calculateFootRect(viewWidth: Int, viewHeight: Int, footHeight: Int)
                : Rect = Rect(0, viewHeight - footHeight, viewWidth, viewHeight)

        data class Attrs(val textSize: Float,
                         val columnWidth: Float,
                         val footHeight: Float)

        data class ColumnData(val title: String,
                              val num: Int)

    }

}