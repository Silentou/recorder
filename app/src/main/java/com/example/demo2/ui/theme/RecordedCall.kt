package com.example.demo2.ui.theme

import android.os.Parcel
import android.os.Parcelable

data class RecordedCall(
    val filePath: String,
    val duration: String,
    val timestamp: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(filePath)
        parcel.writeString(duration)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecordedCall> {
        override fun createFromParcel(parcel: Parcel): RecordedCall {
            return RecordedCall(parcel)
        }

        override fun newArray(size: Int): Array<RecordedCall?> {
            return arrayOfNulls(size)
        }
    }
}

