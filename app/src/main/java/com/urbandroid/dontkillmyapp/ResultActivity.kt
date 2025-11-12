package com.urbandroid.dontkillmyapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources.getSystem
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory
import com.urbandroid.dontkillmyapp.domain.Benchmark
import com.urbandroid.dontkillmyapp.gui.BenchmarkView
import com.urbandroid.dontkillmyapp.gui.EdgeToEdgeUtil
import com.urbandroid.dontkillmyapp.gui.ToolbarUtil
import com.urbandroid.dontkillmyapp.service.BenchmarkService
import dev.doubledot.doki.views.DokiContentView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

val Int.dp: Int get() = (this * getSystem().displayMetrics.density).toInt()
class ResultActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    var benchmarkView : BenchmarkView? = null

    var reportLayout : ViewGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        title = "${getString(R.string.app_name)} ${getString(R.string.report)}"

        val doki_content = findViewById<DokiContentView>(R.id.doki_content)

        val topView = findViewById<View>(dev.doubledot.doki.R.id.deviceManufacturerHeader)

        val param = topView.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(0, 64.dp,0,0)
        topView.layoutParams = param

        val appBar = findViewById<ViewGroup>(dev.doubledot.doki.R.id.appbar)
        val cToolbar = (appBar.get(0) as ViewGroup).get(0) as ViewGroup
        var toolbar = layoutInflater.inflate(R.layout.view_toolbar, cToolbar, false) as Toolbar
        cToolbar.addView(toolbar, 0)

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowHomeEnabled(true)


        doki_content.setButtonsVisibility(false)
        doki_content.findViewById<View>(dev.doubledot.doki.R.id.manufacturerRating).visibility = View.GONE
        doki_content.findViewById<View>(dev.doubledot.doki.R.id.manufacturerRatingHeader).visibility = View.GONE
        val parent = doki_content.findViewById<ViewGroup>(dev.doubledot.doki.R.id.doki_full_content)
        val contentView = parent[1] as ViewGroup

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        reportLayout = inflater.inflate(R.layout.view_report, null) as ViewGroup

        contentView.removeAllViews()
        contentView.addView(reportLayout)

        ToolbarUtil.apply(this)
        EdgeToEdgeUtil.insetsBottom(findViewById<View>(R.id.done))
        EdgeToEdgeUtil.insetsTop(appBar)

        val currentBenchmark = try {
            Benchmark.load(this)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot load benchmark", e)
            Benchmark.clear(this)
            finish()
            return
        }

        if (currentBenchmark == null) {
            Log.i(TAG, "Benchmark null")
            finish()
            return
        }

        try {
            Log.i(TAG, currentBenchmark.toString())

            val chart = reportLayout?.findViewById<ViewGroup>(R.id.chart)
            benchmarkView = BenchmarkView(this, null, 0)
            chart?.addView(benchmarkView)

            benchmarkView?.refresh()

            reportLayout?.findViewById<TextView>(R.id.total)?.text = Benchmark.formatResult(currentBenchmark.getTotalResult())
            reportLayout?.findViewById<TextView>(R.id.work)?.text = Benchmark.formatResult(currentBenchmark.getWorkResult())
            reportLayout?.findViewById<TextView>(R.id.alarm)?.text = Benchmark.formatResult(currentBenchmark.getAlarmResult())
            reportLayout?.findViewById<TextView>(R.id.main)?.text = Benchmark.formatResult(currentBenchmark.getMainResult())
        } catch (e: Exception) {
            Log.e(TAG, "Cannot load benchmark - garbled - obfuscation", e)
            Benchmark.clear(this)
            finish()
            return
        }

        findViewById<View>(R.id.done).setOnClickListener {
            Benchmark.clear(this)
            RateActivity.start(this)
            finish()
        }

        findViewById<View>(R.id.stop).setOnClickListener {
            BenchmarkService.stop(this)
            refreshState()
        }

        if (RateActivity.getTimeToRateAgain(this) > 0 && RateActivity.isTimeToRateAgain(this) && currentBenchmark?.getTotalResult() ?: 0f > 0.9f && !RateActivity.isRateNever(this) && (System.currentTimeMillis() % 5 == 0L)) {
            val manager = ReviewManagerFactory.create(this@ResultActivity)

            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(this@ResultActivity, reviewInfo)
                    flow.addOnCompleteListener { result ->
                        RateActivity.setRateLater(this@ResultActivity)
                    }
                }
            }

        }
    }

    companion object {
        fun start(context : Context) {
            context.startActivity(Intent(context, ResultActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
    }

    var menuShare : MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_result, menu)

        menuShare = menu.findItem(R.id.share)

        menuShare?.setVisible(!BenchmarkService.RUNNING)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.how_it_works -> {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle(R.string.how_it_works)
                builder.setMessage(R.string.how_it_works_text)
                builder.setPositiveButton(R.string.ok, null)
                builder.show()
                return true
            }
            R.id.share -> {
                val benchmark = Benchmark.load(this)
                benchmark?.let { benchmark ->

                    val body = Benchmark.generateTextReport(this, benchmark)
                    val subject = "${getString(R.string.app_name)} ${getString(R.string.report)}"

                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle(R.string.share_with)
//                builder.setMessage("${getString(R.string.warning_title)}: ${getString(R.string.warning_text)}")

                    val arrayAdapter = ArrayAdapter<String>(
                        this,
                        R.layout.dialog_item, resources.getStringArray(R.array.share_array)
                    )

                    builder.setNegativeButton(R.string.cancel, null)

                    builder.setAdapter(arrayAdapter,
                        DialogInterface.OnClickListener { dialog, which ->
                            when(which) {
                                2 -> {
                                    val i = Intent(Intent.ACTION_SEND)
                                    i.type = "text/plain"

                                    var bitmapUri: Uri? = null
                                    try {
                                        bitmapUri = getBitmapUri(getBitmapFromView(findViewById(R.id.doki_content)))
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error $e")
                                    }

                                    i.putExtra(Intent.EXTRA_SUBJECT, subject)
                                    i.putExtra(Intent.EXTRA_TEXT, body)
                                    bitmapUri?.let{
                                        i.putExtra(Intent.EXTRA_STREAM, it);
                                    }
                                    try {
                                        startActivity(Intent.createChooser(i, getString(R.string.share)))
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error $e")
                                    }
                                }
                                1 -> {
                                    val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:?&subject=" + Uri.encode(subject) +
                                            "&body=" + Uri.encode(body)))

                                    var bitmapUri: Uri? = null
                                    try {
                                        bitmapUri = getBitmapUri(getBitmapFromView(findViewById(R.id.doki_content)))
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error $e")
                                    }

                                    i.putExtra(Intent.EXTRA_SUBJECT, subject)
                                    i.putExtra(Intent.EXTRA_TEXT, body)
                                    bitmapUri?.let{
                                        i.putExtra(Intent.EXTRA_STREAM, it);
                                    }
                                    try {
                                        startActivity(Intent.createChooser(i, getString(R.string.share)))
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error $e")
                                    }
                                }
                                0 -> {
                                    val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLScFa3YweHO33W50ifAV8nSRGOFVCPacoikyA53SzkQXeDlQPA/viewform?usp=pp_url&entry.394232744=${Uri.encode(Build.MODEL)}&entry.184762960=${Uri.encode(Build.VERSION.SDK_INT.toString())}&entry.1019994117=${(benchmark.getMainResult() * 100).roundToInt()}&entry.769591257=${(benchmark.getAlarmResult()* 100).roundToInt()}&entry.1490587715=${(benchmark.getWorkResult()* 100).roundToInt()}&entry.2139576680=${benchmark.getDurationSeconds()}&submit=Submit"))
                                    try {
                                        startActivity(i)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error $e")
                                    }
                                }

                            }
                        })
                    builder.show()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getBitmapUri(bitmap : Bitmap) : Uri {
        val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, bitmap,"DontKillMyApp Report ${Date(System.currentTimeMillis())}", null);
        return Uri.parse(bitmapPath);
    }

    fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

    fun refreshState() {
        benchmarkView?.refresh()
        val currentBenchmark = Benchmark.load(this)
        currentBenchmark?.let { currentBenchmark ->
            reportLayout?.findViewById<TextView>(R.id.total)?.text = Benchmark.formatResult(currentBenchmark.getTotalResult())
            reportLayout?.findViewById<TextView>(R.id.work)?.text = Benchmark.formatResult(currentBenchmark.getWorkResult())
            reportLayout?.findViewById<TextView>(R.id.alarm)?.text = Benchmark.formatResult(currentBenchmark.getAlarmResult())
            reportLayout?.findViewById<TextView>(R.id.main)?.text = Benchmark.formatResult(currentBenchmark.getMainResult())
        }

        menuShare?.setVisible(!BenchmarkService.RUNNING)

        if (BenchmarkService.RUNNING) {
            findViewById<View>(R.id.stop).visibility = View.VISIBLE
            findViewById<View>(R.id.done).visibility = View.GONE
            findViewById<View>(R.id.indicator).visibility = View.VISIBLE

        } else {
            findViewById<View>(R.id.stop).visibility = View.GONE
            findViewById<View>(R.id.done).visibility = View.VISIBLE
            findViewById<View>(R.id.indicator).visibility = View.GONE
        }
    }

    var dorefresh = true

    override fun onResume() {
        super.onResume()

        refreshState()

        dorefresh = true
        repeatRefresh()
    }

    override fun onPause() {
        super.onPause()

        dorefresh = false
        repeatRefresh().cancel()
    }

    fun repeatRefresh(): Job {
        return launch {
            while (dorefresh) {
                Log.i(TAG, "Refresh")
                delay(1000)
                refreshState()
            }
        }
    }
}