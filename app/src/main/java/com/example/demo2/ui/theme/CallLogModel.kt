package com.example.demo2.ui.theme

class CallLogModel(
    var phNumber: String? = null,
    var contactName: String? = null,
    var callType: String? = null,
    @JvmField var callDate: String? = null,
    @JvmField var callTime: String? = null,
    var callDuration: String? = null
)