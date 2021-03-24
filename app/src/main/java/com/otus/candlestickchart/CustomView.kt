package com.otus.candlestickchart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

private const val DEFAULT_OFFSET = 60f

class CustomView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private var pathColor = Color.DKGRAY

    private val animator = ValueAnimator.ofArgb(Color.BLACK, Color.RED).apply {
        addUpdateListener {
            pathColor = it.animatedValue as Int
            invalidate()
        }
        repeatCount = ValueAnimator.INFINITE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (animator.isRunning) return false
                animator.start()
                return true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private val paint = Paint().apply {
        strokeWidth = 100f
        color = Color.BLACK
    }

//    private val transformMatrix = Matrix()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        transformMatrix.apply {
//            preTranslate(0f, height.toFloat())
//            preScale(1f, -1f)
//        }
//        canvas.concat(transformMatrix)
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, paint)
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), paint)
        val path = Path()
        path.setFillType(Path.FillType.EVEN_ODD)
        path.lineTo(100f, 150f)
        path.moveTo(300f, 150f)
        path.addRect(RectF(100f, 100f, 150f, 150f), Path.Direction.CCW)
        path.close()
        canvas.drawPath(path, Paint().apply {
            setColor(
                pathColor
            )
        })

    }
}