package com.example.demo2

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.demo2.databinding.ActivityMainBinding
import com.example.demo2.ui.theme.CallLogAdapter
import com.example.demo2.ui.theme.CallLogModel
import com.example.demo2.ui.theme.PaginationScrollListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val callLogModel: ArrayList<CallLogModel> = ArrayList()
    private lateinit var callLogAdapter: CallLogAdapter
    private lateinit var rv_call_logs: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var pageNumber = 1 // Track current page number
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        if (checkPermission()) {
            loadCallLogs()
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
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                val name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
                val date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
                val duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                var typeValue: String = ""
                var dateValue: String = ""
                var timeValue: String = ""
                when (type) {
                    CallLog.Calls.INCOMING_TYPE -> typeValue = "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> typeValue = "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> typeValue = "Missed"
                    CallLog.Calls.VOICEMAIL_TYPE -> typeValue = "Voicemail"
                    CallLog.Calls.REJECTED_TYPE -> typeValue = "Rejected"
                    CallLog.Calls.BLOCKED_TYPE -> typeValue = "Blocked"
                    CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> typeValue = "Externally Answered"
                    else -> typeValue = "NA"
                }
                val dateFormatter = SimpleDateFormat(
                    "dd MMM yyyy", Locale.US
                )
                val timeFormatter = SimpleDateFormat(
                    "HH:mm:ss", Locale.US
                )
                dateValue = dateFormatter.format(Date(date))
                timeValue = timeFormatter.format(Date(date))
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
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_READ_CALL_LOGS = 100
    }
}



