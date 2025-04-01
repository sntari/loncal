package com.example.lon

import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var resetButton: Button
    private lateinit var resultText: TextView
    private lateinit var taxationLayout: LinearLayout

    // 새로 추가한 뷰 변수들
    private lateinit var koreanAmountText: TextView
    private lateinit var add10WanButton: Button
    private lateinit var add100WanButton: Button
    private lateinit var add1000WanButton: Button
    private lateinit var periodButtonsLayout: LinearLayout
    private lateinit var addPeriod1Button: Button
    private lateinit var addPeriod2Button: Button
    private lateinit var addPeriod3Button: Button

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
        resetButton = binding.findViewById(R.id.resetButton)
        resultText = binding.findViewById(R.id.resultText)
        taxationLayout = binding.findViewById(R.id.taxationLayout)

        // 새로 추가한 뷰 초기화
        koreanAmountText = binding.findViewById(R.id.koreanAmountText)
        add10WanButton = binding.findViewById(R.id.add10WanButton)
        add100WanButton = binding.findViewById(R.id.add100WanButton)
        add1000WanButton = binding.findViewById(R.id.add1000WanButton)
        periodButtonsLayout = binding.findViewById(R.id.periodButtonsLayout)
        addPeriod1Button = binding.findViewById(R.id.addPeriod1Button)
        addPeriod2Button = binding.findViewById(R.id.addPeriod2Button)
        addPeriod3Button = binding.findViewById(R.id.addPeriod3Button)

        // 월 납입액 버튼 리스너 설정
        setupDepositAmountButtons()

        // 적립 기간 버튼 리스너 설정
        setupPeriodButtons()

        // TextWatcher로 실시간으로 쉼표 추가 및 한글 금액 표시
        monthlyDepositInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false // 무한 루프 방지 플래그

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변경 전에는 아무 작업도 하지 않음
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, after: Int) {
                // 텍스트가 변경될 때마다 실행되며, 아무 작업도 하지 않음
            }

            override fun afterTextChanged(editable: Editable?) {
                if (isFormatting) return // 이미 텍스트를 수정 중이면 종료

                val inputText = editable.toString().replace(",", "") // 기존의 쉼표 제거

                if (inputText.isNotEmpty()) {
                    try {
                        isFormatting = true // 포맷팅 시작
                        // 숫자를 쉼표로 구분하여 포맷
                        val number = inputText.toLong() // Long 타입으로 변환
                        val formattedText = NumberFormat.getInstance().format(number)
                        monthlyDepositInput.setText(formattedText)
                        monthlyDepositInput.setSelection(formattedText.length) // 커서를 텍스트의 끝으로 설정

                        // 한글 금액 표시
                        koreanAmountText.text = convertToKoreanAmount(number)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace() // 숫자 포맷 에러 처리
                        koreanAmountText.text = ""
                    } finally {
                        isFormatting = false // 포맷팅 종료
                    }
                } else {
                    koreanAmountText.text = ""
                }
            }
        })

        // Year/Month Toggle Button
        yearMonthToggle.setOnClickListener {
            isYearMode = !isYearMode
            yearMonthToggle.text = if (isYearMode) "년" else "월"
            // 토글 변경 시 기간 버튼 텍스트 업데이트
            updatePeriodButtonsText()
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
            val depositText = monthlyDepositInput.text.toString().replace(",", "")
            val monthlyDeposit = depositText.toDoubleOrNull()
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

        // Reset Button (초기화 버튼)
        resetButton.setOnClickListener {
            resetAllFields()
        }

        // 초기 기간 버튼 텍스트 설정
        updatePeriodButtonsText()

        return binding
    }

    private fun resetAllFields() {
        // 텍스트 입력 필드 초기화
        monthlyDepositInput.setText("")
        periodInput.setText("")
        interestInput.setText("")
        preferredTaxRateInput.setText("")

        // 한글 금액 표시 초기화
        koreanAmountText.text = ""

        // 라디오 그룹 초기화 (첫 번째 항목 선택)
        interestTypeGroup.check(R.id.simpleInterest)
        taxationGroup.check(R.id.generalTax)

        // 년/월 모드 초기화 (년 모드로)
        isYearMode = true
        yearMonthToggle.text = "년"
        updatePeriodButtonsText()

        // 세금우대 입력 필드 숨기기
        preferredTaxRateInput.visibility = View.GONE

        // 결과 텍스트 초기화
        resultText.text = "결과가 여기에 표시됩니다."

        // 변수 초기화
        interestType = "simple"
        taxationType = "general"
    }

    private fun setupDepositAmountButtons() {
        // +10만원 버튼
        add10WanButton.setOnClickListener {
            addAmountToDeposit(100000)
        }

        // +100만원 버튼
        add100WanButton.setOnClickListener {
            addAmountToDeposit(1000000)
        }

        // +1000만원 버튼
        add1000WanButton.setOnClickListener {
            addAmountToDeposit(10000000)
        }
    }

    private fun addAmountToDeposit(amountToAdd: Long) {
        val currentText = monthlyDepositInput.text.toString().replace(",", "")
        val currentAmount = if (currentText.isEmpty()) 0L else currentText.toLong()
        val newAmount = currentAmount + amountToAdd

        // 쉼표 포맷 없이 입력하고 TextWatcher에서 처리되도록 함
        monthlyDepositInput.setText(newAmount.toString())
    }

    private fun setupPeriodButtons() {
        // 첫 번째 기간 버튼 (+1년 또는 +6개월)
        addPeriod1Button.setOnClickListener {
            addToPeriod(if (isYearMode) 1 else 6)
        }

        // 두 번째 기간 버튼 (+5년 또는 +12개월)
        addPeriod2Button.setOnClickListener {
            addToPeriod(if (isYearMode) 5 else 12)
        }

        // 세 번째 기간 버튼 (+10년 또는 +24개월)
        addPeriod3Button.setOnClickListener {
            addToPeriod(if (isYearMode) 10 else 24)
        }
    }

    private fun addToPeriod(valueToAdd: Int) {
        val currentText = periodInput.text.toString()
        val currentValue = if (currentText.isEmpty()) 0 else currentText.toInt()
        val newValue = currentValue + valueToAdd
        periodInput.setText(newValue.toString())
    }

    private fun updatePeriodButtonsText() {
        if (isYearMode) {
            addPeriod1Button.text = "+1년"
            addPeriod2Button.text = "+5년"
            addPeriod3Button.text = "+10년"
        } else {
            addPeriod1Button.text = "+6개월"
            addPeriod2Button.text = "+12개월"
            addPeriod3Button.text = "+24개월"
        }
    }

    private fun convertToKoreanAmount(amount: Long): String {
        if (amount == 0L) return ""

        val units = arrayOf("", "만", "억", "조")
        val unitValues = arrayOf(1L, 10000L, 100000000L, 1000000000000L)

        var result = ""
        var remainingAmount = amount

        // 가장 큰 단위부터 처리
        for (i in units.size - 1 downTo 0) {
            val unitValue = unitValues[i]
            if (remainingAmount >= unitValue) {
                val quotient = remainingAmount / unitValue
                remainingAmount %= unitValue

                // 만, 억, 조 단위일 때는 단위도 표시
                if (i > 0) {
                    // 1만은 "1만"으로 표시하고 1만 5천은 "1만 5천"으로 표시
                    if (quotient > 0) {
                        result += "${quotient}${units[i]} "
                    }
                } else {
                    // 일 단위(1천, 1백, ...)
                    if (quotient > 0) {
                        result += "${quotient}"
                    }
                }
            }
        }

        return result.trim() + "원"
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