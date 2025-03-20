package com.example.lon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

/**
 * 대출 일정 목록을 위한 RecyclerView 어댑터 클래스
 * 대출 상환 계획을 리스트 형태로 표시합니다.
 */
class LoanScheduleAdapter : RecyclerView.Adapter<LoanScheduleAdapter.ViewHolder>() {

    private val decimalFormat = DecimalFormat("#,###")  // 숫자 형식을 지정하는 포맷터 (천 단위 구분 기호 사용)
    private var scheduleList: List<LoanSchedule> = listOf()  // 대출 일정 데이터 목록

    /**
     * 새로운 대출 일정 목록을 설정하고 UI를 갱신합니다.
     */
    fun submitList(newList: List<LoanSchedule>) {
        scheduleList = newList
        notifyDataSetChanged()
    }

    /**
     * 각 항목의 뷰를 보유하는 ViewHolder 클래스
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthText: TextView = view.findViewById(R.id.monthText)      // 회차 표시 텍스트뷰
        val paymentText: TextView = view.findViewById(R.id.paymentText)  // 납입금액 표시 텍스트뷰
        val principalText: TextView = view.findViewById(R.id.principalText)  // 원금 표시 텍스트뷰
        val interestText: TextView = view.findViewById(R.id.interestText)    // 이자 표시 텍스트뷰
        val balanceText: TextView = view.findViewById(R.id.balanceText)      // 잔액 표시 텍스트뷰
    }

    /**
     * 새로운 ViewHolder를 생성합니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ViewHolder(view)
    }

    /**
     * 지정된 위치의 데이터를 ViewHolder에 바인딩합니다.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = scheduleList[position]
        holder.monthText.text = "${item.month}회차"
        holder.paymentText.text = "${decimalFormat.format(item.payment)}원"
        holder.principalText.text = "${decimalFormat.format(item.principal)}원"
        holder.interestText.text = "${decimalFormat.format(item.interest)}원"
        holder.balanceText.text = "${decimalFormat.format(item.remainingBalance)}원"
    }

    /**
     * 아이템 목록의 총 크기를 반환합니다.
     */
    override fun getItemCount() = scheduleList.size
}