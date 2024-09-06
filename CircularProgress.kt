package com.example.circularprogressbar

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import java.lang.IllegalArgumentException
import kotlin.math.min

/**
 * Circular Progress Bar
 * @author = YZ
 * @version 1.0
 */

class CircularProgress : View {
    private var progressText = DEFAULT_PROGRESS_TEXT
    private var center = 0f
    private var backgroundCircleRadius = 0f
    private var foregroundCircleStartPoint = 0f
    private var foregroundCircleEndPoint = 0f
    private var strokeWidthBackgroundCircle = 0f
    private var strokeWidthForegroundCircle = 0f
    private var textX = 0f
    private var textY = 0f
    private var textWidth = 0f
    private var textHeight = 0f
    private lateinit var objectAnimator:ObjectAnimator
    private var circularProgressListener: CircularProgressListener? = null

    private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#212121")
    }

    private var foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

/*    private var testPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#eeeeee")
    }*/

    private var textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        textAlign = Paint.Align.CENTER
    }

    var animationDuration = DEFAULT_ANIMATION_DURATION
        set(value) {
            pauseAnimation()
            objectAnimator.duration = value
            field = value
            invalidate()
        }

    var progressTextSize = dpToPx(70f)  // Default 70dp
        set(value) {
            field = value
            requestLayout()
        }

    var progress = DEFAULT_ANGLE
        get() = field
        set(value) {
            if (value >= MAX_PROGRESS) {
                field = MAX_PROGRESS
                progressText = "100"
            } else if (value < 0) throw IllegalArgumentException("Progress must be greater than zero.")
            else {
                field = value
                progressText = (value / 3.6f).toInt().toString()
            }
            invalidate()
        }

    var colorMode = ColorMode.BLUE // Default
        set(value) {
            when (value) {
                ColorMode.BLUE -> {
                    textPaint.shader = foregroundGradientBlue()
                    foregroundPaint.shader = foregroundGradientBlue()
                }
                ColorMode.RED -> {
                    textPaint.shader = foregroundGradientRed()
                    foregroundPaint.shader = foregroundGradientRed()
                }
                ColorMode.GREEN -> {
                    textPaint.shader = foregroundGradientGreen()
                    foregroundPaint.shader = foregroundGradientGreen()
                }
            }
            field = value
            invalidate()
        }

    constructor(context: Context) : super(context) {
        setProgressAnimation()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initAttrs(context, attributeSet)
        setProgressAnimation()
    }

    private fun initAttrs(context: Context, attributeSet: AttributeSet) {
        val typedArray =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.CircularProgress, 0, 0)
        try {
            progress = typedArray.getFloat(R.styleable.CircularProgress_progress, 0f)
            progressTextSize = typedArray.getDimension(
                R.styleable.CircularProgress_progressTextSize,
                dpToPx(70f)
            )
            colorMode =
                ColorMode.values()[typedArray.getInteger(R.styleable.CircularProgress_colorMode, 0)]
        } finally {
            typedArray.recycle()
        }
    }

    fun setCircularProgressListener(callback: CircularProgressListener) {
        this.circularProgressListener = callback
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val defaultSize = dpToPx(200f).toInt()
        val min = when {
            widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY -> { // exact number or match_parent
                defaultSize
            }
            else -> min(widthSize, heightSize)
        }
        setMeasuredDimension(min, min) //square layout
        Log.d(DEBUG_TAG, "onMeasure(), width=$measuredWidth height=$measuredHeight")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        strokeWidthBackgroundCircle = w * 0.20f
        strokeWidthForegroundCircle = w * 0.14f
        backgroundPaint.strokeWidth = strokeWidthBackgroundCircle
        foregroundPaint.strokeWidth = strokeWidthForegroundCircle
        center = w / 2f
        backgroundCircleRadius = (measuredWidth - strokeWidthBackgroundCircle) / 2f
        val dashPathEffect: DashPathEffect = DashPathEffect(
            floatArrayOf(
                strokeWidthForegroundCircle * 0.3f,
                strokeWidthForegroundCircle * 0.05f
            ), 0f
        )
        foregroundPaint.pathEffect = dashPathEffect
        foregroundPaint.setShadowLayer(
            strokeWidthForegroundCircle * 0.2f,
            0f,
            0f,
            Color.parseColor("#f6f4e6")
        )

        foregroundCircleStartPoint =
            (strokeWidthBackgroundCircle - strokeWidthForegroundCircle) / 2f + strokeWidthForegroundCircle / 2f
        foregroundCircleEndPoint = w - foregroundCircleStartPoint
        autoTextBounds(w, h)
        Log.d(DEBUG_TAG, "onSizeChanged(), width=$w height=$h")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(center, center, backgroundCircleRadius, backgroundPaint)
        canvas?.drawArc(
            foregroundCircleStartPoint,
            foregroundCircleStartPoint,
            foregroundCircleEndPoint,
            foregroundCircleEndPoint,
            START_ANGLE,
            progress,
            false,
            foregroundPaint
        )
        drawProgressText(canvas!!)
    }

    private fun drawProgressText(canvas: Canvas) {
        //canvas.drawRect(textX, textY - textHeight, textX + textWidth, textY, testPaint)
        canvas.drawText(progressText, (textX + textWidth / 2f), textY, textPaint)
    }

    private fun autoTextBounds(w: Int, h: Int) {
        val bounds = Rect()
        textPaint.textSize = progressTextSize
        textPaint.getTextBounds(DEFAULT_PROGRESS_TEXT, 0, DEFAULT_PROGRESS_TEXT.length, bounds)
        textWidth =
            textPaint.measureText(DEFAULT_PROGRESS_TEXT) // calculate width
        textHeight = bounds.height().toFloat()
        val circleBound = w - strokeWidthBackgroundCircle * 2
        if (textWidth > circleBound || textHeight > circleBound) {
            var tmpSize = progressTextSize
            while (textWidth > circleBound || textHeight > circleBound) {
                tmpSize -= 1
                textPaint.textSize = tmpSize
                textPaint.getTextBounds(
                    DEFAULT_PROGRESS_TEXT,
                    0,
                    DEFAULT_PROGRESS_TEXT.length,
                    bounds
                )
                textWidth =
                    textPaint.measureText(DEFAULT_PROGRESS_TEXT)
                textHeight = bounds.height().toFloat()
            }
        }
        textX = (w - textWidth) / 2f
        textY = (h + textHeight) / 2f
        textPaint.strokeWidth = textPaint.textSize * 0.1f
        val dashPathEffect =
            DashPathEffect(floatArrayOf(textPaint.textSize * 0.8f, textPaint.textSize * 0.05f), 0f)
        textPaint.pathEffect = dashPathEffect
    }

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun foregroundGradientBlue(): LinearGradient = LinearGradient(
        0f,
        0f,
        dpToPx(50f),
        dpToPx(50f),
        Color.parseColor("#10eaf0"),
        Color.parseColor("#5edfff"),
        Shader.TileMode.MIRROR
    )

    private fun foregroundGradientRed(): LinearGradient = LinearGradient(
        0f,
        0f,
        dpToPx(50f),
        dpToPx(50f),
        Color.parseColor("#ff0000"),
        Color.parseColor("#ff5722"),
        Shader.TileMode.MIRROR
    )

    private fun foregroundGradientGreen(): LinearGradient = LinearGradient(
        0f,
        0f,
        dpToPx(50f),
        dpToPx(50f),
        Color.parseColor("#00bd56"),
        Color.parseColor("#4ef037"),
        Shader.TileMode.MIRROR
    )

    private fun setProgressAnimation() {
        objectAnimator = ObjectAnimator.ofFloat(this, "progress", MAX_PROGRESS)
        objectAnimator.interpolator = LinearInterpolator()
        objectAnimator.duration = animationDuration
        objectAnimator.addUpdateListener {
            circularProgressListener.let {
                it?.onProgressValue(objectAnimator.animatedValue.toString().toFloat())
            }
        }

        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                Log.d(DEBUG_TAG, "onAnimationStart()")
            }

            override fun onAnimationEnd(p0: Animator?) {
                circularProgressListener.let {
                    it?.onProgressEnd()
                }
                Log.d(DEBUG_TAG, "onAnimationEnd()")
            }

            override fun onAnimationCancel(p0: Animator?) {
                Log.d(DEBUG_TAG, "onAnimationCancel()")
            }

            override fun onAnimationRepeat(p0: Animator?) {
                Log.d(DEBUG_TAG, "onAnimationRepeat()")
            }
        })
    }

    fun startAnimation() {
        if (objectAnimator.isRunning || !objectAnimator.isStarted) {
            objectAnimator.cancel()
            objectAnimator.start()
            circularProgressListener.let {
                it?.onProgressStart()
            }
        }
    }

    fun resumeAnimation() {
        if (objectAnimator.isPaused) {
            objectAnimator.resume()
            circularProgressListener.let {
                it?.onProgressResume()
            }
        }
    }

    fun pauseAnimation() {
        if (objectAnimator.isRunning && !objectAnimator.isPaused) {
            objectAnimator.pause()
            circularProgressListener.let {
                it?.onProgressPause()
            }
        }
    }

    fun stopAnimation() {
        objectAnimator.cancel()
    }

    companion object {
        const val DEFAULT_ANIMATION_DURATION = 5000L
        const val MAX_PROGRESS = 360f
        const val DEFAULT_PROGRESS_TEXT = "100"
        const val DEFAULT_ANGLE = 160f
        const val START_ANGLE = -90f
        const val DEBUG_TAG = "CIRCULAR_PROGRESS"
    }

    enum class ColorMode {
        BLUE, RED, GREEN
    }

    interface CircularProgressListener {
        fun onProgressValue(progress: Float)
        fun onProgressStart()
        fun onProgressPause()
        fun onProgressResume()
        fun onProgressEnd()
    }
}