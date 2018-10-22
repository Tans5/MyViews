package com.tans.views

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
import com.tans.views.others.*

class SimplifiedViewPager : FrameLayout {

    private lateinit var events: Events

    private val viewPager: ViewPager by lazy {
        ViewPager(context).apply {
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

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun start(size: Int, circleScroll: Boolean = false, indicator: Indicator? = null, events: Events, f: (Controller.() -> Unit)? = null) {
        if (size <= 0) return
        this.events = events

        viewPager.adapter = ImageViewPagerAdapter(size, circleScroll)
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
                if (events.pageScrollStateChanged is Option.Some) {
                    events.pageScrollStateChanged.value.invoke(p0)
                }
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                if (events.pageScrolled is Option.Some) {
                    events.pageScrolled.value.invoke(p0, p1, p2)
                }
            }

            override fun onPageSelected(p0: Int) {
                if (indicator != null) {
                    for (i in 0 until pointsParent.childCount) {
                        (pointsParent.getChildAt(i) as RadioButton).isChecked = i == p0.scrollPosition(size, circleScroll)
                    }
                }

                if (events.pageSelected is Option.Some) {
                    events.pageSelected.value.invoke(p0.scrollPosition(size, circleScroll))
                }
            }

        })

        if (indicator != null) {
            createPoints(size, indicator).forEachIndexed { i, view ->
                pointsParent.addView(view, i)
            }
            pointsParent.layoutParams = createPointParentLayoutParam(indicator)
            (pointsParent.getChildAt(0) as RadioButton).isChecked = true
        }

        if (circleScroll) viewPager.setCurrentItem(CIRCLE_SCROLL_ITEM_SIZE / 2, false)

        if (f != null) {
            Controller(size, circleScroll).f()
        }
    }

    private fun createPoints(size: Int, indicator: Indicator): List<RadioButton> = MutableList(size) { createPointView(indicator) }

    private fun createPointView(indicator: Indicator): RadioButton = RadioButton(context).apply {
        background = indicator.drawable.constantState?.newDrawable()
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

    private fun Int.scrollPosition(size: Int, isScroll: Boolean) =
            if (isScroll) {
                if (this@scrollPosition >= CIRCLE_SCROLL_ITEM_SIZE / 2) {
                    (this@scrollPosition - CIRCLE_SCROLL_ITEM_SIZE / 2) % size
                } else {
                    (size - (CIRCLE_SCROLL_ITEM_SIZE / 2 - this@scrollPosition) % size) % size
                }
            } else {
                this
            }

    inner class ImageViewPagerAdapter(val size: Int, val circleScroll: Boolean) : PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 == p1

        override fun getCount(): Int = if (circleScroll) CIRCLE_SCROLL_ITEM_SIZE else size

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, view: Any) {
            super.setPrimaryItem(container, position, view)
            if (events.selectedImage is Option.Some) {
                (events.selectedImage as Option.Some<Action1<View>>).value
                        .invoke(view as View)
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any = events.loadView.invoke(position.scrollPosition(size, circleScroll))
                .apply {
                    if (layoutParams == null) {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                    container.addView(this)
                    setOnClickListener { _ ->
                        if (events.itemClick is Option.Some) {
                            (events.itemClick as Option.Some<Action1<Int>>).value
                                    .invoke(position.scrollPosition(size, circleScroll))
                        }
                    }
                }
    }

    inner class Controller(val size: Int, val circleScroll: Boolean) {
        fun moveToPage(page: Int) {
            val len = page - viewPager.currentItem.scrollPosition(size, circleScroll)
            viewPager.currentItem = viewPager.currentItem + len
        }

        fun moveToNextPage() {
            viewPager.currentItem ++
        }
    }

    data class Events(val loadView: Func1<Int, View>,
                      val itemClick: Option<Action1<Int>> = none(),
                      val pageScrollStateChanged: Option<Action1<Int>> = none(),
                      val pageScrolled: Option<Action3<Int, Float, Int>> = none(),
                      val pageSelected: Option<Action1<Int>> = none(),
                      val selectedImage: Option<Action1<View>> = none())

    data class Indicator(val width: Int,
                         val height: Int,
                         val margin: Int,
                         val drawable: Drawable,
                         val parentMarginBottom: Int)

    companion object {
        const val CIRCLE_SCROLL_ITEM_SIZE = 1000
    }

}