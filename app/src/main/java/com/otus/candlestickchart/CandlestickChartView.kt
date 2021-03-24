package com.otus.candlestickchart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import java.text.SimpleDateFormat
import java.util.*

private const val DEFAULT_OFFSET = 60f

class CandlestickChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    data class Candle(
        val tradeDate: Date,
        val openPrice: Float,
        val closePrice: Float,
        val maxPrice: Float,
        val minPrice: Float
    )

    private val gestureDetector = GestureDetector(context, CsCvGestureDetector())

    var isZoomed = false

    private val textBounds = Rect()
    private val userPoint = PointF(0f, 0f)

    private var candlesDataList = emptyList<Candle>()
    private var maxPriceInDataSet = 0f

    @ColorInt
    private var bodyClosedColor = Color.RED

    @ColorInt
    private var bodyOpenedColor = Color.GREEN
    private var priceLineWidth = 4f
    private var candleBodyWidth = 50f
    private var candleBodyWidthScale = 0.5f

    init {
        val attrs = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.CandlestickChartView,
            defStyleAttr,
            defStyleRes
        )
        try {
            bodyClosedColor =
                attrs.getColor(R.styleable.CandlestickChartView_cscv_close_color, bodyClosedColor)
            bodyOpenedColor =
                attrs.getColor(R.styleable.CandlestickChartView_cscv_open_color, bodyOpenedColor)
            candleBodyWidth =
                attrs.getDimension(
                    R.styleable.CandlestickChartView_cscv_candle_body_width,
                    candleBodyWidth
                )
            priceLineWidth = attrs.getDimension(
                R.styleable.CandlestickChartView_cscv_price_line_width,
                priceLineWidth
            )
        } finally {
            attrs.recycle()
        }

    }

    private val coordinatesPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            alpha = 200
            strokeWidth = 5f
        }
    }

    private val candleBodyPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private val candleBodyBorder by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = getHalfLineWidth()
        }
    }

    private val priceDynamicPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = priceLineWidth
        }
    }


    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 200
    }

    fun setCandles(candles: List<Candle>) {
        candlesDataList = candles.distinctBy { it.tradeDate }.sortedBy { it.tradeDate }
        maxPriceInDataSet = candles.maxBy { it.maxPrice }?.maxPrice ?: maxPriceInDataSet
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawUserPointer(canvas)
        canvas.save()
        drawAbscissaLine(canvas)
        drawOrdinateLine(canvas)
        drawLineSections(canvas)
        candlesDataList
            .forEachIndexed { index, candle ->
                drawCandle(canvas, index, candle)
            }
        canvas.restore()
    }

    private fun drawUserPointer(canvas: Canvas) {
        canvas.drawLine(
            userPoint.x,
            height - DEFAULT_OFFSET - getHalfLineWidth(),
            userPoint.x,
            userPoint.y,
            coordinatesPaint
        )
        canvas.drawLine(DEFAULT_OFFSET, userPoint.y, userPoint.x, userPoint.y, coordinatesPaint)
        val yHeight = height - DEFAULT_OFFSET
        val scaledYPoint = yHeight - userPoint.y + getHalfLineWidth() * 2
        val pointPrice = (maxPriceInDataSet * (scaledYPoint) / yHeight).toString()
        textPaint.getTextBounds(pointPrice, 0, pointPrice.length, textBounds)
        val xPos = userPoint.x
        val yPos = userPoint.y - coordinatesPaint.strokeWidth
        canvas.drawText(pointPrice, xPos, yPos, textPaint)
    }

    private fun drawCandle(canvas: Canvas, index: Int, candle: Candle) {
        val oneSegmentXOffset = (width - DEFAULT_OFFSET) / candlesDataList.size
        val elementOffset = index + 1
        var candleScaledWidth = candleBodyWidth
        while (oneSegmentXOffset <= candleScaledWidth) {
            candleScaledWidth *= candleBodyWidthScale
        }

        val segmentX =
            oneSegmentXOffset * elementOffset - candleScaledWidth / 2f - getHalfLineWidth()
        val minPriceDynamicStartY =
            height.toFloat() - (candle.minPrice / maxPriceInDataSet) * (height - getHalfLineWidth() * 2 - DEFAULT_OFFSET)
        val maxPriceDynamicEndY =
            height.toFloat() - (candle.maxPrice / maxPriceInDataSet) * (height - getHalfLineWidth() * 2 - DEFAULT_OFFSET)
        canvas.drawLine(
            segmentX,
            minPriceDynamicStartY,
            segmentX,
            maxPriceDynamicEndY,
            priceDynamicPaint
        )
        if (candle.closePrice > candle.openPrice) {
            candleBodyPaint.color = bodyOpenedColor
        } else {
            candleBodyPaint.color = bodyClosedColor
        }
        val bodyOpenStartY =
            height.toFloat() - (candle.openPrice / maxPriceInDataSet) * (height - getHalfLineWidth() * 2 - DEFAULT_OFFSET)
        val bodyMaxCloseEndY =
            height.toFloat() - (candle.closePrice / maxPriceInDataSet) * (height - getHalfLineWidth() * 2 - DEFAULT_OFFSET)
        canvas.drawRect(
            segmentX - candleScaledWidth * 0.5f,
            bodyOpenStartY,
            segmentX + candleScaledWidth * 0.5f,
            bodyMaxCloseEndY,
            candleBodyPaint
        )
        canvas.drawRect(
            segmentX - candleScaledWidth * 0.5f,
            bodyOpenStartY,
            segmentX + candleScaledWidth * 0.5f,
            bodyMaxCloseEndY,
            candleBodyBorder
        )

    }

    private fun drawOrdinateLine(canvas: Canvas) {
        canvas.translate(DEFAULT_OFFSET, -DEFAULT_OFFSET)
        canvas.drawLine(
            0f,
            DEFAULT_OFFSET + getHalfLineWidth(),
            0f,
            height.toFloat() + getHalfLineWidth(),
            coordinatesPaint
        )
    }

    private fun drawAbscissaLine(canvas: Canvas) {
        val oneSegmentXOffset = (width - DEFAULT_OFFSET) / candlesDataList.size
        var candleScaledWidth = candleBodyWidth
        while (oneSegmentXOffset <= candleScaledWidth) {
            candleScaledWidth *= candleBodyWidthScale
        }
        canvas.drawLine(
            0f,
            height.toFloat(),
            width.toFloat() - DEFAULT_OFFSET - candleScaledWidth / 2f,
            height.toFloat(),
            coordinatesPaint
        )
    }

    private fun drawLineSections(canvas: Canvas) {
        if (candlesDataList.isEmpty()) return
        candlesDataList.forEachIndexed { index, candle ->
            drawSegments(canvas, index, candle)
            drawCandle(canvas, index, candle)
        }

        drawPriceSegments(canvas)
    }

    private fun drawPriceSegments(canvas: Canvas) {
        val segmentSize = 15f
        val priceRange = 0..if (isZoomed) candlesDataList.size * 4 else {
            candlesDataList.size
        }
        val oneSegmentYOffset = (height.toFloat() - DEFAULT_OFFSET) / priceRange.last
        for (index in priceRange) {
            val elementOffset = index + 1
            val segmentY = height - oneSegmentYOffset * elementOffset + getHalfLineWidth() * 2
            canvas.drawLine(-segmentSize, segmentY, 0f, segmentY, coordinatesPaint)
            drawPriceOrdinateSegment(canvas, index, segmentY, priceRange.last)
        }
    }

    private fun drawSegments(canvas: Canvas, index: Int, candle: Candle) {
        val oneSegmentXOffset = (width - DEFAULT_OFFSET) / candlesDataList.size
        var candleScaledWidth = candleBodyWidth
        while (oneSegmentXOffset <= candleScaledWidth) {
            candleScaledWidth *= candleBodyWidthScale
        }
        val elementOffset = index + 1
        val segmentSize = 15f
        val segmentX =
            oneSegmentXOffset * elementOffset - candleScaledWidth / 2f - getHalfLineWidth()
        val segmentStartY = height.toFloat()
        val segmentStopY = segmentStartY + segmentSize
        canvas.drawLine(segmentX, segmentStartY, segmentX, segmentStopY, coordinatesPaint)
        drawDatesOnAbscissaSegment(canvas, candle.tradeDate, segmentX)
    }

    private fun drawDatesOnAbscissaSegment(canvas: Canvas, tradeDate: Date, segmentX: Float) {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 30f
        val text = formatDate(tradeDate)
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val xPos = segmentX - textBounds.right / 2f - coordinatesPaint.strokeWidth
        val yPos =
            canvas.height - textPaint.descent() - textPaint.ascent() + coordinatesPaint.strokeWidth
        canvas.drawText(text, xPos, yPos, textPaint);
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd.MM", Locale.getDefault()).format(date)
    }

    private fun drawPriceOrdinateSegment(
        canvas: Canvas,
        index: Int,
        segmentY: Float,
        scaleSize: Int
    ) {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 30f
        val maxPrice = candlesDataList.maxBy { it.maxPrice }!!
        val priceStep = maxPrice.maxPrice / scaleSize
        val nextPrice = ((index + 1) * priceStep).toInt().toString()
        textPaint.getTextBounds(nextPrice, 0, nextPrice.length, textBounds)
        val xPos = -DEFAULT_OFFSET / 2f
        val yPos =
            segmentY - textPaint.descent() - textPaint.ascent() + coordinatesPaint.strokeWidth
        canvas.drawText(nextPrice, xPos, yPos, textPaint)
    }

    private fun getHalfLineWidth(): Float {
        return coordinatesPaint.strokeWidth / 2f
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        return when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_MOVE -> {
                userPoint.x = event.x
                userPoint.y = event.y
                invalidate()
                return false
            }
            else -> super.onTouchEvent(event)
        }
    }

    inner class CsCvGestureDetector : GestureDetector.SimpleOnGestureListener() {


        override fun onDoubleTap(e: MotionEvent): Boolean {
            isZoomed = !isZoomed
            invalidate()
            return super.onDoubleTap(e)
        }
    }
}