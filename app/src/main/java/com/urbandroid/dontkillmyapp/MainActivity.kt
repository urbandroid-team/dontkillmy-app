package com.urbandroid.dontkillmyapp

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.urbandroid.dontkillmyapp.domain.Benchmark
import com.urbandroid.dontkillmyapp.service.BenchmarkService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener {
            Log.i(TAG, "start clicked")

            if (BenchmarkService.RUNNING) {
                Toast.makeText(this, R.string.already_running, Toast.LENGTH_LONG).show()
            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.duration)
//                builder.setMessage("${getString(R.string.warning_title)}: ${getString(R.string.warning_text)}")

                val arrayAdapter = ArrayAdapter<String>(
                    this,
                    R.layout.dialog_item, resources.getStringArray(R.array.duration_array)
                )

                builder.setNegativeButton(R.string.cancel, null)

                builder.setAdapter(arrayAdapter,
                    DialogInterface.OnClickListener { dialog, which ->
                        PreferenceManager.getDefaultSharedPreferences(MainActivity@this).edit().putLong(
                            KEY_BENCHMARK_DURATION, (which * HOUR_IN_MS) + HOUR_IN_MS).apply()

                        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                        builder.setTitle(R.string.warning_title)
                        builder.setMessage(R.string.warning_text)
                        builder.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                            BenchmarkService.start(this)
                        })
                        builder.setNegativeButton(R.string.cancel, null)

                        builder.show()
                    })
                builder.show()
            }


        }
    }


    override fun onResume() {
        super.onResume()

        doki_content.loadContent(Build.MANUFACTURER)
        doki_content.setButtonsVisibility(false)

        val currentBenchmark = Benchmark.load(this)
        currentBenchmark?.let {
            if (it.running) {
                BenchmarkService.start(this)
            } else {
                ResultActivity.start(this)
            }
        }

    }
}