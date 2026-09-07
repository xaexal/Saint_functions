package com.xaexal.app.DTO;

import java.math.BigDecimal;

public interface iExpense {
    Integer getId();
    Integer getChurchId();
    Integer getTypeId();
    String getTypename();
    BigDecimal getAmount();
    String getIssued();
    String getRemark();
    String getUpdated();
}
