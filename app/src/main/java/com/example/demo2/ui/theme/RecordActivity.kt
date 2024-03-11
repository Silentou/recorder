package com.example.demo2.ui.theme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.demo2.MainActivity
import com.example.demo2.R
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class RecordActivity : AppCompatActivity() {
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private val recordedCallsList: MutableList<RecordedCall> = mutableListOf()
    private var currentRecordingStartTimeMillis: Long = 0
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordCallAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        val recordedCallsList = intent.getSerializableExtra(EXTRA_RECORDED_CALLS) as ArrayList<RecordedCall>
        Log.d(TAG, "onCreate: $recordedCallsList")
        swipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout)
        swipeRefreshLayout?.setOnRefreshListener {
            recyclerView = findViewById(R.id.activity_rv)
            adapter = RecordCallAdapter(recordedCallsList)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.updateData(recordedCallsList)
            swipeRefreshLayout?.isRefreshing = false

        }
        if (checkPermission()) {
            startForegroundService(Intent(this, CallLogsService::class.java))
        } else {
            requestPermissions()
        }
        recyclerView = findViewById(R.id.activity_rv)
        adapter = RecordCallAdapter(recordedCallsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.updateData(recordedCallsList)
    }

    private fun checkPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALL_LOG),
                MainActivity.PERMISSIONS_REQUEST_READ_CALL_LOGS
            )
            false
        } else {
            true
        }
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO
            ),
            MainActivity.REQUEST_PERMISSIONS
        )
    }
    companion object {
        private const val PERMISSIONS_REQUEST_READ_CALL_LOGS = 100
        const val TAG = "RecordActivity"
        const val EXTRA_RECORDED_CALLS = "extra_recorded_calls"

        const val REQUEST_PERMISSIONS = 123
    }
}