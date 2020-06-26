package com.urbandroid.dontkillmyapp.domain

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.urbandroid.dontkillmyapp.*
import java.util.*

data class Benchmark(val from : Long, var to : Long) {

    var running : Boolean = true

    val workEvents = mutableListOf<Long>()
    val mainEvents = mutableListOf<Long>()
    val alarmEvents = mutableListOf<Long>()

    fun getWorkResult() : Float {
        return ((workEvents.size + 1) / ((to - from) / WORK_REPEAT_MS.toFloat())).coerceAtMost(1f)
    }

    fun getTotalResult() : Float {
        return (getWorkResult() + (2 * getAlarmResult()) + getMainResult()) / 4f
    }

    fun getMainResult() : Float {
        return ((mainEvents.size + 1) / ((to - from) / MAIN_REPEAT_MS.toFloat())).coerceAtMost(1f)
    }

    fun getAlarmResult() : Float {
        return ((alarmEvents.size + 1) / ((to - from) / (ALARM_REPEAT_MS + ALARM_REPEAT_MARGIN_MS).toFloat())).coerceAtMost(1f)
    }

    fun getDuration() : Long {
        return to - from
    }

    fun getDurationSeconds() : Long {
        return (to - from) / 1000
    }


    override fun toString(): String {
        return "Benchmark ${Date(from)} ${Date(to)} MAIN ${mainEvents.size} WORK ${workEvents.size} ALARM ${alarmEvents.size}"
    }

    companion object {

        fun load(context : Context) : Benchmark? {
            val json = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_BENCHMARK, null)
            json?.apply {
                return Gson().fromJson(json, Benchmark::class.java)
            }
            return null
        }


        fun generateTextReport(context : Context, benchmark : Benchmark?) : String {
            return "${context.getString(R.string.app_name)} ${context.getString(R.string.report)} \n\n" +
                    "${Build.MODEL} (${Build.DEVICE}) by ${Build.MANUFACTURER}, Android ${Build.VERSION.RELEASE} \n\n" +
                    "TOTAL \n${Benchmark.formatResult(benchmark?.getTotalResult() ?: 0f)} \n\n" +
                    "WORK \n${Benchmark.formatResult(benchmark?.getWorkResult() ?: 0f)} \n\n" +
                    "MAIN \n${Benchmark.formatResult(benchmark?.getMainResult() ?: 0f)} \n\n" +
                    "ALARM \n${Benchmark.formatResult(benchmark?.getAlarmResult() ?: 0f)} \n\n" +
                    "DURATION \n${DateUtils.formatElapsedTime((benchmark?.getDurationSeconds() ?: 0))} \n\n" +
                    "Source: https://dontkillmyapp.com \n"
        }

        @SuppressLint("ApplySharedPref")
        fun save(context : Context, benchmark : Benchmark) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_BENCHMARK, Gson().toJson(benchmark)).commit()

            Log.i(TAG, "SAVE $benchmark")
        }

        @SuppressLint("ApplySharedPref")
        fun clear(context : Context) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_BENCHMARK, null).commit()
        }

        fun formatResult(result : Float) : String {
            return "${(result * 100).toInt()}%"
//            return "${DecimalFormat("0.0").format(result * 100)}%"
        }

        fun finishBenchmark(context : Context, benchmark: Benchmark) {
            benchmark.running = false
            benchmark.to = System.currentTimeMillis()
            save(context, benchmark)

            Log.i(TAG, "FINISH $benchmark")
        }

    }


}