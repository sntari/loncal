package com.example.lon

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import kotlin.math.pow

class LoanCalculatorFragment : Fragment() {

    private lateinit var principalInput: EditText      // 대출 원금 입력
    private lateinit var principalKorean: TextView     // 대출 원금 한글 표시
    private lateinit var interestInput: EditText       // 이자율 입력 필드
    private lateinit var gracePeriodInput: EditText    // 거치기간
    private lateinit var periodInput: EditText         // 대출기간
    private lateinit var calculateButton: Button       // 계산 버튼
    private lateinit var resetButton: Button           // 초기화 버튼
    private lateinit var resultText: TextView          // 결과 표시 텍스트뷰
    private lateinit var scheduleRecyclerView: RecyclerView  // 상환 일정을 표시할 리사이클러뷰
    private lateinit var scheduleAdapter: LoanScheduleAdapter  // 상환 일정 어댑터
    private lateinit var paymentMethodSpinner: Spinner // 상환방법 스피너
    private lateinit var infoIcon: ImageView           // 정보 아이콘

    // 금액 빠른 입력 버튼
    private lateinit var addMillion: Button            // +100만 버튼
    private lateinit var addTenMillion: Button         // +1000만 버튼
    private lateinit var addHundredMillion: Button     // +1억 버튼

    // 기간 빠른 입력 버튼
    private lateinit var period12: Button              // 12개월 버튼
    private lateinit var period24: Button              // 24개월 버튼
    private lateinit var period36: Button              // 36개월 버튼
    private lateinit var grace0: Button                // 0개월 버튼
    private lateinit var grace6: Button                // 6개월 버튼
    private lateinit var grace12: Button               // 12개월 버튼

    private val decimalFormat = DecimalFormat("#,###")  // 숫자 포맷을 위한 DecimalFormat 객체

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Fragment의 레이아웃 설정
        val rootView = inflater.inflate(R.layout.fragment_loan_calculator, container, false)

        // XML에서 UI 요소를 찾아 변수에 할당
        principalInput = rootView.findViewById(R.id.principalInput)
        principalKorean = rootView.findViewById(R.id.principalKorean)
        interestInput = rootView.findViewById(R.id.interestInput)
        periodInput = rootView.findViewById(R.id.periodInput)
        gracePeriodInput = rootView.findViewById(R.id.gracePeriodInput)
        calculateButton = rootView.findViewById(R.id.calculateButton)
        resetButton = rootView.findViewById(R.id.resetButton)
        resultText = rootView.findViewById(R.id.resultText)
        scheduleRecyclerView = rootView.findViewById(R.id.scheduleRecyclerView)
        paymentMethodSpinner = rootView.findViewById(R.id.paymentMethodSpinner)
        infoIcon = rootView.findViewById(R.id.infoIcon)

        // 금액 빠른 입력 버튼
        addMillion = rootView.findViewById(R.id.addMillion)
        addTenMillion = rootView.findViewById(R.id.addTenMillion)
        addHundredMillion = rootView.findViewById(R.id.addHundredMillion)

        // 기간 빠른 입력 버튼
        period12 = rootView.findViewById(R.id.period12)
        period24 = rootView.findViewById(R.id.period24)
        period36 = rootView.findViewById(R.id.period36)
        grace0 = rootView.findViewById(R.id.grace0)
        grace6 = rootView.findViewById(R.id.grace6)
        grace12 = rootView.findViewById(R.id.grace12)

        // 리사이클러뷰에 어댑터와 레이아웃 매니저 설정
        scheduleAdapter = LoanScheduleAdapter()
        scheduleRecyclerView.layoutManager = LinearLayoutManager(context)
        scheduleRecyclerView.adapter = scheduleAdapter

        // 상환 방법 Spinner 설정
        val paymentMethods = arrayOf("원금 균등 상환", "원리금 균등 상환", "만기 일시 상환")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paymentMethods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentMethodSpinner.adapter = adapter

        // 대출 원금 입력 시 자동 포맷팅 및 한글 변환
        setupPrincipalInputFormatting()

        // 계산 버튼 클릭 이벤트 리스너 설정
        calculateButton.setOnClickListener {
            calculateLoan()  // 대출 계산 메서드 호출
        }

        // 초기화 버튼 클릭 이벤트 리스너 설정
        resetButton.setOnClickListener {
            resetInputs()
        }

        // 금액 빠른 입력 버튼 이벤트 설정
        setupQuickAmountButtons()

        // 기간 빠른 입력 버튼 이벤트 설정
        setupQuickPeriodButtons()

        // 설명 데이터
        val paymentDescriptions = mapOf(
            "원금 균등 상환" to "✅ 매월 일정한 원금을 갚음\n✅ 이자는 남은 대출 잔액에 따라 감소\n" +
                    "✅ 장점 : 총 이자 원리금>원금\n✅ 단점 : 초기 부담 ↑\n",
            "원리금 균등 상환" to "✅ 매월 원금과 이자의 합이 일정\n✅ 초반에는 이자 비중이 큼\n" +
                    "✅ 장점 : 안정적인 상환 계획\n✅ 단점 : 총 이자 원리금>원금\n",
            "만기 일시 상환" to "✅ 대출 기간 동안 이자만 납부\n✅ 만기에 원금을 한 번에 상환\n" +
                    "✅ 장점 : 월 상환 부담이 가장 적다.\n✅ 단점 : 총 이자 부담은 가장 크다\n"
        )

