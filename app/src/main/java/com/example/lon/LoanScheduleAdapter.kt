package com.example.lon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class LoanScheduleAdapter : RecyclerView.Adapter<LoanScheduleAdapter.ViewHolder>() {

    private val decimalFormat = DecimalFormat("#,###")
    private var scheduleList: List<LoanSchedule> = listOf()

    fun submitList(newList: List<LoanSchedule>) {
        scheduleList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthText: TextView = view.findViewById(R.id.monthText)
        val paymentText: TextView = view.findViewById(R.id.paymentText)
        val principalText: TextView = view.findViewById(R.id.principalText)
        val interestText: TextView = view.findViewById(R.id.interestText)
        val balanceText: TextView = view.findViewById(R.id.balanceText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = scheduleList[position]
        holder.monthText.text = "${item.month}회차"
        holder.paymentText.text = "${decimalFormat.format(item.payment)}원"
        holder.principalText.text = "${decimalFormat.format(item.principal)}원"
        holder.interestText.text = "${decimalFormat.format(item.interest)}원"
        holder.balanceText.text = "${decimalFormat.format(item.remainingBalance)}원"
    }

    override fun getItemCount() = scheduleList.size
}