package com.xaexal.app.DTO;

public interface DailyStatResult {
    String getDate();   // "2026-08-23" 형태
    Long getCount();    // 해당 일자 기준 누적 등록교인 수
}
