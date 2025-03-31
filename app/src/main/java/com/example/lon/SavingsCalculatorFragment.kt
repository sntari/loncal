package com.example.lon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import kotlin.math.pow

class SavingsCalculatorFragment : Fragment() {

    private lateinit var monthlyDepositInput: EditText
    private lateinit var periodInput: EditText
    private lateinit var interestInput: EditText
    private lateinit var yearMonthToggle: Button
    private lateinit var interestTypeGroup: RadioGroup
    private lateinit var taxationGroup: RadioGroup
    private lateinit var preferredTaxRateInput: EditText
    private lateinit var calculateButton: Button
    private lateinit var resultText: TextView
    private lateinit var taxationLayout: LinearLayout

    private var isYearMode = true
    private var interestType = "simple"
    private var taxationType = "general"
    private var taxRate = 15.4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_savings_calculator, container, false)

        // Initialize Views
        monthlyDepositInput = binding.findViewById(R.id.monthlyDepositInput)
        periodInput = binding.findViewById(R.id.periodInput)
        interestInput = binding.findViewById(R.id.interestInput)
        yearMonthToggle = binding.findViewById(R.id.yearMonthToggle)
        interestTypeGroup = binding.findViewById(R.id.interestTypeGroup)
        taxationGroup = binding.findViewById(R.id.taxationGroup)
        preferredTaxRateInput = binding.findViewById(R.id.preferredTaxRateInput)
        calculateButton = binding.findViewById(R.id.calculateButton)
        resultText = binding.findViewById(R.id.resultText)
        taxationLayout = binding.findViewById(R.id.taxationLayout)

        // Year/Month Toggle Button
        yearMonthToggle.setOnClickListener {
            isYearMode = !isYearMode
            yearMonthToggle.text = if (isYearMode) "년" else "월"
        }

        // Interest Type (Simple/Compound Interest)
        interestTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            interestType = when (checkedId) {
                R.id.simpleInterest -> "simple"
                R.id.compoundInterest -> "compound"
                else -> "simple"
            }
        }

        // Taxation Type (General/Tax-Exempt/Tax-Preferred)
        taxationGroup.setOnCheckedChangeListener { _, checkedId ->
            taxationType = when (checkedId) {
                R.id.generalTax -> "general"
                R.id.taxExempt -> "exempt"
                R.id.taxPreferred -> "preferred"
                else -> "general"
            }

            // Show or hide the preferred tax rate input
            preferredTaxRateInput.visibility = if (taxationType == "preferred") View.VISIBLE else View.GONE
        }

        // Calculate Button
        calculateButton.setOnClickListener {
            val monthlyDeposit = monthlyDepositInput.text.toString().toDoubleOrNull()
            val period = periodInput.text.toString().toIntOrNull()
            val interestRate = interestInput.text.toString().toDoubleOrNull()

            if (monthlyDeposit != null && period != null && interestRate != null) {
                val result = calculateSavings(
                    monthlyDeposit,
                    period,
                    interestRate,
                    isYearMode,
                    interestType,
                    taxationType
                )
                displayResult(result)
            } else {
                resultText.text = "모든 필드를 올바르게 입력해주세요."
            }
        }

        return binding
    }

    private fun calculateSavings(
        monthlyDeposit: Double,
        period: Int,
        interestRate: Double,
        isYearMode: Boolean,
        interestType: String,
        taxationType: String
    ): SavingsResult {
        var effectivePeriod = period
        if (isYearMode) {
            effectivePeriod *= 12 // Convert years to months
        }

        val totalDeposit = monthlyDeposit * effectivePeriod
        var totalInterest = 0.0

        if (interestType == "simple") {
            for (month in 1..effectivePeriod) {
                totalInterest += monthlyDeposit * (interestRate / 100) * (effectivePeriod - month + 1) / 12
            }
        } else if (interestType == "compound") {
            val monthlyRate = (interestRate / 100) / 12
            var balance = 0.0
            for (month in 1..effectivePeriod) {
                balance = (balance + monthlyDeposit) * (1 + monthlyRate)
            }
            totalInterest = balance - totalDeposit
        }

        var taxAmount = 0.0
        when (taxationType) {
            "general" -> taxAmount = totalInterest * 0.154
            "preferred" -> {
                val preferredTaxRate = preferredTaxRateInput.text.toString().toDoubleOrNull() ?: 0.0
                taxAmount = totalInterest * (preferredTaxRate / 100)
            }
            "exempt" -> taxAmount = 0.0
        }

        val afterTaxInterest = totalInterest - taxAmount
        val afterTaxMaturityAmount = totalDeposit + afterTaxInterest

        return SavingsResult(
            totalDeposit, totalInterest, taxAmount, afterTaxMaturityAmount
        )
    }

    private fun displayResult(result: SavingsResult) {
        resultText.text = """
            원금 합계: ${String.format("%,.0f", result.totalDeposit)}원
            세전 이자: ${String.format("%,.0f", result.interest)}원
            이자 과세: ${String.format("%,.0f", result.taxAmount)}원
            세후 만기수령액: ${String.format("%,.0f", result.afterTaxMaturityAmount)}원
        """.trimIndent()
    }

    data class SavingsResult(
        val totalDeposit: Double,
        val interest: Double,
        val taxAmount: Double,
        val afterTaxMaturityAmount: Double
    )
}