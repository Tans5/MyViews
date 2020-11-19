package com.tans.views.recyclerview

import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class CircleDisplayLayoutManager() : androidx.recyclerview.widget.RecyclerView.LayoutManager() {
    val orientation: Orientation = Orientation.VERTICAL
    private val orientationHelper = when (orientation) {
        Orientation.HORIZONTAL -> androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper(this)
        Orientation.VERTICAL -> androidx.recyclerview.widget.OrientationHelper.createVerticalHelper(this)
    }

    private val layoutState: LayoutState by lazy { LayoutState(available = orientationHelper.totalSpace) }

    override fun isAutoMeasureEnabled(): Boolean = true

    override fun scrollVerticallyBy(dy: Int, recycler: androidx.recyclerview.widget.RecyclerView.Recycler, state: androidx.recyclerview.widget.RecyclerView.State): Int {
        val scrollOrientation = if (dy >= 0) ScrollOrientation.END else ScrollOrientation.START
        val consumedSize = fillChildren(recycler, state, scrollOrientation, Math.abs(dy), false)
        val consumedY = if (scrollOrientation == ScrollOrientation.END) consumedSize else -consumedSize
        orientationHelper.offsetChildren(-consumedY)
        if (scrollOrientation == ScrollOrientation.START) {
            recycleFromEnd(consumedSize, recycler)
        } else {
            recycleFromStart(consumedSize, recycler)
        }
        return consumedY
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: androidx.recyclerview.widget.RecyclerView.Recycler?, state: androidx.recyclerview.widget.RecyclerView.State?): Int {
        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    override fun onLayoutChildren(recycler: androidx.recyclerview.widget.RecyclerView.Recycler, state: androidx.recyclerview.widget.RecyclerView.State) {
        if (state.itemCount <= 0) {
            removeAndRecycleAllViews(recycler)
        } else {
            detachAndScrapAttachedViews(recycler)
            fillChildren(recycler, state, ScrollOrientation.END, 0, true)
        }
    }

    override fun canScrollHorizontally(): Boolean = orientation == Orientation.HORIZONTAL

    override fun canScrollVertically(): Boolean = orientation == Orientation.VERTICAL

    override fun generateDefaultLayoutParams()
            : androidx.recyclerview.widget.RecyclerView.LayoutParams = androidx.recyclerview.widget.RecyclerView.LayoutParams(androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT,
            androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT)

    private fun initLayoutState() {
        layoutState.available = orientationHelper.endAfterPadding
        layoutState.offset = orientationHelper.startAfterPadding
        layoutState.position = -1
    }

    private fun updateLayoutState(scrollOrientation: ScrollOrientation,
                                  requestSize: Int) {
        val baseView = if (scrollOrientation == ScrollOrientation.END) {
            getChildToEnd()
        } else {
            getChildToStart()
        }
        layoutState.scrollOrientation = scrollOrientation
        layoutState.position = if (baseView != null) {
            getPosition(baseView)
        } else {
            -2
        }
        layoutState.offset = if (scrollOrientation == ScrollOrientation.END) {
            orientationHelper.getDecoratedEnd(baseView)
        } else {
            orientationHelper.getDecoratedStart(baseView)
        }
        layoutState.available = if (scrollOrientation == ScrollOrientation.END) {
            orientationHelper.endAfterPadding - layoutState.offset + requestSize
        } else {
            layoutState.offset - orientationHelper.startAfterPadding + requestSize
        }
    }

    private fun fillChildren(recycler: androidx.recyclerview.widget.RecyclerView.Recycler,
                             state: androidx.recyclerview.widget.RecyclerView.State,
                             scrollOrientation: ScrollOrientation,
                             requestSize: Int,
                             needInitLayoutState: Boolean): Int {
        if (needInitLayoutState) {
            initLayoutState()
        } else {
            updateLayoutState(scrollOrientation, requestSize)
        }
        while (layoutState.hasMore(state) && layoutState.available >= 0) {
            val v = layoutState.next(recycler) ?: break
            if (scrollOrientation == ScrollOrientation.START) {
                addView(v, 0)
            } else {
                addView(v)
            }
            measureChildWithMargins(v, 0, 0)
            val left = this.paddingLeft
            val top = if (layoutState.scrollOrientation == ScrollOrientation.END) {
                layoutState.offset
            } else {
                layoutState.offset - orientationHelper.getDecoratedMeasurement(v)
            }
            val right = orientationHelper.getDecoratedMeasurementInOther(v)
            val bottom = if (layoutState.scrollOrientation == ScrollOrientation.END) {
                layoutState.offset + orientationHelper.getDecoratedMeasurement(v)
            } else {
                layoutState.offset
            }
            layoutDecoratedWithMargins(v, left, top, right, bottom)
            updateLayoutState(scrollOrientation, requestSize)
        }
        return if (isEdgeItemViewComplateShow(scrollOrientation)) {
            0
        } else {
            Math.min(edgeViewAvailable(scrollOrientation), requestSize)
        }
    }

    private fun recycleFromStart(consumedSize: Int, recycler: androidx.recyclerview.widget.RecyclerView.Recycler) {
        val itemCount = itemCount
        for (i in 0 until itemCount) {
            val view = getChildAt(i)
            if (view != null) {
                if (orientationHelper.getDecoratedEnd(view) < consumedSize) {
                    removeAndRecycleViewAt(i, recycler)
                }
            }
        }
    }

    private fun recycleFromEnd(consumedSize: Int, recycler: androidx.recyclerview.widget.RecyclerView.Recycler) {
        val itemCount = itemCount
        for (i in (0 until itemCount).reversed()) {
            val view = getChildAt(i)
            if (view != null) {
                if (orientationHelper.getDecoratedStart(view) < consumedSize) {
                    removeAndRecycleViewAt(i, recycler)
                }
            }
        }
    }

    private fun isEdgeItemViewComplateShow(scrollOrientation: ScrollOrientation): Boolean {
        return if (isAddedEdgeItemView(scrollOrientation)) {
            if (scrollOrientation == ScrollOrientation.END) {
                val lastView = getChildToEnd()
                if (lastView != null) {
                    val available = orientationHelper.getDecoratedEnd(lastView) - orientationHelper.endAfterPadding
                    available <= 0
                } else {
                    true
                }
            } else {
                val firstView = getChildToStart()
                if (firstView != null) {
                    val available = orientationHelper.startAfterPadding - orientationHelper.getDecoratedStart(firstView)
                    available <= 0
                } else {
                    true
                }
            }
        } else {
            false
        }
    }

    private fun isAddedEdgeItemView(scrollOrientation: ScrollOrientation): Boolean {
        return if (scrollOrientation == ScrollOrientation.END) {
            val lastChild = getChildToEnd()
            if (lastChild != null) {
                getPosition(lastChild) == itemCount
            } else {
                false
            }
        } else {
            val firstChild = getChildToStart()
            if (firstChild != null) {
                getPosition(firstChild) == 0
            } else {
                false
            }
        }
    }

    private fun edgeViewAvailable(scrollOrientation: ScrollOrientation): Int {
        return if (scrollOrientation == ScrollOrientation.END) {
            val lastChild = getChildToEnd()
            if (lastChild != null) {
                val available = orientationHelper.getDecoratedEnd(lastChild) - orientationHelper.endAfterPadding
                if (available > 0) {
                    available
                } else {
                    0
                }
            } else {
                0
            }
        } else {
            val firstView = getChildToStart()
            if (firstView != null) {
                val available = orientationHelper.startAfterPadding - orientationHelper.getDecoratedStart(firstView)
                if (available > 0) {
                    available
                } else {
                    0
                }
            } else {
                0
            }
        }
    }

    private fun getChildToEnd(): View? = getChildAt(childCount - 1)

    private fun getChildToStart(): View? = getChildAt(0)

    companion object {
        enum class Orientation(val value: Int) {
            VERTICAL(0),
            HORIZONTAL(1)
        }

        enum class ScrollOrientation(val value: Int) {
            START(0),
            END(1)
        }

        data class LayoutState(var position: Int = 0,
                               var available: Int,
                               var offset: Int = 0,
                               var scrollOrientation: ScrollOrientation = ScrollOrientation.END) {

            fun hasMore(state: androidx.recyclerview.widget.RecyclerView.State): Boolean {
                return if (scrollOrientation == ScrollOrientation.START) {
                    position - 1 in 0 until  state.itemCount
                } else {
                    position + 1 in 0 until  state.itemCount
                }
            }

            fun next(recycler: androidx.recyclerview.widget.RecyclerView.Recycler): View? {
                return if (scrollOrientation == ScrollOrientation.START) {
                    recycler.getViewForPosition(position - 1)
                } else {
                    recycler.getViewForPosition(position + 1)
                }
            }
        }
    }

}