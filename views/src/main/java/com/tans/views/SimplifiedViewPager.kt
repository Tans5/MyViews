package jp.co.rakuten.golf.gora2.ui.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton

class SimplifiedViewPager : FrameLayout {

    private val viewPager: ViewPager by lazy {
        ViewPager(context).apply {
            overScrollMode = ViewGroup.OVER_SCROLL_NEVER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            this@SimplifiedViewPager.addView(this)
        }

    }

    private val pointsParent: LinearLayout by lazy {
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            this@SimplifiedViewPager.addView(this)
        }
    }

    var events: Events? = null

    var configData: ConfigData? = null
        set(value) {
            field = value
            refresh()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        viewPager.adapter = ImageViewPagerAdapter()
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
                events?.pageScrollStateChanged?.invoke(p0)
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                events?.pageScrolled?.invoke(p0, p1, p2)
            }

            override fun onPageSelected(pos: Int) {
                val configDataVal = configData

                if (configDataVal != null) {
                    val realPos = pos.scrollPosition(configDataVal.size, configDataVal.circleScroll)
                    if (configDataVal.indicator != null) {
                        for (i in 0 until pointsParent.childCount) {
                            (pointsParent.getChildAt(i) as RadioButton).isChecked = i == realPos
                        }
                    }

                    events?.pageSelected?.invoke(realPos)
                }
            }
        })
    }

    private fun refresh() {
        viewPager.adapter?.notifyDataSetChanged()
        val configDataVal = configData ?: return

        if (configDataVal.indicator != null) {
            createPoints(configDataVal.size, configDataVal.indicator).forEachIndexed { i, view ->
                pointsParent.addView(view, i)
            }
            pointsParent.layoutParams = createPointParentLayoutParam(configDataVal.indicator)
            (pointsParent.getChildAt(0) as RadioButton).isChecked = true
        }
        if (configDataVal.circleScroll)
            viewPager.setCurrentItem(CIRCLE_SCROLL_ITEM_SIZE / 2, false)
        else
            viewPager.setCurrentItem(0, false)
    }

    private fun createPoints(size: Int, indicator: Indicator): List<RadioButton> = MutableList(size) { createPointView(indicator) }

    private fun createPointView(indicator: Indicator): RadioButton = RadioButton(context).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            background = indicator.drawable.constantState?.newDrawable()
        }
        buttonDrawable = null
        layoutParams = LinearLayout.LayoutParams(context.dp2px(indicator.width.toFloat()), context.dp2px(indicator.height.toFloat())).apply {
            setMargins(context.dp2px(indicator.margin.toFloat()), 0, context.dp2px(indicator.margin.toFloat()), 0)
        }
    }

    private fun createPointParentLayoutParam(indicator: Indicator): FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    .apply {
                        setMargins(0, 0, 0, this@SimplifiedViewPager.context.dp2px(indicator.parentMarginBottom.toFloat()))
                        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                    }

    inner class ImageViewPagerAdapter : PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 == p1

        override fun getCount(): Int {
            val configDataVal = configData
            return when {
                configDataVal == null -> 0
                configDataVal.circleScroll -> CIRCLE_SCROLL_ITEM_SIZE
                else -> configDataVal.size
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, view: Any) {
            super.setPrimaryItem(container, position, view)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val configDataVal = configData!!
            return configDataVal.loadView(position.scrollPosition(configDataVal.size, configDataVal.circleScroll))
                    .apply {
                        if (layoutParams == null) {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT)
                        }
                        container.addView(this)
                        setOnClickListener { _ ->
                            val click = events?.itemClick
                            click?.invoke(position.scrollPosition(configDataVal.size, configDataVal.circleScroll))
                        }
                    }
        }
    }

    fun moveToPage(page: Int) {
        val configDataVal = configData ?: return
        val len = page - viewPager.currentItem
                .scrollPosition(configDataVal.size, configDataVal.circleScroll)
        viewPager.currentItem = viewPager.currentItem + len
    }

    fun moveToNextPage() {
        val configDataVal = configData ?: return
        if (configDataVal.circleScroll || viewPager.currentItem < configDataVal.size) {
            viewPager.currentItem++
        }
    }

    fun moveToPrePage() {
        val configDataVal = configData ?: return
        if (configDataVal.circleScroll || viewPager.currentItem > 0) {
            viewPager.currentItem--
        }
    }

    data class Events(val itemClick: Action1<Int>? = null,
                      val pageScrollStateChanged: Action1<Int>? = null,
                      val pageScrolled: Action3<Int, Float, Int>? = null,
                      val pageSelected: Action1<Int>? = null,
                      val selectedImage: Action1<View>? = null)

    data class Indicator(val width: Int,
                         val height: Int,
                         val margin: Int,
                         val drawable: Drawable,
                         val parentMarginBottom: Int)

    data class ConfigData(val circleScroll: Boolean = false,
                          val size: Int = 0,
                          val indicator: Indicator? = null,
                          val loadView: Func1<Int, View>)

    companion object {
        const val CIRCLE_SCROLL_ITEM_SIZE = 1000

        private fun Int.scrollPosition(size: Int, isCircleScroll: Boolean) =
                if (isCircleScroll) {
                    if (this >= CIRCLE_SCROLL_ITEM_SIZE / 2) {
                        (this - CIRCLE_SCROLL_ITEM_SIZE / 2) % size
                    } else {
                        (size - (CIRCLE_SCROLL_ITEM_SIZE / 2 - this) % size) % size
                    }
                } else {
                    this
                }
    }

}

typealias Action1<T> = (T) -> Unit
typealias Action2<T1, T2> = (T1, T2) -> Unit
typealias Action3<T1, T2, T3> = (T1, T2, T3) -> Unit

typealias Func1<T, R> = (T) -> R

sealed class Option<out T> {

    abstract fun isEmpty(): Boolean

    data class Some<out T>(val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false

    }

    object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true
    }

    companion object {
        fun none() = None
    }
}

fun none() = Option.None

fun <A> A.some() = Option.Some(this)

fun Context.dp2px(dp: Float): Int {
    val density = resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}