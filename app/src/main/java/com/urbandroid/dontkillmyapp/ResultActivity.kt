package com.urbandroid.dontkillmyapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.google.android.play.core.review.ReviewManagerFactory
import com.urbandroid.dontkillmyapp.domain.Benchmark
import com.urbandroid.dontkillmyapp.gui.BenchmarkView
import com.urbandroid.dontkillmyapp.service.BenchmarkService
import kotlinx.android.synthetic.main.activity_result.*
import java.util.*
import kotlin.math.roundToInt


class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        supportActionBar?.setDisplayShowHomeEnabled(true)

        title = "${getString(R.string.app_name)} ${getString(R.string.report)}"

//        doki_content.loadContent(Build.MANUFACTURER)
//
//        doki_content.loadContent(Build.MANUFACTURER)
        doki_content.setButtonsVisibility(false)
        doki_content.findViewById<View>(dev.doubledot.doki.R.id.manufacturerRating).visibility = View.GONE
        doki_content.findViewById<View>(dev.doubledot.doki.R.id.manufacturerRatingHeader).visibility = View.GONE
        val parent = doki_content.findViewById<ViewGroup>(dev.doubledot.doki.R.id.doki_full_content)
        val contentView = parent[1] as ViewGroup

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val reportLayout = inflater.inflate(R.layout.report, null) as ViewGroup

        contentView.removeAllViews()
        contentView.addView(reportLayout)

        val currentBenchmark = Benchmark.load(this)

        if (currentBenchmark == null) {
            finish()
        }


        currentBenchmark?.let {
            Log.i(TAG, currentBenchmark.toString())

            if (currentBenchmark.running) {
                BenchmarkService.stop(this)
                Benchmark.finishBenchmark(this, currentBenchmark)
            }

            val chart = reportLayout.findViewById<ViewGroup>(R.id.chart)
            chart.addView(BenchmarkView(this, null, 0, it))

            reportLayout.findViewById<TextView>(R.id.total).text = Benchmark.formatResult(it.getTotalResult())
            reportLayout.findViewById<TextView>(R.id.work).text = Benchmark.formatResult(it.getWorkResult())
            reportLayout.findViewById<TextView>(R.id.alarm).text = Benchmark.formatResult(it.getAlarmResult())
            reportLayout.findViewById<TextView>(R.id.main).text = Benchmark.formatResult(it.getMainResult())

            //Log.i(TAG, "Coroutine result: "+Benchmark.formatResult(it.getCoroutineResult()))
        }

        done.setOnClickListener {
            Benchmark.clear(this)
            RateActivity.start(this)
            finish()
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
            context.startActivity(Intent(context, ResultActivity::class.java))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_result, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.how_it_works -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
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

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
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
}