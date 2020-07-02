package com.urbandroid.dontkillmyapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_rate.*

class RateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)

        setTitle(R.string.rate)

        rate.setOnClickListener {
            val url = "$PLAY_STORE_PREFIX$packageName"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: java.lang.Exception) {
                Toast.makeText(this, "Cannot open $url", Toast.LENGTH_LONG).show()
            }
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(KEY_RATE_DONE, true).apply()
            finish()
        }

        later.setOnClickListener {
            finish()
        }

        never.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(KEY_RATE_NEVER, true).apply()

            finish()
        }
    }

    companion object {
        fun start(context : Context) {
            if (
                !PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_RATE_DONE, false) &&
                !PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_RATE_NEVER, false)
            ) {
                context.startActivity(Intent(context, RateActivity::class.java))
            }
        }
    }
}