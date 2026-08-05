package com.nxtbit.emobies_24

import io.flutter.app.FlutterApplication

class EmobiesApplication : FlutterApplication() {
    companion object {
        var crashInfo: String? = null
    }

    override fun onCreate() {
        try {
            super.onCreate()
        } catch (t: Throwable) {
            crashInfo = "APPLICATION CRASH:\n\n$t\n\n${t.stackTraceToString()}"
        }
    }
}
