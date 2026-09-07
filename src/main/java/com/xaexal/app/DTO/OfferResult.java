package com.xaexal.app.DTO;

public interface OfferResult {
    String getName();    // a.name
    String getOffered(); // b.offered
    Long getTotal();     // b.total (SUM 결과이므로 Long 권장)
}