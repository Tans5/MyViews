package com.tans.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import java.lang.RuntimeException
import kotlin.math.sqrt

class ToolTipsLayout : FrameLayout {

    private var indicatorDirection = IndicatorDirection.Top
    private var indicatorGravity = IndicatorGravity.Center
    private var indicatorMarginStart: Float = 0f
    private var indicatorMarginEnd: Float = 0f
    private var indicatorRadius: Float = 0f
    private var indicatorSize: Float = 0f

    private var radius: Float = 10f
    @ColorInt private var backGroundColor: Int = Color.WHITE

    @ColorInt private var shadowColor: Int = Color.GRAY
    private var shadowOffsetX = 2f
    private var shadowOffsetY = 2f
    private var shadowBlurRadius = 10f

    private var drawPath: Path? = null
    private val paint: Paint by lazy {
        val p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.FILL
        p
    }

    constructor(context: Context) : super(context) {
        setWillNotDraw(false)
        initAttrs()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setWillNotDraw(false)
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ToolTipsLayout))
    }

    constructor(context: Context, attrs: AttributeSet, attrsStyle: Int) : super(context, attrs, attrsStyle) {
        setWillNotDraw(false)
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ToolTipsLayout))
    }

    private fun initAttrs(typedArray: TypedArray? = null) {
        indicatorDirection = IndicatorDirection.values()
                .getOrNull(typedArray?.getInt(R.styleable.ToolTipsLayout_indicator_direction, IndicatorDirection.Top.ordinal) ?: IndicatorDirection.Top.ordinal)
                ?: IndicatorDirection.Top
        indicatorGravity = IndicatorGravity.values()
                .getOrNull(typedArray?.getInt(R.styleable.ToolTipsLayout_indicator_gravity, IndicatorGravity.Center.ordinal) ?: IndicatorGravity.Center.ordinal)
                ?: IndicatorGravity.Center
        indicatorMarginStart = typedArray?.getDimension(R.styleable.ToolTipsLayout_indicator_marginStart, 0f) ?: 0f
        indicatorMarginEnd = typedArray?.getDimension(R.styleable.ToolTipsLayout_indicator_marginEnd, 0f) ?: 0f

        radius = typedArray?.getDimension(R.styleable.ToolTipsLayout_radius, context.dp2px(5f).toFloat()) ?: context.dp2px(5f).toFloat()
        shadowColor = typedArray?.getColor(R.styleable.ToolTipsLayout_shadow_color, Color.GRAY) ?: Color.GRAY
        shadowOffsetX = typedArray?.getDimension(R.styleable.ToolTipsLayout_shadow_offset_x, 2f) ?: 2f
        shadowOffsetY = typedArray?.getDimension(R.styleable.ToolTipsLayout_shadow_offset_y, 2f) ?: 2f
        shadowBlurRadius = typedArray?.getDimension(R.styleable.ToolTipsLayout_shadow_blur_radius, 10f) ?: 10f
        backGroundColor = typedArray?.getColor(R.styleable.ToolTipsLayout_tips_background_color, Color.WHITE) ?: Color.WHITE
        indicatorSize = typedArray?.getDimension(R.styleable.ToolTipsLayout_indicator_size, context.dp2px(12f).toFloat()) ?: context.dp2px(12f).toFloat()
        indicatorRadius = typedArray?.getDimension(R.styleable.ToolTipsLayout_indicator_radius, context.dp2px(2f).toFloat()) ?: context.dp2px(2f).toFloat()
        typedArray?.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            invalidateDrawer()
        }
    }

    fun invalidateDrawer() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        drawPath = calculateBackgroundPath()
        paint.color = backGroundColor
        paint.setShadowLayer(shadowBlurRadius, shadowOffsetX, shadowOffsetY, shadowColor)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val path = drawPath
        if (canvas != null && path != null)
            canvas.drawPath(path, paint)
    }

    private fun calculateBackgroundPath(): Path {
        val indicatorSize = indicatorSize
        val indicatorHeight = (indicatorSize * sqrt(3.0) / 2.0).toFloat()
        val indicatorMarginStart = indicatorMarginStart
        val indicatorMarginEnd = indicatorMarginEnd
        val indicatorDirection = indicatorDirection
        val indicatorGravity = indicatorGravity
        val indicatorRadius = indicatorRadius
        val indicatorRadiusWidth = indicatorRadius / 2.0f
        val indicatorRadiusHeight = (indicatorRadius * sqrt(3.0) / 2.0).toFloat()

        val shadowBlurRadius = shadowBlurRadius
        val shadowOffsetStartX = shadowBlurRadius - shadowOffsetX
        if (shadowOffsetStartX < 0) throw RuntimeException("Wrong shadowOffsetX value: $shadowOffsetX")
        val shadowOffsetTopY = shadowBlurRadius - shadowOffsetY
        if (shadowOffsetTopY < 0) throw RuntimeException("Wrong shadowOffsetY value: $shadowOffsetY")
        val drawWidth = if (indicatorDirection == IndicatorDirection.Top || indicatorDirection == IndicatorDirection.Bottom) {
            measuredWidth - shadowBlurRadius * 2
        } else {
            measuredHeight - shadowBlurRadius * 2
        }
        val drawHeight = if (indicatorDirection == IndicatorDirection.Top || indicatorDirection == IndicatorDirection.Bottom) {
            measuredHeight - shadowBlurRadius * 2
        } else {
            measuredWidth - shadowBlurRadius * 2
        }
        val radius = radius
        if (radius * 2 >= drawWidth || radius * 2 >= drawHeight) throw RuntimeException("Wrong radius value: $radius")

        val path = Path()

        val indicatorOffsetX = when (indicatorGravity) {
            IndicatorGravity.Start -> {
                if (indicatorDirection == IndicatorDirection.Top || indicatorDirection == IndicatorDirection.End) {
                    val start = indicatorMarginEnd
                    if (start + indicatorSize + radius > drawWidth) {
                        throw RuntimeException("Indicator size error!!")
                    } else {
                        start
                    }
                } else {
                    val start = drawWidth - indicatorSize - indicatorMarginEnd - radius
                    if (start < radius) {
                        throw RuntimeException("Indicator size error!!")
                    } else {
                        start - radius
                    }
                }
            }
            IndicatorGravity.End -> {
                if (indicatorDirection == IndicatorDirection.Top || indicatorDirection == IndicatorDirection.End) {
                    val start = drawWidth - indicatorSize - indicatorMarginEnd - radius
                    if (start < radius) {
                        throw RuntimeException("Indicator size error!!")
                    } else {
                        start - radius
                    }
                } else {
                    val start = indicatorMarginEnd
                    if (start + indicatorSize + radius > drawWidth) {
                        throw RuntimeException("Indicator size error!!")
                    } else {
                        start
                    }
                }
            }
            IndicatorGravity.Center -> {
                val start = drawWidth / 2 - indicatorSize / 2 + if (indicatorDirection == IndicatorDirection.Top || indicatorDirection == IndicatorDirection.End) {
                    indicatorMarginStart - indicatorMarginEnd
                } else {
                    indicatorMarginEnd - indicatorMarginStart
                }
                if (start < radius || start + indicatorSize > drawWidth) {
                    throw RuntimeException("Indicator size error!!")
                } else {
                    start - radius
                }
            }
        }
        path.moveTo(radius, indicatorHeight)
        path.lineTo( radius + indicatorOffsetX, indicatorHeight)
        path.lineTo( radius + indicatorOffsetX + (indicatorSize / 2.0f - indicatorRadiusWidth), indicatorRadiusHeight)
        path.quadTo( radius + indicatorOffsetX + indicatorSize / 2.0f, 0f,  radius + indicatorOffsetX + (indicatorSize / 2.0f + indicatorRadiusWidth),  indicatorRadiusHeight)
        path.lineTo( radius + indicatorOffsetX + indicatorSize,  indicatorHeight)
        path.lineTo( drawWidth - radius, indicatorHeight)
        path.quadTo(drawWidth, indicatorHeight,  drawWidth,  radius + indicatorHeight)
        path.lineTo(drawWidth,  drawHeight - radius)
        path.quadTo(drawWidth,  drawHeight,  drawWidth - radius,  drawHeight)
        path.lineTo(radius, drawHeight)
        path.quadTo(0f,  drawHeight, 0f, drawHeight - radius)
        path.lineTo(0f,  radius + indicatorHeight)
        path.quadTo(0f,  indicatorHeight,  radius,  indicatorHeight)
        path.close()
        path.transform(Matrix().apply {
            when (indicatorDirection) {
                IndicatorDirection.Top -> {}
                IndicatorDirection.End -> postRotate(90f, drawHeight / 2f, drawHeight / 2f)
                IndicatorDirection.Bottom -> postRotate(180f, drawWidth / 2f, drawHeight / 2f)
                IndicatorDirection.Start -> postRotate(270f, drawHeight / 2f, drawHeight / 2f)
            }
            postTranslate(shadowOffsetStartX, shadowOffsetTopY)
        })

        return path
    }

    companion object {
        enum class IndicatorDirection { Top, Bottom, Start, End }
        enum class IndicatorGravity { Start, End, Center }
    }

}