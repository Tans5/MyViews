package com.tans.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.tans.views.AvatarCropView
import com.tans.views.SimplifiedViewPager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cropView = findViewById<AvatarCropView>(R.id.crop_view)
        cropView.fitCenterDisplay()
        val displayView = findViewById<ImageView>(R.id.display_iv)
        findViewById<Button>(R.id.crop_bt).setOnClickListener { _ ->
            val bitmap = cropView.cropBitmap()
            displayView.setImageBitmap(bitmap)
        }
    }
}
