package com.example.lon

data class LoanSchedule(
    val month: Int,           // 상환 월 (몇 번째 달인지)
    val payment: Double,      // 월 납입금액 (원금 + 이자)
    val principal: Double,    // 원금 상환액
    val interest: Double,     // 이자 납입액
    val remainingBalance: Double  // 남은 대출 잔액
)