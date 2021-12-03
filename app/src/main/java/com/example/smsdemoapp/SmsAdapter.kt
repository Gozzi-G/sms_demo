package com.example.smsdemoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SmsAdapter: RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    var smsList = mutableListOf<SMS>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    class SmsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val smsDate: TextView = itemView.findViewById(R.id.vgMessageDate)
        val smsBody: TextView = itemView.findViewById(R.id.vgMessageBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sms, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val sms = smsList[position]

        holder.smsDate.text = sms.date
        holder.smsBody.text = sms.message
    }

    override fun getItemCount(): Int {
        return smsList.size
    }
}