package com.example.lon

data class LoanSchedule(
    val month: Int,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val remainingBalance: Double
)