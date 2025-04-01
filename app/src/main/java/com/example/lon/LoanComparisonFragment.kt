package com.example.lon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

class LoanComparisonFragment : Fragment() {
    // 대출 A 입력 필드
    private lateinit var loanAAmountEditText: EditText
    private lateinit var loanATermEditText: EditText
    private lateinit var loanAInterestRateEditText: EditText
    private lateinit var loanARepaymentTypeSpinner: Spinner

    // 대출 B 입력 필드
    private lateinit var loanBAmountEditText: EditText
    private lateinit var loanBTermEditText: EditText
    private lateinit var loanBInterestRateEditText: EditText
    private lateinit var loanBRepaymentTypeSpinner: Spinner

    // 결과 표시 필드
    private lateinit var loanARepaymentTypeTextView: TextView
    private lateinit var loanBRepaymentTypeTextView: TextView
    private lateinit var loanAAmountTextView: TextView
    private lateinit var loanBAmountTextView: TextView
    private lateinit var loanAInterestTermTextView: TextView
    private lateinit var loanBInterestTermTextView: TextView
    private lateinit var loanAMonthlyPaymentTextView: TextView
    private lateinit var loanBMonthlyPaymentTextView: TextView
    private lateinit var loanATotalInterestTextView: TextView
    private lateinit var loanBTotalInterestTextView: TextView
    private lateinit var loanATotalPaymentTextView: TextView
    private lateinit var loanBTotalPaymentTextView: TextView
    private lateinit var betterLoanTextView: TextView
    private lateinit var resetImageView: ImageView

    // 버튼
    private lateinit var compareButton: Button

    // 상환 방식 타입
    private val repaymentTypes = arrayOf("원리금균등상환", "원금균등상환", "만기일시상환")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loan_comparison, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        initializeViews(view)

        // 스피너 어댑터 설정
        setupSpinners()

        // 입력값 변경 리스너 설정
        setupTextChangeListeners()

        // 버튼 클릭 리스너 설정
        setupButtonListeners()

