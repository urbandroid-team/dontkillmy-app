package com.urbandroid.dontkillmyapp

import java.util.concurrent.TimeUnit

val ALARM_REPEAT_MS : Long = TimeUnit.MINUTES.toMillis(10)
const val ALARM_REPEAT_MARGIN_MS : Long = 30000
val WORK_REPEAT_MS : Long = TimeUnit.SECONDS.toMillis(10)
val MAIN_REPEAT_MS : Long = TimeUnit.SECONDS.toMillis(10)

val HOUR_IN_MS : Long = TimeUnit.HOURS.toMillis(1)
val BENCHMARK_DURATION : Long = HOUR_IN_MS * 3

const val KEY_RATE_LATER : String = "Rate_later_1"
const val KEY_RATE_NEVER : String = "Rate_never_1"
const val KEY_RATE_DONE : String = "Rate_done_1"
const val KEY_BENCHMARK : String = "Benchmark"
const val KEY_BENCHMARK_DURATION : String = "Benchmark_duration"

const val NOTIFICATION_CHANNEL_FOREGROUND = "foreground"
const val NOTIFICATION_CHANNEL_REPORT = "report"

const val ACTION_ALARM = "com.urbandroid.dontkillmyapp.ACTION_ALARM"
const val EXTRA_DURATION_MS : String = "extra_duration_ms"

const val TAG = "DKMA"

const val PLAY_STORE_PREFIX = "market://details?id="
