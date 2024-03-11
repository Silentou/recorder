package com.example.demo2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.demo2.databinding.ActivityMainBinding
import com.example.demo2.ui.theme.CallLogAdapter
import com.example.demo2.ui.theme.CallLogModel
import com.example.demo2.ui.theme.CallLogsService
import com.example.demo2.ui.theme.PaginationScrollListener
import com.example.demo2.ui.theme.RecordActivity
import com.example.demo2.ui.theme.RecordActivity.Companion.EXTRA_RECORDED_CALLS
import com.example.demo2.ui.theme.RecordInterface
import com.example.demo2.ui.theme.RecordedCall
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() , RecordInterface {
    private lateinit var binding: ActivityMainBinding
    private val callLogModel: ArrayList<CallLogModel> = ArrayList()
    private lateinit var callLogAdapter: CallLogAdapter
    private lateinit var rv_call_logs: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val recordedCallsList: MutableList<RecordedCall> = mutableListOf()
    private var currentRecordingStartTimeMillis: Long = 0
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private var pageNumber = 1 // Track current page number
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recordBtn.setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_RECORDED_CALLS, ArrayList(recordedCallsList))
            startActivity(intent)
        }

        initializeViews()
        if (checkPermission()) {
            startForegroundService(Intent(this, CallLogsService::class.java))
            loadCallLogs()
            registerPhoneStateListener()
            loadRecordedCalls()
        } else {
            requestPermissions()
        }
        if (checkPermission()) {
            startForegroundService(Intent(this, CallLogsService::class.java))
            loadCallLogs()
            registerPhoneStateListener()
            loadRecordedCalls()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO
            ),
            REQUEST_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CALL_LOGS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCallLogs()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }else if (requestCode == REQUEST_PERMISSIONS) {
            registerPhoneStateListener()
        }
    }



    private fun initializeViews() {
        rv_call_logs = binding.activityMainRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            callLogAdapter = CallLogAdapter(this@MainActivity, callLogModel)
            adapter = callLogAdapter
            addOnScrollListener(object : PaginationScrollListener(layoutManager as LinearLayoutManager) {
                override fun loadMoreItems() {
                    if (!isLoading && !isLastPage) {
                        pageNumber++
                        loadCallLogs()
                    }
                }
            })
        }

        swipeRefreshLayout = binding.activityMainSwipeRefreshLayout.apply {
            setOnRefreshListener {
                pageNumber = 1
                callLogModel.clear()
                loadCallLogs()
                isRefreshing = false
            }
        }
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
                PERMISSIONS_REQUEST_READ_CALL_LOGS
            )
            false
        } else {
            true
        }
    }

    private fun loadCallLogs() {
        isLoading = true
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val callLogs = ArrayList<CallLogModel>()
            moveToOffset(cursor)

            var count = 0
            while (cursor.moveToNext() && count < pageSize) {
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)

                val number = numberIndex.takeIf { it >= 0 }?.let { cursor.getString(it) }
                val name = nameIndex.takeIf { it >= 0 }?.let { cursor.getString(it) }
                val type = typeIndex.takeIf { it >= 0 }?.let { cursor.getInt(it) }
                val date = dateIndex.takeIf { it >= 0 }?.let { cursor.getLong(it) }
                val duration = durationIndex.takeIf { it >= 0 }?.let { cursor.getLong(it) }
                var dateValue: String
                var timeValue: String
                val typeValue: String = when (type) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    CallLog.Calls.VOICEMAIL_TYPE -> "Voicemail"
                    CallLog.Calls.REJECTED_TYPE -> "Rejected"
                    CallLog.Calls.BLOCKED_TYPE -> "Blocked"
                    CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> "Externally Answered"
                    else -> "NA"
                }
                val dateFormatter = SimpleDateFormat(
                    "dd MMM yyyy", Locale.US
                )
                val timeFormatter = SimpleDateFormat(
                    "HH:mm:ss", Locale.US
                )
                dateValue = date?.let { Date(it) }?.let { dateFormatter.format(it) }.toString()
                timeValue = date?.let { Date(it) }?.let { timeFormatter.format(it) }.toString()
                val callLog = CallLogModel(
                    number,
                    name,
                    typeValue,
                    dateValue,
                    timeValue,
                    duration.toString()
                )
                callLogs.add(callLog)
                count++
            }

            callLogModel.addAll(callLogs)
            callLogAdapter = CallLogAdapter(this@MainActivity, callLogModel)
            callLogAdapter.notifyDataSetChanged()
            isLoading = false
            isLastPage = !cursor.moveToNext()
        }
    }

    private fun moveToOffset(cursor: Cursor) {
        val offset = (pageNumber - 1) * pageSize
        if (offset > 0) {
            cursor.move(offset)
        }
    }

    private fun registerPhoneStateListener() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }
    private fun startRecording() {
        // Start recording audio
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        val audioFilePath = getAudioFilePath() // Get the file path for saving the audio file
        mediaRecorder.setOutputFile(audioFilePath)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            currentRecordingStartTimeMillis = System.currentTimeMillis()
            isRecording = true
        } catch (e: IOException) {
            Log.e(TAG, "startRecording: ", e)
        }
    }

    private fun getAudioFilePath(): String {
        // Define a directory where recorded audio files will be saved
        val directory = File(getExternalFilesDir(null), "recordings")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        // Generate a unique file name for the audio file
        val fileName = "recording_${System.currentTimeMillis()}.3gp"
        return File(directory, fileName).absolutePath
    }

    private fun loadRecordedCalls() {
        // Retrieve all recorded call audio files from the directory
        val directory = File(getExternalFilesDir(null), "recordings")
        val recordedCalls = mutableListOf<RecordedCall>()
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                val duration = getDurationFromFile(file)
                val timestamp = file.lastModified()
                recordedCalls.add(RecordedCall(file.absolutePath, duration, timestamp))
            }
        }
        recordedCallsList.addAll(recordedCalls)
    }

    private fun getDurationFromFile(file: File): String {
        // Calculate duration of the audio file (if needed)
        // You can use MediaMetadataRetriever or other methods to get duration from the file
        return "00:00" // Placeholder for duration
    }


    private fun stopRecording() {
        // Stop recording audio
        mediaRecorder.stop()
        mediaRecorder.release()
        isRecording = false

        // Calculate duration of the recorded call
        val durationMillis = System.currentTimeMillis() - currentRecordingStartTimeMillis
        val durationSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)
        val duration = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)

        // Create RecordedCall object and add it to the recordedCallsList
        val recordedCall = RecordedCall("file_path_here", duration, currentRecordingStartTimeMillis)
        recordedCallsList.add(recordedCall)
//        val listener = intent.getSerializableExtra(EXTRA_RECORDED_CALLS) as? RecordInterface
//        listener?.record(recordedCall)
        Log.d(TAG, "stopRecording: $recordedCallsList")
    }
    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    startRecording()
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    // Call answered or outgoing call
                    startRecording()
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    // Call ended
                    if (isRecording) {
                        stopRecording()
                    }
                }
            }
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_READ_CALL_LOGS = 100
        const val TAG = "MainActivity"
        const val REQUEST_PERMISSIONS = 123
    }

    override fun record(recordedCall: RecordedCall) {
        TODO("Not yet implemented")
    }
}