        resetImageView.setOnClickListener {
            resetEditTextFields()
        }
    }

    private fun initializeViews(view: View) {
        // 대출 A 입력 필드
        loanAAmountEditText = view.findViewById(R.id.et_loan_a_amount)
        loanATermEditText = view.findViewById(R.id.et_loan_a_term)
        loanAInterestRateEditText = view.findViewById(R.id.et_loan_a_interest_rate)
        loanARepaymentTypeSpinner = view.findViewById(R.id.spinner_loan_a_repayment_type)

        // 대출 B 입력 필드
        loanBAmountEditText = view.findViewById(R.id.et_loan_b_amount)
        loanBTermEditText = view.findViewById(R.id.et_loan_b_term)
        loanBInterestRateEditText = view.findViewById(R.id.et_loan_b_interest_rate)
        loanBRepaymentTypeSpinner = view.findViewById(R.id.spinner_loan_b_repayment_type)

        // 결과 표시 필드
        loanARepaymentTypeTextView = view.findViewById(R.id.tv_loan_a_repayment_type)
        loanBRepaymentTypeTextView = view.findViewById(R.id.tv_loan_b_repayment_type)
        loanAAmountTextView = view.findViewById(R.id.tv_loan_a_amount)
        loanBAmountTextView = view.findViewById(R.id.tv_loan_b_amount)
        loanAInterestTermTextView = view.findViewById(R.id.tv_loan_a_interest_term)
        loanBInterestTermTextView = view.findViewById(R.id.tv_loan_b_interest_term)
        loanAMonthlyPaymentTextView = view.findViewById(R.id.tv_loan_a_monthly_payment)
        loanBMonthlyPaymentTextView = view.findViewById(R.id.tv_loan_b_monthly_payment)
        loanATotalInterestTextView = view.findViewById(R.id.tv_loan_a_total_interest)
        loanBTotalInterestTextView = view.findViewById(R.id.tv_loan_b_total_interest)
        loanATotalPaymentTextView = view.findViewById(R.id.tv_loan_a_total_payment)
        loanBTotalPaymentTextView = view.findViewById(R.id.tv_loan_b_total_payment)
        betterLoanTextView = view.findViewById(R.id.tv_better_loan)

        // 버튼
        compareButton = view.findViewById(R.id.btn_compare)
        resetImageView = view.findViewById(R.id.resetIcon)
    }

    private fun resetEditTextFields() {
        // EditText 초기화
        loanAAmountEditText.text.clear()
        loanATermEditText.text.clear()
        loanAInterestRateEditText.text.clear()

        loanBAmountEditText.text.clear()
        loanBTermEditText.text.clear()
        loanBInterestRateEditText.text.clear()

        // 스피너 초기화 (처음 항목 선택)
        loanARepaymentTypeSpinner.setSelection(0)
        loanBRepaymentTypeSpinner.setSelection(0)

        // 결과 TextView 초기화
        loanARepaymentTypeTextView.text = "-"
        loanBRepaymentTypeTextView.text = "-"
        loanAAmountTextView.text = "0원"
        loanBAmountTextView.text = "0원"
        loanAInterestTermTextView.text = "-"
        loanBInterestTermTextView.text = "-"
        loanAMonthlyPaymentTextView.text = "0원"
        loanBMonthlyPaymentTextView.text = "0원"
        loanATotalInterestTextView.text = "0원"
        loanBTotalInterestTextView.text = "0원"
        loanATotalPaymentTextView.text = "0원"
        loanBTotalPaymentTextView.text = "0원"
        betterLoanTextView.text = ""
        resetColor()
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            repaymentTypes
        )

        loanARepaymentTypeSpinner.adapter = adapter
        loanBRepaymentTypeSpinner.adapter = adapter
    }

    private fun setupTextChangeListeners() {
        val textWatcher = { /* 입력값 변경 시 자동 계산은 사용하지 않음 */ }

        loanAAmountEditText.doAfterTextChanged { textWatcher() }
        loanATermEditText.doAfterTextChanged { textWatcher() }
        loanAInterestRateEditText.doAfterTextChanged { textWatcher() }

        loanBAmountEditText.doAfterTextChanged { textWatcher() }
        loanBTermEditText.doAfterTextChanged { textWatcher() }
        loanBInterestRateEditText.doAfterTextChanged { textWatcher() }
    }

    private fun setupButtonListeners() {
        compareButton.setOnClickListener {
            updateCalculations()
        }
    }

    private fun updateCalculations() {
        try {
            // 대출 A 정보 파싱
            val loanAAmount = loanAAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val loanATerm = loanATermEditText.text.toString().toIntOrNull() ?: 0
            val loanAInterestRate = loanAInterestRateEditText.text.toString().toDoubleOrNull() ?: 0.0
            val loanARepaymentType = loanARepaymentTypeSpinner.selectedItemPosition
            val loanARepaymentTypeName = repaymentTypes[loanARepaymentType]

            // 대출 B 정보 파싱
            val loanBAmount = loanBAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val loanBTerm = loanBTermEditText.text.toString().toIntOrNull() ?: 0
            val loanBInterestRate = loanBInterestRateEditText.text.toString().toDoubleOrNull() ?: 0.0
            val loanBRepaymentType = loanBRepaymentTypeSpinner.selectedItemPosition
            val loanBRepaymentTypeName = repaymentTypes[loanBRepaymentType]

            // 대출 A 계산
            val (loanAMonthlyPayment, loanATotalInterest, _) =
                calculateLoan(loanAAmount, loanATerm, loanAInterestRate, loanARepaymentType)

            // 대출 B 계산
            val (loanBMonthlyPayment, loanBTotalInterest, _) =
                calculateLoan(loanBAmount, loanBTerm, loanBInterestRate, loanBRepaymentType)

            // 결과 표시
            displayResults(
                loanAAmount, loanBAmount,
                loanAMonthlyPayment, loanBMonthlyPayment,
                loanATotalInterest, loanBTotalInterest,
                loanATerm, loanBTerm,
                loanAInterestRate, loanBInterestRate,
                loanARepaymentTypeName, loanBRepaymentTypeName
            )

        } catch (e: Exception) {
            // 에러 처리
            e.printStackTrace()
        }
    }

    private fun calculateLoan(
        principal: Double,
        termInMonths: Int,
        annualInterestRate: Double,
        repaymentType: Int
    ): Triple<Double, Double, List<Double>> {
        val monthlyInterestRate = annualInterestRate / 100 / 12
        var monthlyPayment = 0.0
        var totalInterest = 0.0
        val principalBalance = mutableListOf<Double>()

        when (repaymentType) {
            0 -> { // 원리금균등상환
                // 매월 상환금액이 동일
                if (monthlyInterestRate > 0 && termInMonths > 0) {
                    monthlyPayment = principal * monthlyInterestRate * (1 + monthlyInterestRate).pow(termInMonths) /
                            ((1 + monthlyInterestRate).pow(termInMonths) - 1)

                    var remainingPrincipal = principal
                    for (month in 1..termInMonths) {
                        val interestForMonth = remainingPrincipal * monthlyInterestRate
                        val principalForMonth = monthlyPayment - interestForMonth
                        remainingPrincipal -= principalForMonth
                        principalBalance.add(remainingPrincipal)
                        totalInterest += interestForMonth
                    }
                }
            }
            1 -> { // 원금균등상환
                // 매월 원금상환액이 동일
                if (termInMonths > 0) {
                    val monthlyPrincipal = principal / termInMonths
                    var remainingPrincipal = principal

                    for (month in 1..termInMonths) {
                        val interestForMonth = remainingPrincipal * monthlyInterestRate
                        val paymentForMonth = monthlyPrincipal + interestForMonth

                        if (month == 1) {
                            monthlyPayment = paymentForMonth
                        }

                        remainingPrincipal -= monthlyPrincipal
                        principalBalance.add(remainingPrincipal)
                        totalInterest += interestForMonth
                    }
                }
            }
            2 -> { // 만기일시상환
                // 매월 이자만 납부하고 만기에 원금 상환
                monthlyPayment = principal * monthlyInterestRate
                totalInterest = monthlyPayment * termInMonths

                for (month in 1 until termInMonths) {
                    principalBalance.add(principal)
                }
                principalBalance.add(0.0)
            }
        }

        return Triple(monthlyPayment, totalInterest, principalBalance)
    }


    private fun displayResults(
        loanAAmount: Double,
        loanBAmount: Double,
        loanAMonthlyPayment: Double,
        loanBMonthlyPayment: Double,
        loanATotalInterest: Double,
        loanBTotalInterest: Double,
        loanATerm: Int,
        loanBTerm: Int,
        loanAInterestRate: Double,
        loanBInterestRate: Double,
        loanARepaymentTypeName: String,
        loanBRepaymentTypeName: String
    ) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

        // 모든 TextView를 먼저 기본 색상으로 리셋
        resetTextColors()

        // 상환 방식
        loanARepaymentTypeTextView.text = loanARepaymentTypeName
        loanBRepaymentTypeTextView.text = loanBRepaymentTypeName

        // 대출 금액
        loanAAmountTextView.text = numberFormat.format(loanAAmount) + "원"
        loanBAmountTextView.text = numberFormat.format(loanBAmount) + "원"

        // 금리/기간
        loanAInterestTermTextView.text = "${loanAInterestRate}% / ${loanATerm}개월"
        loanBInterestTermTextView.text = "${loanBInterestRate}% / ${loanBTerm}개월"

        // 월 납입금
        loanAMonthlyPaymentTextView.text = numberFormat.format(loanAMonthlyPayment) + "원"
        loanBMonthlyPaymentTextView.text = numberFormat.format(loanBMonthlyPayment) + "원"

        // 총 이자금액
        loanATotalInterestTextView.text = numberFormat.format(loanATotalInterest) + "원"
        loanBTotalInterestTextView.text = numberFormat.format(loanBTotalInterest) + "원"

        // 총 상환금액
        val loanATotalPayment = loanAAmount + loanATotalInterest
        val loanBTotalPayment = loanBAmount + loanBTotalInterest
        loanATotalPaymentTextView.text = numberFormat.format(loanATotalPayment) + "원"
        loanBTotalPaymentTextView.text = numberFormat.format(loanBTotalPayment) + "원"

        // 절약 금액 및 더 좋은 대출 표시
        val savingsAmount = Math.abs(loanATotalInterest - loanBTotalInterest)

        val greenColor = resources.getColor(android.R.color.holo_green_dark)
        val whiteColor = resources.getColor(android.R.color.white)

        if (loanATotalInterest < loanBTotalInterest) {
            // 대출 A가 더 유리한 경우
            betterLoanTextView.text = "대출 A의 이자가 ${numberFormat.format(savingsAmount)}원 더 적습니다."
            betterLoanTextView.setTextColor(whiteColor)

            // 대출 A 관련 모든 텍스트 색상을 녹색으로 변경
            setLoanATextColors(greenColor)

        } else if (loanBTotalInterest < loanATotalInterest) {
            // 대출 B가 더 유리한 경우
            betterLoanTextView.text = "대출 B의 이자가 ${numberFormat.format(savingsAmount)}원 더 적습니다."
            betterLoanTextView.setTextColor(whiteColor)

            // 대출 B 관련 모든 텍스트 색상을 녹색으로 변경
            setLoanBTextColors(greenColor)

        } else {
            // 두 대출이 동일한 경우
            betterLoanTextView.text = "두 대출의 이자가 동일합니다."
            betterLoanTextView.setTextColor(resources.getColor(android.R.color.darker_gray))
        }
    }

    private fun resetTextColors() {
        val defaultColor = resources.getColor(android.R.color.black)

        // 대출 A 관련 텍스트뷰
        loanARepaymentTypeTextView.setTextColor(defaultColor)
        loanAAmountTextView.setTextColor(defaultColor)
        loanAInterestTermTextView.setTextColor(defaultColor)
        loanAMonthlyPaymentTextView.setTextColor(defaultColor)
        loanATotalInterestTextView.setTextColor(defaultColor)
        loanATotalPaymentTextView.setTextColor(defaultColor)

        // 대출 B 관련 텍스트뷰
        loanBRepaymentTypeTextView.setTextColor(defaultColor)
        loanBAmountTextView.setTextColor(defaultColor)
        loanBInterestTermTextView.setTextColor(defaultColor)
        loanBMonthlyPaymentTextView.setTextColor(defaultColor)
        loanBTotalInterestTextView.setTextColor(defaultColor)
        loanBTotalPaymentTextView.setTextColor(defaultColor)
    }

    private fun resetColor(){
        val defaultColor = resources.getColor(android.R.color.black)
        val blueColor = resources.getColor(android.R.color.holo_blue_dark)
        val redColor = resources.getColor(android.R.color.holo_red_dark)

        // 대출 A 관련 텍스트뷰
        loanARepaymentTypeTextView.setTextColor(blueColor)
        loanAAmountTextView.setTextColor(blueColor)
        loanAInterestTermTextView.setTextColor(blueColor)
        loanAMonthlyPaymentTextView.setTextColor(blueColor)
        loanATotalInterestTextView.setTextColor(defaultColor)
        loanATotalPaymentTextView.setTextColor(defaultColor)

        // 대출 B 관련 텍스트뷰
        loanBRepaymentTypeTextView.setTextColor(redColor)
        loanBAmountTextView.setTextColor(redColor)
        loanBInterestTermTextView.setTextColor(redColor)
        loanBMonthlyPaymentTextView.setTextColor(redColor)
        loanBTotalInterestTextView.setTextColor(defaultColor)
        loanBTotalPaymentTextView.setTextColor(defaultColor)
    }

    private fun setLoanATextColors(color: Int) {
        loanARepaymentTypeTextView.setTextColor(color)
        loanAAmountTextView.setTextColor(color)
        loanAInterestTermTextView.setTextColor(color)
        loanAMonthlyPaymentTextView.setTextColor(color)
        loanATotalInterestTextView.setTextColor(color)
        loanATotalPaymentTextView.setTextColor(color)
    }

    private fun setLoanBTextColors(color: Int) {
        loanBRepaymentTypeTextView.setTextColor(color)
        loanBAmountTextView.setTextColor(color)
        loanBInterestTermTextView.setTextColor(color)
        loanBMonthlyPaymentTextView.setTextColor(color)
        loanBTotalInterestTextView.setTextColor(color)
        loanBTotalPaymentTextView.setTextColor(color)
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoanComparisonFragment()
    }
}