package com.shanbay.stageprogress

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stage_progress.setOnClickListener(View.OnClickListener {  })

        Log.d("zgc","onCreate----------")
        step_progress.postDelayed({
            Log.d("zgc","postDelayed----------")
            step_progress.setNodeNum(5)
            step_progress.setNodeIndex(1)
            step_progress.invalidate()
        },2000)

    }
}
