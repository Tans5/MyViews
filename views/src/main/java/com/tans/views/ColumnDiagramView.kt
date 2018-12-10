package com.tans.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ColumnDiagramView : View {

    // From 0 to 1.
    private var animatorProgress: Float = 1f

    private val animator: ValueAnimator by lazy { initAnimator() }

    private val textPaint: Paint by lazy { createTextPaint() }

    private var columnDiagramPaints: List<Paint>? = null

    private var columnDiagramRects: List<Rect>? = null

    var columnData: List<ColumnData>? = null
        set(value) {
            field = value
            refreshRectsAndPaints()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun initAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 2000
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
        if (columnDiagramPaints != null &&
                columnDiagramRects != null &&
                columnData != null) {
            columnDiagramPaints.withIndex()
                    .forEach { (index, paint) ->
                        val rect = columnDiagramRects[index].withProgress(progress = animatorProgress)
                        canvas?.drawRect(rect, paint)
                        canvas?.drawText(columnData[index].title,
                                rect.left.toFloat(), (rect.bottom + TEXT_MARGIN).toFloat(),
                                textPaint)
                    }
        }
        //canvas?.drawText("I',m Tans", 10f, 10f, textPaint)
    }

    fun startAnimator() {
        if (animator.isRunning) {
            animator.cancel()
        }
        animator.start()
    }

    private fun refreshRectsAndPaints() {
        val columnData = this.columnData
        if (columnData == null) {
            return
        } else {
            val viewHeight = measuredHeight
            val viewWidth = measuredWidth
            val maxNum = columnData.maxBy { column -> column.num }?.num ?: 0
            if (maxNum == 0) {
                return
            } else {
                val averageWidth: Int = (viewWidth / columnData.size)
                val maxColumnHeight = ((viewHeight - FOOT_HEIGHT) * MAX_COLUMN_HEIGHT_RATE).toInt()
                if (averageWidth < COLUMN_WIDTH || maxColumnHeight < 0) {
                    return
                } else {
                    columnDiagramRects = columnData.withIndex()
                            .map { (index, column) ->
                                val height = (column.num.toFloat()) / (maxNum.toFloat()) * maxColumnHeight
                                val marginLeft = (averageWidth * index) + (averageWidth - COLUMN_WIDTH) / 2
                                createColumnDiagramRect(width = COLUMN_WIDTH, height = height.toInt(),
                                        viewWidth = viewWidth, viewHeight = viewHeight,
                                        marginLeft = marginLeft, marginBottom = FOOT_HEIGHT)
                            }
                    columnDiagramPaints = columnDiagramRects?.map { rect -> createColumnDiagramPaint(rect.createShader()) }
                }
            }
        }
    }

    companion object {

        private const val COLUMN_WIDTH: Int = 30

        private const val FOOT_HEIGHT = 200

        private const val MAX_COLUMN_HEIGHT_RATE = 0.85

        private const val TEXT_SIZE = 14f

        private const val TEXT_MARGIN = 20


        private fun createColumnDiagramPaint(shader: Shader): Paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            this.shader = shader
        }

        private fun createTextPaint(): Paint = Paint().apply {
            isAntiAlias = true
            textSize = TEXT_SIZE
            color = Color.WHITE
        }

        private fun createColumnDiagramRect(width: Int, height: Int,
                                    viewWidth: Int, viewHeight: Int,
                                    marginLeft: Int, marginBottom: Int)
                : Rect = Rect(marginLeft, viewHeight - height - marginBottom,
                width + marginLeft, viewHeight - marginBottom)

        private fun Rect.withProgress(progress: Float)
                : Rect = Rect(left, (bottom - (bottom - top) * progress).toInt(),
                right, bottom)

        private fun Rect.createShader(): Shader {
            val startPoint = arrayOf((left + right) / 2, bottom)
            val endPoint = arrayOf((left + right) / 2, top)
            val startColor = Color.rgb(247, 107, 28)
            val endColor = Color.rgb(251, 218, 97)
            return LinearGradient(startPoint[0].toFloat(), startPoint[1].toFloat(),
                    endPoint[0].toFloat(), endPoint[1].toFloat(),
                    startColor, endColor,
                    Shader.TileMode.CLAMP)
        }

        data class ColumnData(val title: String,
                              val num: Int)

    }

}