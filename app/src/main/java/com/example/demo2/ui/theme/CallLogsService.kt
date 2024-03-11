package com.example.demo2.ui.theme

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CallLogsService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Perform your call log and recorded call fetching here
        return START_STICKY
    }
}