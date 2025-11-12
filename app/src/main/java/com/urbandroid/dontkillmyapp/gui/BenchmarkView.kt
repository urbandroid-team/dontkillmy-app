package com.urbandroid.dontkillmyapp.gui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.urbandroid.dontkillmyapp.domain.Benchmark
import java.util.concurrent.TimeUnit

class BenchmarkView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var benchmark : Benchmark? = null

    var p : Paint = Paint()

    val timePerLine = TimeUnit.MINUTES.toMillis(5)

    fun getDip(context: Context, pixel: Int): Int {
        return (pixel.toFloat() * context.resources
            .displayMetrics.density + 0.5f).toInt()
    }

    fun refresh() {
        val latest = Benchmark.load(this.context)
        latest?.let {
            benchmark = it
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        benchmark?.let {benchmark ->

            p.style = Paint.Style.FILL
            p.color = Color.parseColor("#e57373")

            val lines = benchmark.getDuration() / timePerLine

            canvas?.let {
                val size = getDip(context, 4).toFloat()

                p.color = Color.parseColor("#4CAF50")

                benchmark.workEvents.forEach{
                    val x = computeX(benchmark.from, benchmark.to, it, canvas, size)
                    val y = computeY(benchmark.from, benchmark.to, it, canvas, size)

                    canvas.drawCircle(x, y, size, p)
                }

                p.color = Color.parseColor("#FB8C00")

                benchmark.mainEvents.forEach{
                    val triangleSize = size * 0.9f

                    val x = computeX(benchmark.from, benchmark.to, it, canvas, size)
                    val y = computeY(benchmark.from, benchmark.to, it, canvas, size)

                    canvas.drawPath(Path().apply {
                        moveTo(x-triangleSize, y + triangleSize)
                        lineTo(x+triangleSize, y + triangleSize)
                        lineTo(x, y - size)
                        lineTo(x - triangleSize, y + triangleSize)
                    }, p)
                }

                p.color = Color.parseColor("#3F51B5")
                benchmark.alarmEvents.forEach{
                    val x = computeX(benchmark.from, benchmark.to, it, canvas, size)
                    val y = computeY(benchmark.from, benchmark.to, it, canvas, size)
                    canvas.drawRect(x - size, y - size, x + size, y + size, p)
                }
            }
        }

    }

    private fun computeX(from : Long, to : Long, ts : Long, canvas : Canvas, size : Float) : Float {
        return (((ts - from) % timePerLine / timePerLine.toFloat()) * (canvas.width - size)) + size
    }

    private fun computeY(from : Long, to : Long, ts : Long, canvas : Canvas, size : Float) : Float {
        return (((ts - from) / timePerLine) * (size * 4)) + size
    }

}