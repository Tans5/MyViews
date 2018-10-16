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
import com.tans.views.R

typealias LoadImage = (Int, ImageView) -> Unit
typealias PageScrollState = (Int) -> Unit
typealias PageScrollStateChanged = (Int, Float, Int) -> Unit
typealias ClickListener = (Int) -> Unit

class ImageViewPager: FrameLayout, ViewPager.OnPageChangeListener {

    private val viewPager: ViewPager by lazy {
        ViewPager(context).let {
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    .apply {
                        it.layoutParams = this
                    }
            addView(it)
            it.addOnPageChangeListener(this)
            it
        }
    }

    private val pointsParent: LinearLayout by lazy {
        LinearLayout(context).let {
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    .apply {
                        this.setMargins(0, 0, 0 ,25)
                        this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                        it.layoutParams = this
                    }
            it.orientation = LinearLayout.HORIZONTAL
            addView(it)
            it
        }
    }

    private val adapter: ImageViewPagerAdapter by lazy { ImageViewPagerAdapter() }

    private var size: Int = 0
    private lateinit var loadImage: LoadImage
    private var pageScrollState: PageScrollState? = null
    private var pageScrollStateChanged: PageScrollStateChanged? = null
    private var onClickListener: ClickListener? = null
    var showIndicator: Boolean = false
    var circleScroll: Boolean = false

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onPageScrollStateChanged(p0: Int) {
        pageScrollState?.invoke(p0)
    }

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
        pageScrollStateChanged?.invoke(p0, p1, p2)
    }

    override fun onPageSelected(p0: Int) {
        if(showIndicator) {
            for (i in 0 until size) {
                (pointsParent.getChildAt(i) as RadioButton).isChecked = i == calculatePosition(p0)
            }
        }
    }

    fun start(size: Int, loadImage: LoadImage, circleScroll: Boolean = false, showIndicator: Boolean = true) {
        if(size <=0) return
        this.size = size
        this.loadImage = loadImage
        this.circleScroll = circleScroll
        this.showIndicator = showIndicator
        viewPager.adapter = adapter

        if (showIndicator) {
            createPoints()
            (pointsParent.getChildAt(0) as RadioButton).isChecked = true
        }

        if (circleScroll) viewPager.setCurrentItem(CIRCLE_SCROLL_ITEM_SIZE / 2, false)
    }

    fun setPageScrollStateListener(listener: PageScrollState) {
        this.pageScrollState = listener
    }

    fun setPageScrollStateChangedListener(listener: PageScrollStateChanged) {
        this.pageScrollStateChanged = listener
    }

    fun setOnItemClickListener(listener: ClickListener) {
        this.onClickListener = listener
    }

    fun moveToNextPage() {
        viewPager.currentItem = viewPager.currentItem + 1
    }

    fun currentPosition() = calculatePosition(viewPager.currentItem)

    fun moveToPage(pos: Int) {
        val len = pos - calculatePosition(viewPager.currentItem)
        viewPager.currentItem = viewPager.currentItem + len
    }

    private fun createPoints() {
        for(i in 0 until size) {
            pointsParent.addView(createPointView(i), i)
        }
    }

    private fun createPointView(tag: Int): View = RadioButton(context).let {
        it.setBackgroundDrawable(resources.getDrawable(R.drawable.view_pager_postion_maker))
        LinearLayout.LayoutParams(20, 20).apply {
            setMargins(10, 0,  10 ,0)
            it.layoutParams = this
        }
        it.tag = tag
        it.setOnClickListener {
            moveToPage(tag)
        }
        it
    }

    private fun createImageView() : ImageView = ImageView(context).let {
        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        it.scaleType = ImageView.ScaleType.CENTER_CROP
        it
    }

    private fun calculatePosition(position: Int): Int {
        return if(circleScroll) {
            if (position >= CIRCLE_SCROLL_ITEM_SIZE / 2) {
                (position - CIRCLE_SCROLL_ITEM_SIZE / 2) % size
            } else {
                (size - (CIRCLE_SCROLL_ITEM_SIZE / 2 - position) % size) % size
            }
        } else {
            position
        }
    }

    inner class ImageViewPagerAdapter: PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 == p1

        override fun getCount(): Int = if (circleScroll) CIRCLE_SCROLL_ITEM_SIZE else size

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any = createImageView()
                .let {
                    container.addView(it)
                    loadImage.invoke(calculatePosition(position), it)
                    it.setOnClickListener {
                        onClickListener?.invoke(calculatePosition(viewPager.currentItem))
                    }
                    it
                }
    }

    companion object {
        const val CIRCLE_SCROLL_ITEM_SIZE = 1000
    }

}