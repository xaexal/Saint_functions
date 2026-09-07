package com.xaexal.app.DTO;

public interface iPaymentMethod {
    Integer getId();
    String getMemberName();
    String getType();
    String getCardCompany();
    String getCardNumberMasked();
    String getCardType();
    String getCardExpiryYearMonth();
    String getBankCode();
    String getAccountNumberMasked();
    String getAccountHolderName();
    String getAccountType();
    Boolean getIsDefault();
}
