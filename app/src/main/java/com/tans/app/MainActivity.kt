package com.tans.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.tans.views.ColumnDiagramView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = findViewById<ColumnDiagramView>(R.id.column_diagram_view)
        view.post {
            view.columnData = (1 .. 12).map {
                ColumnDiagramView.Companion.ColumnData("$it", it)
            }
            view.startAnimator()
        }
    }
}
