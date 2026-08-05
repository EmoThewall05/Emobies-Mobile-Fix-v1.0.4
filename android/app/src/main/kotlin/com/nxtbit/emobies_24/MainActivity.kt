package com.nxtbit.emobies_24

import android.graphics.Color
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import io.flutter.embedding.android.FlutterActivity
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity: FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
        } catch (t: Throwable) {
            showCrashScreen(t)
        }
    }

    private fun showCrashScreen(t: Throwable) {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        val tv = TextView(this).apply {
            text = "NATIVE CRASH:\n\n$t\n\n$sw"
            setTextColor(Color.RED)
            setBackgroundColor(Color.WHITE)
            textSize = 10f
            setPadding(24, 24, 24, 24)
        }
        val scroll = ScrollView(this)
        scroll.addView(tv)
        setContentView(scroll)
    }
}
