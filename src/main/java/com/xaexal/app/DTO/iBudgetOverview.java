package com.xaexal.app.DTO;

import java.math.BigDecimal;

public interface iBudgetOverview {
    Integer getTypeId();
    Integer getParId();
    Integer getDepth();
    String getTypename();
    BigDecimal getBudgetAmount();
    BigDecimal getSpentAmount();
}
