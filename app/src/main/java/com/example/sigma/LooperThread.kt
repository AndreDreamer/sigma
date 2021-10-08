package com.example.sigma

import android.os.Handler
import android.os.Looper

class LooperThread : Thread() {
    var handler: Handler? = null
    override fun run() {
        Looper.prepare()
        handler = Looper.myLooper()?.let { Handler(it) }
        Looper.loop()

    }

}