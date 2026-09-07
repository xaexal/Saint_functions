package com.xaexal.app.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface iOffer {
	Integer getId();
	Integer getIncomeId();
	Integer getChurchId();
	String getChurchName();
    Integer getTypeId();
    BigDecimal getAmount(); // 헌금 금액 (정밀도 유지를 위해 BigDecimal 사용)
    Integer getMemberId(); // 헌금자 회원 ID
    String getMobile();
    LocalDate getOffered(); // 헌금 드린 날짜 (date 타입 매핑)
    String getTypename();
    String getName();
    String getUpdated();
}
