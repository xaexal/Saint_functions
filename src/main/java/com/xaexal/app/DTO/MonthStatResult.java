package com.xaexal.app.DTO;

public interface MonthStatResult {
    String getMonth();  // "2025-08" 형태
    Long getCount();    // 해당 월의 등록 인원
}