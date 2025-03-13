package com.example.lon

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.util.*

class LoanCalculatorActivity : AppCompatActivity() {

    private lateinit var principalInput: EditText
    private lateinit var interestInput: EditText
    private lateinit var paymentInput: EditText
    private lateinit var calculateButton: Button
    private lateinit var resultText: TextView
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var scheduleAdapter: LoanScheduleAdapter

    private val decimalFormat = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_calculator)

        principalInput = findViewById(R.id.principalInput)
        interestInput = findViewById(R.id.interestInput)
        paymentInput = findViewById(R.id.paymentInput)
        calculateButton = findViewById(R.id.calculateButton)
        resultText = findViewById(R.id.resultText)
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView)

        scheduleAdapter = LoanScheduleAdapter()
        scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        scheduleRecyclerView.adapter = scheduleAdapter

        calculateButton.setOnClickListener {
            calculateLoan()
        }
    }

    private fun calculateLoan() {
        val principal = principalInput.text.toString().replace(",", "").toDoubleOrNull() ?: return
        val annualInterestRate = interestInput.text.toString().toDoubleOrNull() ?: return
        var monthlyPayment = paymentInput.text.toString().replace(",", "").toDoubleOrNull() ?: return

        val monthlyInterestRate = annualInterestRate / 100 / 12
        var remainingBalance = principal
        var month = 0
        var totalPayment = 0.0
        val scheduleList = mutableListOf<LoanSchedule>()

        while (remainingBalance > 0) {
            month++
            val interestPayment = remainingBalance * monthlyInterestRate
            val principalPayment = monthlyPayment - interestPayment

            if (remainingBalance < monthlyPayment) {
                monthlyPayment = remainingBalance + interestPayment
            }

            remainingBalance -= principalPayment
            totalPayment += monthlyPayment

            scheduleList.add(LoanSchedule(month, monthlyPayment, principalPayment, interestPayment, remainingBalance))

            if (remainingBalance < 0.01) break
        }

        val totalInterest = totalPayment - principal
        resultText.text = "총 상환 기간: ${month}개월\n총 이자액: ${decimalFormat.format(totalInterest)}원"

        scheduleAdapter.submitList(scheduleList)
    }
}