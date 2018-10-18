package com.tans.views

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import com.tans.views.others.*


class ImageViewPager : FrameLayout {

    private lateinit var events: Events

    private val viewPager: ViewPager by lazy {
        ViewPager(context).also {
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    .apply {
                        it.layoutParams = this
                    }
            addView(it)
        }

    }

    private val pointsParent: LinearLayout by lazy {
        LinearLayout(context).also {
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    .apply {
                        this.setMargins(0, 0, 0, 25)
                        this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                        it.layoutParams = this
                    }
            it.orientation = LinearLayout.HORIZONTAL
            addView(it)
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun start(size: Int, circleScroll: Boolean = false, showIndicator: Boolean = true, events: Events, f: (Controller.() -> Unit)? = null) {
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
                if (showIndicator) {
                    for (i in 0 until pointsParent.childCount) {
                        (pointsParent.getChildAt(i) as RadioButton).isChecked = i == p0.scrollPosition(size, circleScroll)
                    }
                }
            }

        })

        if (showIndicator) {
            createPoints(size).forEachIndexed { i, view ->
                pointsParent.addView(view, i)
            }
            (pointsParent.getChildAt(0) as RadioButton).isChecked = true
        }

        if (circleScroll) viewPager.setCurrentItem(CIRCLE_SCROLL_ITEM_SIZE / 2, false)

        if (f != null) {
            Controller(size, circleScroll).f()
        }
    }

    private fun createPoints(size: Int): List<RadioButton> = MutableList(size) { createPointView() }

    private fun createPointView(): RadioButton = RadioButton(context).also {
        it.background = resources.getDrawable(R.drawable.view_pager_postion_maker, context.theme)
        LinearLayout.LayoutParams(20, 20).apply {
            setMargins(10, 0, 10, 0)
            it.layoutParams = this
        }
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
                (events.selectedImage as Option.Some<Action1<ImageView>>).value
                        .invoke(view as ImageView)
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any = events.loadImage.invoke(position.scrollPosition(size, circleScroll))
                .also {
                    if (it.layoutParams == null) {
                        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                    container.addView(it)
                    it.setOnClickListener {
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

        fun moveToNextpage() {
            viewPager.currentItem ++
        }
    }

    data class Events(val loadImage: Func1<Int, ImageView>,
                      val itemClick: Option<Action1<Int>> = none(),
                      val pageScrollStateChanged: Option<Action1<Int>> = none(),
                      val pageScrolled: Option<Action3<Int, Float, Int>> = none(),
                      val pageSelected: Option<Action1<Int>> = none(),
                      val selectedImage: Option<Action1<ImageView>> = none())

    companion object {
        const val CIRCLE_SCROLL_ITEM_SIZE = 1000
    }

}