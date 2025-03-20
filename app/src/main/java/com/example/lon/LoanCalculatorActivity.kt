// 애플리케이션의 패키지 선언
package com.example.lon

// 필요한 안드로이드 및 자바 클래스 가져오기
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

// AppCompatActivity를 상속받는 대출 계산기의 메인 액티비티 클래스
class LoanCalculatorActivity : AppCompatActivity() {

    // UI 요소를 위한 private 속성들
    private lateinit var principalInput: EditText      // 대출 원금 입력 필드
    private lateinit var interestInput: EditText       // 이자율 입력 필드
    private lateinit var paymentInput: EditText        // 월 납입금 입력 필드
    private lateinit var calculateButton: Button       // 계산 버튼
    private lateinit var resultText: TextView          // 결과 표시 텍스트뷰
    private lateinit var scheduleRecyclerView: RecyclerView  // 상환 일정을 표시할 리사이클러뷰
    private lateinit var scheduleAdapter: LoanScheduleAdapter  // 상환 일정 어댑터


    // 숫자 포맷을 위한 DecimalFormat 객체
    private val decimalFormat = DecimalFormat("#,###")

    // 액티비티가 생성될 때 호출되는 메서드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_calculator)  // 레이아웃 설정

        // XML에서 UI 요소를 찾아 변수에 할당
        principalInput = findViewById(R.id.principalInput)
        interestInput = findViewById(R.id.interestInput)
        paymentInput = findViewById(R.id.paymentInput)
        calculateButton = findViewById(R.id.calculateButton)
        resultText = findViewById(R.id.resultText)
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView)

        // 리사이클러뷰에 어댑터와 레이아웃 매니저 설정
        scheduleAdapter = LoanScheduleAdapter()
        scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        scheduleRecyclerView.adapter = scheduleAdapter

        // 계산 버튼 클릭 이벤트 리스너 설정
        calculateButton.setOnClickListener {
            calculateLoan()  // 대출 계산 메서드 호출
        }
    }

    // 대출 계산을 수행하는 메서드
    private fun calculateLoan() {
        // 입력값 가져오기 및 숫자로 변환 (쉼표 제거)
        val principal = principalInput.text.toString().replace(",", "").toDoubleOrNull() ?: return
        val annualInterestRate = interestInput.text.toString().toDoubleOrNull() ?: return
        var monthlyPayment = paymentInput.text.toString().replace(",", "").toDoubleOrNull() ?: return

        // 월 이자율 계산
        val monthlyInterestRate = annualInterestRate / 100 / 12
        var remainingBalance = principal  // 잔액 초기화
        var month = 0  // 월 카운터 초기화
        var totalPayment = 0.0  // 총 납입금액 초기화
        val scheduleList = mutableListOf<LoanSchedule>()  // 상환 일정 리스트 생성

        // 잔액이 0보다 큰 동안 반복
        while (remainingBalance > 0) {
            month++  // 월 증가
            val interestPayment = remainingBalance * monthlyInterestRate  // 이자 금액 계산
            val principalPayment = monthlyPayment - interestPayment  // 원금 납입액 계산

            // 잔액이 월 납입금보다 작은 경우 (마지막 납입)
            if (remainingBalance < monthlyPayment) {
                monthlyPayment = remainingBalance + interestPayment  // 마지막 납입금 조정
            }

            remainingBalance -= principalPayment  // 잔액에서 원금 납입액 차감
            totalPayment += monthlyPayment  // 총 납입금액 누적

            // 상환 일정 리스트에 현재 월의 정보 추가
            scheduleList.add(LoanSchedule(month, monthlyPayment, principalPayment, interestPayment, remainingBalance))

            // 잔액이 거의 0이면 반복 종료
            if (remainingBalance < 0.01) break
        }

        // 총 이자액 계산
        val totalInterest = totalPayment - principal
        // 결과 텍스트 설정
        resultText.text = "총 상환 기간: ${month}개월\n총 이자액: ${decimalFormat.format(totalInterest)}원"

        // 어댑터에 상환 일정 리스트 제출
        scheduleAdapter.submitList(scheduleList)
    }
}