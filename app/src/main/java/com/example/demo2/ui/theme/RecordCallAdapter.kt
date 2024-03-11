package com.example.demo2.ui.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demo2.R

class RecordCallAdapter(private val recordedCalls: MutableList<RecordedCall>) : RecyclerView.Adapter<RecordCallAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record_calls, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val call = recordedCalls[position]
        holder.bind(call)
    }

    override fun getItemCount(): Int {
        return recordedCalls.size
    }
    fun updateData(newData: MutableList<RecordedCall>) {
        recordedCalls.clear()
        recordedCalls.addAll(newData)
        notifyDataSetChanged()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(recordedCall: RecordedCall) {
            // Bind recorded call details to views in the item layout
            itemView.findViewById<TextView>(R.id.textViewFilePath).text = recordedCall.filePath
            itemView.findViewById<TextView>(R.id.textViewDuration).text = recordedCall.duration
            itemView.findViewById<TextView>(R.id.textViewTimestamp).text = recordedCall.timestamp.toString()
        }
    }
}
