package com.tans.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.tans.views.SimplifiedViewPager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<SimplifiedViewPager>(R.id.image_viewpager)
                .start(size = 5,
                        circleScroll = false,
                        indicator = SimplifiedViewPager.Indicator(
                                width = 8,
                                height = 8,
                                margin = 5,
                                drawable = resources.getDrawable(R.drawable.view_pager_postion_maker, theme),
                                parentMarginBottom = 20),
                        events = SimplifiedViewPager.Events(loadView = { ImageView(this).also {
                            it.setImageDrawable(resources.getDrawable(R.drawable.test, theme))
                        } }))
    }
}
