package com.otus.candlestickchart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<CandlestickChartView>(R.id.candle_chart).setCandles(
			mutableListOf(
				CandlestickChartView.Candle(Date(2021, 5, 10), 100f, 150f, 188f, 100f),
				CandlestickChartView.Candle(Date(2021, 5, 23), 100f, 50f, 200f, 30f),
				CandlestickChartView.Candle(Date(2021, 2, 5), 200f, 5f, 10f, 0f),
				CandlestickChartView.Candle(Date(2021, 7, 23), 133f, 10f, 220f, 5f)
			)
		)
    }
}