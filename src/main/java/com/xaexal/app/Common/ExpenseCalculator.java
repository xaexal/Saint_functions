package com.xaexal.app.Common;

import java.time.LocalDate;

public class ExpenseCalculator {

    private ExpenseCalculator() {}

    // payday < 오늘(일) 이면 이번달 payday+1일부터, payday >= 오늘(일) 이면 이전달 payday+1일부터
    // 그 기간의 (등록교인수 x capita) 누적합계를 기간일수로 나눈 값 == 등록교인수 x capita (매일 동일값이므로)
    public static Integer calculate(Integer payday, Integer capita, int memberCount, LocalDate today) {
        if (payday == null || payday <= 0) return null;
        LocalDate firstOfMonth = payday < today.getDayOfMonth()
            ? today.withDayOfMonth(1)
            : today.withDayOfMonth(1).minusMonths(1);
        LocalDate start = firstOfMonth.plusDays(payday);
        if (start.isAfter(today)) return null;
        int cap = capita == null ? 0 : capita;
        return memberCount * cap;
    }
}
