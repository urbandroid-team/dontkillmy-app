package com.urbandroid.dontkillmyapp

import android.Manifest.permission
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources.getSystem
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.get
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urbandroid.dontkillmyapp.domain.Benchmark
import com.urbandroid.dontkillmyapp.gui.EdgeToEdgeUtil
import com.urbandroid.dontkillmyapp.gui.ToolbarUtil
import com.urbandroid.dontkillmyapp.service.BenchmarkService
import dev.doubledot.doki.views.DokiContentView

class MainActivity : AppCompatActivity() {

    val launchServiceAfterPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.all { true }) {
            startBenchmark()
        }
    }
    val chooseDurationAfterPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.all { true }) {
            chooseDuration()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val topView = findViewById<View>(dev.doubledot.doki.R.id.deviceManufacturerHeader)

        val param = topView.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(0, 64.dp,0,0)
        topView.layoutParams = param

        val appBar = findViewById<ViewGroup>(dev.doubledot.doki.R.id.appbar)
        val cToolbar = (appBar.get(0) as ViewGroup).get(0) as ViewGroup
        var fab = findViewById<View>(R.id.fab)
        var stop = findViewById<View>(R.id.stop)
        var toolbar = layoutInflater.inflate(R.layout.view_toolbar, cToolbar, false) as Toolbar
        cToolbar.addView(toolbar, 0)


        ToolbarUtil.apply(this)
        supportActionBar?.setTitle("")

        EdgeToEdgeUtil.insetsTop(appBar)
        EdgeToEdgeUtil.insetsBottom(fab)

        fab.setOnClickListener {
            if (missingPermissions()) {
                requestPermissions(launchServiceAfterPermission)
            } else {
                Log.i(TAG, "start clicked")
                if (BenchmarkService.RUNNING) {
                    ResultActivity.start(this)
                    finish()
//                Toast.makeText(this, R.string.already_running, Toast.LENGTH_LONG).show()
                } else {
                    chooseDuration()
                }
            }
        }
    }


    fun missingPermissions() : Boolean {
        return !hasNotificationPermission() || !hasExactAlarmPermission(this)
    }
    fun hasNotificationPermission() : Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || NotificationManagerCompat.from(this).areNotificationsEnabled()
    }


    fun requestPermissions(resultLauncher : ActivityResultLauncher<Array<String>>) {
        var permissions = mutableListOf<String>()
        if (!hasExactAlarmPermission(this)) {
            permissions.add(permission.SCHEDULE_EXACT_ALARM)
//            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2) {
                showExactAlarmPermissionDialog(this)
//            }
        }
        if (!hasNotificationPermission()) {
            permissions.add(permission.POST_NOTIFICATIONS)
        }

        if (permissions.isNotEmpty()) {
            resultLauncher.launch(permissions.toTypedArray())
        }
    }

    fun chooseDuration() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(R.string.duration)

        val arrayAdapter = ArrayAdapter(
            this,
            R.layout.dialog_item, resources.getStringArray(R.array.duration_array)
        )

        builder.setNegativeButton(R.string.cancel, null)

        builder.setAdapter(arrayAdapter
        ) { _, which ->
            PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(
                KEY_BENCHMARK_DURATION, (which * HOUR_IN_MS) + HOUR_IN_MS
            ).apply()

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle(R.string.warning_title)
            builder.setMessage(R.string.warning_text)
            builder.setPositiveButton(
                R.string.ok
            ) { _, _ ->
                startBenchmark()
                ResultActivity.start(this)
                finish()
            }
            builder.setNegativeButton(R.string.cancel, null)

            builder.show()
        }
        builder.show()

    }

    fun startBenchmark() {
//        Toast.makeText(MainActivity@this, R.string.started, Toast.LENGTH_SHORT).show()
        BenchmarkService.start(this)
    }

    val Int.dp: Int get() = (this * getSystem().displayMetrics.density).toInt()

    fun hasExactAlarmPermission(context : Context) : Boolean {
        val am = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            return false
        }
        return true
    }

    fun showExactAlarmPermissionDialog(context : Context) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle(context.getString(R.string.exact_alarm_restrictions_title))
            builder.setMessage(context.getString(R.string.exact_alarm_restrictions_summary))
            builder.setPositiveButton(
                R.string.ok
            ) { _, _ ->
                try {
                    val intent = Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                } catch (e: java.lang.Exception) {
                    Toast.makeText(
                        context, R.string.general_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            builder.setNegativeButton(R.string.cancel, null)
            builder.show()
    }

    override fun onResume() {
        super.onResume()

        if (hasExactAlarmPermission(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                launchServiceAfterPermission.launch(arrayOf(permission.POST_NOTIFICATIONS))
            }
        }

        val doki_content = findViewById<DokiContentView>(R.id.doki_content)
        doki_content.loadContent()
        doki_content.setButtonsVisibility(false)

        val currentBenchmark = Benchmark.load(this)
        currentBenchmark?.let {
            ResultActivity.start(this)
            finish()
            return
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.how_it_works -> {
                val builder = MaterialAlertDialogBuilder(this)
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
                i.type = "text/plain"

                i.putExtra(Intent.EXTRA_SUBJECT, subject)
                i.putExtra(Intent.EXTRA_TEXT, body)
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.tell_others)))
                } catch (e: Exception) {
                    Log.e(TAG, "Error $e")
                }

            }
            R.id.rate -> {
                val url = "$PLAY_STORE_PREFIX$packageName"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this, "Cannot open $url", Toast.LENGTH_LONG).show()
                }
            }
            R.id.source -> {
                val url = "https://github.com/urbandroid-team/dontkillmy-app"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this, "Cannot open $url", Toast.LENGTH_LONG).show()
                }
            }
            R.id.translate -> {
                val url = "https://docs.google.com/spreadsheets/d/1DJ6nvdv2X8Q8e4NXu9VE0dT3SL72JX9evTmlDDALfRM/edit#gid=141810181"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this, "Cannot open $url", Toast.LENGTH_LONG).show()
                }
            }
        }
        return true
    }

}