        // ? 아이콘 클릭 시 설명 표시
        infoIcon.setOnClickListener {
            val selectedMethod = paymentMethodSpinner.selectedItem.toString()
            val message = paymentDescriptions[selectedMethod] ?: "설명이 없습니다."

            AlertDialog.Builder(requireContext())
                .setTitle(selectedMethod)
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show()
        }

        return rootView
    }

    // 대출 원금 입력 시 자동 포맷팅 및 한글 변환 설정
    private fun setupPrincipalInputFormatting() {
        principalInput.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    principalInput.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                    val parsed = cleanString.toLongOrNull() ?: 0L

                    val formatted = if (parsed == 0L) "" else decimalFormat.format(parsed)
                    current = formatted
                    principalInput.setText(formatted)
                    principalInput.setSelection(formatted.length)

                    // 한글 금액 표시
                    principalKorean.text = if (parsed == 0L) "" else convertToKorean(parsed)

                    principalInput.addTextChangedListener(this)
                }
            }
        })
    }

    // 숫자를 한글 금액으로 변환
    private fun convertToKorean(number: Long): String {
        val units = arrayOf("", "만", "억", "조")
        val result = StringBuilder()
        var num = number
        var unitIndex = 0

        while (num > 0) {
            val part = (num % 10000).toInt()
            if (part > 0) {
                result.insert(0, "$part${units[unitIndex]} ")
            }
            num /= 10000
            unitIndex++
        }

        return result.toString().trim() + "원"
    }

    // 금액 빠른 입력 버튼 설정
    private fun setupQuickAmountButtons() {
        addMillion.setOnClickListener {
            addAmount(1_000_000)
        }

        addTenMillion.setOnClickListener {
            addAmount(10_000_000)
        }

        addHundredMillion.setOnClickListener {
            addAmount(100_000_000)
        }
    }

    // 기존 금액에 추가 금액 더하기
    private fun addAmount(amountToAdd: Long) {
        val currentText = principalInput.text.toString().replace(",", "")
        val currentAmount = currentText.toLongOrNull() ?: 0L
        val newAmount = currentAmount + amountToAdd

        val formatted = decimalFormat.format(newAmount)
        principalInput.setText(formatted)
        principalInput.setSelection(formatted.length)

        // 한글 금액 업데이트
        principalKorean.text = convertToKorean(newAmount)
    }

    // 기간 빠른 입력 버튼 설정
    private fun setupQuickPeriodButtons() {
        period12.setOnClickListener {
            periodInput.setText("12")
        }

        period24.setOnClickListener {
            periodInput.setText("24")
        }

        period36.setOnClickListener {
            periodInput.setText("36")
        }

        grace0.setOnClickListener {
            gracePeriodInput.setText("0")
        }

        grace6.setOnClickListener {
            gracePeriodInput.setText("6")
        }

        grace12.setOnClickListener {
            gracePeriodInput.setText("12")
        }
    }

    // 입력값 초기화
    private fun resetInputs() {
        principalInput.setText("")
        principalKorean.text = ""
        interestInput.setText("")
        periodInput.setText("")
        gracePeriodInput.setText("")
        resultText.text = ""

        // 상환 일정 초기화
        scheduleAdapter.submitList(emptyList())
    }


    // 대출 계산을 수행하는 메서드
    private fun calculateLoan() {
        // 입력값 가져오기 및 숫자로 변환 (쉼표 제거)
        val principal = principalInput.text.toString().replace(",", "").toDoubleOrNull() ?: return
        val annualInterestRate = interestInput.text.toString().toDoubleOrNull() ?: return
        val totalPeriod = periodInput.text.toString().toIntOrNull() ?: return
        val gracePeriod = gracePeriodInput.text.toString().toIntOrNull() ?: 0

        val selectedPaymentMethod = paymentMethodSpinner.selectedItem.toString()

        // 상환 기간 계산 (총 기간 - 거치 기간)
        val repaymentPeriod = totalPeriod - gracePeriod

        // 월 이자율 계산
        val monthlyInterestRate = annualInterestRate / 100 / 12
        var remainingBalance = principal  // 잔액 초기화
        var totalInterest = 0.0  // 총 이자 초기화
        var totalPayment = 0.0  // 총 납입금액 초기화
        val scheduleList = mutableListOf<LoanSchedule>()  // 상환 일정 리스트 생성

        // 거치 기간 처리 (모든 상환 방식에 공통)
        for (month in 1..gracePeriod) {
            // 거치 기간에는 이자만 납부
            val interestPayment = remainingBalance * monthlyInterestRate

            totalInterest += interestPayment
            totalPayment += interestPayment

            scheduleList.add(
                LoanSchedule(
                    month,
                    interestPayment,  // 월 납입금 = 이자만
                    0.0,  // 원금 납부 없음
                    interestPayment,  // 이자 납부액
                    remainingBalance  // 잔액 변동 없음
                )
            )
        }

        when (selectedPaymentMethod) {
            "원금 균등 상환" -> {
                // 원금 균등상환 (매월 동일한 원금 + 잔액에 대한 이자)
                val monthlyPrincipalPayment = principal / repaymentPeriod

                for (i in 1..repaymentPeriod) {
                    val month = gracePeriod + i

                    // 이자 계산
                    val interestPayment = remainingBalance * monthlyInterestRate

                    // 마지막 회차 처리 (오차 보정)
                    if (i == repaymentPeriod) {
                        val finalPrincipalPayment = remainingBalance
                        val finalMonthlyPayment = finalPrincipalPayment + interestPayment

                        totalInterest += interestPayment
                        totalPayment += finalMonthlyPayment

                        scheduleList.add(
                            LoanSchedule(
                                month,
                                finalMonthlyPayment,
                                finalPrincipalPayment,
                                interestPayment,
                                0.0  // 잔액 0
                            )
                        )

                        remainingBalance = 0.0
                    } else {
                        // 일반 회차 처리
                        val monthlyPayment = monthlyPrincipalPayment + interestPayment

                        remainingBalance -= monthlyPrincipalPayment
                        totalInterest += interestPayment
                        totalPayment += monthlyPayment

                        scheduleList.add(
                            LoanSchedule(
                                month,
                                monthlyPayment,
                                monthlyPrincipalPayment,
                                interestPayment,
                                remainingBalance
                            )
                        )
                    }
                }
            }

            "원리금 균등 상환" -> {
                // 원리금 균등상환의 월 납입금 계산 (PMT 공식 사용)
                val monthlyPayment = if (monthlyInterestRate > 0) {
                    principal * monthlyInterestRate * (1 + monthlyInterestRate).pow(repaymentPeriod.toDouble()) /
                            ((1 + monthlyInterestRate).pow(repaymentPeriod.toDouble()) - 1)
                } else {
                    principal / repaymentPeriod  // 이자가 0인 경우 원금만 균등 분할
                }

                // 상환 기간 처리
                for (i in 1..repaymentPeriod) {
                    val month = gracePeriod + i

                    // 이자 계산
                    val interestPayment = remainingBalance * monthlyInterestRate

                    // 마지막 회차 처리 (오차 보정)
                    if (i == repaymentPeriod || remainingBalance < (monthlyPayment - interestPayment)) {
                        val finalPrincipalPayment = remainingBalance
                        val finalMonthlyPayment = finalPrincipalPayment + interestPayment

                        totalInterest += interestPayment
                        totalPayment += finalMonthlyPayment

                        scheduleList.add(
                            LoanSchedule(
                                month,
                                finalMonthlyPayment,
                                finalPrincipalPayment,
                                interestPayment,
                                0.0  // 잔액 0
                            )
                        )

                        remainingBalance = 0.0
                    } else {
                        // 일반 회차 처리
                        val principalPayment = monthlyPayment - interestPayment

                        remainingBalance -= principalPayment
                        totalInterest += interestPayment
                        totalPayment += monthlyPayment

                        scheduleList.add(
                            LoanSchedule(
                                month,
                                monthlyPayment,
                                principalPayment,
                                interestPayment,
                                remainingBalance
                            )
                        )
                    }
                }
            }

            "만기 일시 상환" -> {
                // 만기 일시 상환 (전체 기간 동안 이자만 납부, 마지막에 원금 상환)
                // 거치 기간 이후의 이자 납부 기간
                for (i in 1 until repaymentPeriod) {
                    val month = gracePeriod + i

                    // 이자만 납부
                    val interestPayment = remainingBalance * monthlyInterestRate

                    totalInterest += interestPayment
                    totalPayment += interestPayment

                    scheduleList.add(
                        LoanSchedule(
                            month,
                            interestPayment,
                            0.0,  // 원금 납부 없음
                            interestPayment,
                            remainingBalance  // 잔액 변동 없음
                        )
                    )
                }

                // 만기 회차 (마지막 달에 원금 상환)
                val finalMonth = totalPeriod
                val finalInterestPayment = remainingBalance * monthlyInterestRate
                val finalMonthlyPayment = remainingBalance + finalInterestPayment

                totalInterest += finalInterestPayment
                totalPayment += finalMonthlyPayment

                scheduleList.add(
                    LoanSchedule(
                        finalMonth,
                        finalMonthlyPayment,
                        remainingBalance,  // 원금 전체 상환
                        finalInterestPayment,
                        0.0  // 잔액 0
                    )
                )

                remainingBalance = 0.0
            }
        }

        // 결과 텍스트 설정
        resultText.text = "총 상환금: ${decimalFormat.format(totalPayment)}원\n"+
                "총 대출이자: ${decimalFormat.format(totalInterest)}원\n"+
                "총 상환기간: ${totalPeriod}개월(거치: ${gracePeriod}개월)\n"


        // 어댑터에 상환 일정 리스트 제출
        scheduleAdapter.submitList(scheduleList)
    }
}