package com.urbandroid.dontkillmyapp.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.urbandroid.dontkillmyapp.*
import com.urbandroid.dontkillmyapp.domain.Benchmark
import java.text.DateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class BenchmarkService : Service() {

    lateinit var currentBenchmark : Benchmark

    var wakeLock: PowerManager.WakeLock? = null

    lateinit var h : Handler

    var executor : ScheduledExecutorService? = null

    private val receiver = object:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "Broadcast ${intent?.action}")

            context?.let {
                when(intent?.action) {
                    ACTION_ALARM -> {
                        Log.i(TAG, "Alarm")
                        currentBenchmark.alarmEvents.add(System.currentTimeMillis())
                        scheduleAlarm()
                        checkBenchmarkEnd(currentBenchmark)
                    }
                    else -> return
                }
            }
        }
    }

    private val mainRunnable = object:Runnable {
        override fun run() {
            Log.i(TAG, "Main")
            h.postDelayed(this, MAIN_REPEAT_MS)
            currentBenchmark.mainEvents.add(System.currentTimeMillis())
            checkBenchmarkEnd(currentBenchmark)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "onCreate")

        RUNNING = true

        h = Handler()

        val now = System.currentTimeMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelName = getString(R.string.benchmark);
            val importance = NotificationManager.IMPORTANCE_LOW;
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_FOREGROUND, channelName, importance);
            notificationChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(notificationChannel);

            val channelNameReport = getString(R.string.report);
            val importanceReport = NotificationManager.IMPORTANCE_HIGH;
            val notificationChannelReport = NotificationChannel(NOTIFICATION_CHANNEL_REPORT, channelNameReport, importanceReport);
            notificationChannelReport.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannelReport);
        }

        val duration = PreferenceManager.getDefaultSharedPreferences(this).getLong(KEY_BENCHMARK_DURATION, BENCHMARK_DURATION)

        currentBenchmark = Benchmark.load(this) ?: Benchmark(now, now + duration).also {
            Benchmark.save(this, it)
        }

        Log.i(TAG, "Benchmark $currentBenchmark RUNNING ${currentBenchmark.running}")

        val i = Intent(this, ResultActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pi = PendingIntent.getActivity(this, 4242, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_FOREGROUND)
            .setSmallIcon(R.drawable.ic_dkma)
            .setChannelId(NOTIFICATION_CHANNEL_FOREGROUND)
            .setColor(resources.getColor(R.color.colorAccent))
            .setContentIntent(pi)
            .addAction(0, getString(R.string.stop), pi)
            .setShowWhen(false)
            .setContentText(getString(R.string.running, DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(currentBenchmark.from)), DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(currentBenchmark.to))))

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            notificationBuilder.setContentTitle(getString(R.string.app_name))
        }

        startForeground(2342, notificationBuilder.build())

        checkBenchmarkEnd(currentBenchmark)

        currentBenchmark.workEvents.add(System.currentTimeMillis())

        executor = Executors.newScheduledThreadPool(1)
        executor?.scheduleAtFixedRate(Runnable {
            h.post(Runnable {
                Log.i(TAG, "Work")
                currentBenchmark.workEvents.add(System.currentTimeMillis())

                checkBenchmarkEnd(currentBenchmark)
            })
        }, WORK_REPEAT_MS, WORK_REPEAT_MS, TimeUnit.MILLISECONDS)

        h.post(mainRunnable)

        registerReceiver(receiver, IntentFilter(ACTION_ALARM));
        scheduleAlarm()

        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DKMA::BenchmarkWakeLock").apply {
                    acquire()
                }
            }
    }

    private fun checkBenchmarkEnd(benchmark : Benchmark) : Boolean {
        Log.i(TAG, "Benchmark running ${benchmark.running}")
        Benchmark.save(this, currentBenchmark)

        if (!benchmark.running) {
            Log.i(TAG, "Benchmark not running, stop service")
            stopSelf()
            return true
        }
        if (System.currentTimeMillis() > benchmark.to) {
            Log.i(TAG, "Benchmark completed, finishing")
            Benchmark.finishBenchmark(this, benchmark)
            showFinishNotification()
            stopSelf()
            return true
        }
        return false
    }

    private fun showFinishNotification() {
        val i = Intent(this, ResultActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pi = PendingIntent.getActivity(this, 4242, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_FOREGROUND)
            .setSmallIcon(R.drawable.ic_dkma)
            .setChannelId(NOTIFICATION_CHANNEL_REPORT)
            .setColor(resources.getColor(R.color.colorAccent))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setShowWhen(false)
            .setContentText(getString(R.string.finished))

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            notificationBuilder.setContentTitle(getString(R.string.app_name))
        }

        with(NotificationManagerCompat.from(this)) {
            notify(4242, notificationBuilder.build())
        }

    }

    private fun getAlarmIntent() : PendingIntent {
        val i = Intent(ACTION_ALARM)
        i.setPackage(packageName)
        return PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun scheduleAlarm() {
        Log.i(TAG, "Scheduling at alarm ${Date(System.currentTimeMillis() + ALARM_REPEAT_MS)}")
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + ALARM_REPEAT_MS,
                    getAlarmIntent()
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExact(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + ALARM_REPEAT_MS,
                    getAlarmIntent()
                )
            }
            else -> {
                (getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + ALARM_REPEAT_MS,
                    getAlarmIntent()
                )
            }
        }
    }

    private fun cancelAlarm() {
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(getAlarmIntent())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)

        cancelAlarm()

        h.removeCallbacks(mainRunnable)

        //supervisorJob.cancelChildren()

        wakeLock?.release()

        executor?.shutdown()

        RUNNING = false
    }

    companion object {

        var RUNNING : Boolean = false

        fun start(context : Context) {
            Log.i(TAG, "Starting service")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, BenchmarkService::class.java))
            } else {
                context.startService(Intent(context, BenchmarkService::class.java))
            }
        }

        fun stop(context : Context) {
            context.stopService(Intent(context, BenchmarkService::class.java))
        }
    }
}