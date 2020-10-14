package com.example.circularprogressbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val DEBUG_TAG = "TEST"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress_circular.apply {
            colorMode = CircularProgress.ColorMode.RED
            animationDuration = 10000L
            //progressTextSize = 100f
            progress = 0f
            setCircularProgressListener(object : CircularProgress.CircularProgressListener {
                override fun onProgressValue(progress: Float) {
                    Log.d(DEBUG_TAG, "onProgressValue(), progress=$progress")
                }

                override fun onProgressStart() {
                    Log.d(DEBUG_TAG, "onProgressStart()")
                }

                override fun onProgressPause() {
                    Log.d(DEBUG_TAG, "onProgressPause()")
                }

                override fun onProgressResume() {
                    Log.d(DEBUG_TAG, "onProgressResume()")
                }

                override fun onProgressEnd() {
                    Log.d(DEBUG_TAG, "onProgressEnd()")
                }
            })
        }

        button4.setOnClickListener {
            progress_circular.startAnimation()
        }

        button5.setOnClickListener {
            progress_circular.stopAnimation()
        }

        button6.setOnClickListener {
            progress_circular.pauseAnimation()
        }

        button7.setOnClickListener {
            progress_circular.resumeAnimation()
        }

        button3.setOnClickListener {
            if (progress_circular.colorMode == CircularProgress.ColorMode.BLUE) {
                progress_circular.colorMode = CircularProgress.ColorMode.RED
            } else if (progress_circular.colorMode == CircularProgress.ColorMode.RED) {
                progress_circular.colorMode = CircularProgress.ColorMode.GREEN
            } else if (progress_circular.colorMode == CircularProgress.ColorMode.GREEN) {
                progress_circular.colorMode = CircularProgress.ColorMode.BLUE
            }
        }
    }
}