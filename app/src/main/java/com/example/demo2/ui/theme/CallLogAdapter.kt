package com.example.demo2.ui.theme

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.demo2.R

class CallLogAdapter(private var context: Context, private var callLogModelArrayList: ArrayList<CallLogModel>) :
    RecyclerView.Adapter<CallLogAdapter.MyViewHolder>() {
    private var isLoading = false




    private var px = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val r = parent.resources
        px = Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, r.displayMetrics
            )
        )
        val v = LayoutInflater.from(context).inflate(R.layout.layout_call_log, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position == 0) {
            val layoutParams = holder.cardView.layoutParams as MarginLayoutParams
            layoutParams.topMargin = px
            holder.cardView.requestLayout()
        }
        val currentLog = callLogModelArrayList[position]

        Log.d("onBindViewHolder", "onBindViewHolder:${currentLog.contactName} ")
        holder.tv_ph_num.text = currentLog.phNumber
        holder.tv_contact_name.text = currentLog.contactName
        holder.tv_call_type.text = currentLog.callType
        holder.tv_call_date.text = currentLog.callDate
        holder.tv_call_time.text = currentLog.callTime
        holder.tv_call_duration.text = currentLog.callDuration
    }

    override fun getItemCount(): Int {
        return callLogModelArrayList.size
    }

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        var cardView: CardView
        var tv_ph_num: TextView
        var tv_contact_name: TextView
        var tv_call_type: TextView
        var tv_call_date: TextView
        var tv_call_time: TextView
        var tv_call_duration: TextView

        init {
            tv_ph_num = itemView.findViewById(R.id.layout_call_log_ph_no)
            tv_contact_name = itemView.findViewById(R.id.layout_call_log_contact_name)
            tv_call_type = itemView.findViewById(R.id.layout_call_log_type)
            tv_call_date = itemView.findViewById(R.id.layout_call_log_date)
            tv_call_time = itemView.findViewById(R.id.layout_call_log_time)
            tv_call_duration = itemView.findViewById(R.id.layout_call_log_duration)
            cardView = itemView.findViewById(R.id.layout_call_log_cardview)
        }
    }
}