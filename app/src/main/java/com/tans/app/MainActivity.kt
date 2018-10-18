package com.tans.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.tans.views.ImageViewPager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<ImageViewPager>(R.id.image_viewpager)
                .start(size = 5,
                        circleScroll = true,
                        showIndicator = true,
                        events = ImageViewPager.Events(loadImage = { ImageView(this).also {
                            it.setImageDrawable(resources.getDrawable(R.drawable.test, theme))
                        } })) {
                    moveToPage(3)
                }
    }
}
