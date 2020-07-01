package com.urbandroid.dontkillmyapp

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.how_it_works -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.how_it_works)
                builder.setMessage(R.string.how_it_works_text)
                builder.setPositiveButton(R.string.ok, null)
                builder.show()
            }
            R.id.support -> {
                var subject = "DontKillMyApp Feedback"
                var body = ""

                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@urbandroid.com?&subject=" + Uri.encode(subject) +
                        "&body="))

                i.putExtra(Intent.EXTRA_SUBJECT, subject)
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.contact_support)))
                } catch (e: Exception) {
                    Log.e(TAG, "Error $e")
                }

            }
            R.id.tell_others -> {
                var subject = "${getString(R.string.app_name)} ${getString(R.string.benchmark)}"
                var body = getString(R.string.tell_others_text)

                val i = Intent(Intent.ACTION_SEND)
                i.setType("text/plain")

                i.putExtra(Intent.EXTRA_SUBJECT, subject)
                i.putExtra(Intent.EXTRA_TEXT, body)
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.tell_others)))
                } catch (e: Exception) {
                    Log.e(TAG, "Error $e")
                }

            }
        }
        return true
    }

}