package com.example.lon

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoanPagerAdapter(activity: LoanCalculatorActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> LoanCalculatorFragment()
            1 -> LoanComparisonFragment()  // 대출 비교
            2 -> SavingsCalculatorFragment()  // 적금 계산
            3 -> MyLoanFragment()  // 마이 대출
            else -> LoanCalculatorFragment()
        }
    }
}