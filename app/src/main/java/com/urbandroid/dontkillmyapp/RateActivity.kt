package com.urbandroid.dontkillmyapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.urbandroid.dontkillmyapp.gui.EdgeToEdgeUtil
import com.urbandroid.dontkillmyapp.gui.ToolbarUtil
import java.util.concurrent.TimeUnit

class RateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)

        setTitle(R.string.rate)

        val rate = findViewById<View>(R.id.rate);
        val later = findViewById<View>(R.id.later);
        val never = findViewById<View>(R.id.never);

        ToolbarUtil.apply(this)
        EdgeToEdgeUtil.insetsBottom(findViewById<View>(R.id.bottom))

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
            setRateLater(this)
            finish()
        }

        never.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(KEY_RATE_NEVER, true).apply()

            finish()
        }
    }

    companion object {

        fun isRateDone(context : Context) : Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_RATE_DONE, false)
        }

        fun isTimeToRateAgain(context : Context) : Boolean {
            val ts = PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_RATE_LATER, -1)
            return ts == -1L || System.currentTimeMillis() - ts > TimeUnit.DAYS.toMillis(7)
        }

        fun getTimeToRateAgain(context : Context) : Long {
            return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_RATE_LATER, -1)
        }

        fun isRateNever(context : Context) : Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_RATE_NEVER, false)
        }

        fun setRateLater(context : Context) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_RATE_LATER, System.currentTimeMillis()).apply()
        }

        fun shouldStartRating(context : Context) : Boolean {
            return !isRateDone(context) && !isRateNever(context) && isTimeToRateAgain(context)
        }

        fun start(context : Context) {
            if (shouldStartRating(context)) {
                context.startActivity(Intent(context, RateActivity::class.java))
            }
        }
    }
}