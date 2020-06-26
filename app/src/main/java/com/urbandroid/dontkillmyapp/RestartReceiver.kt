package com.urbandroid.dontkillmyapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.urbandroid.dontkillmyapp.domain.Benchmark
import com.urbandroid.dontkillmyapp.service.BenchmarkService

class RestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "Receiver " + intent?.action)

        context?.apply {
            val currentBenchmark = Benchmark.load(context)
            if (currentBenchmark != null && currentBenchmark.running) {
                BenchmarkService.start(this)
            }

        }
    }
}