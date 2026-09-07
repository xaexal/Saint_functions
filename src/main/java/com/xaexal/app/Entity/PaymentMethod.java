package com.xaexal.app.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_methods")
@Getter @Setter
public class PaymentMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "church_id", columnDefinition = "INT UNSIGNED")
    private Integer churchId;

    @Column(name = "member_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer memberId;

    @Column(columnDefinition = "ENUM('card','account')")
    private String type;

    @Column(name = "billing_key", length = 128)
    private String billingKey;

    @Column(columnDefinition = "ENUM('active','expired','deleted')")
    private String status;

    @Column(name = "card_company", length = 32)
    private String cardCompany;

    @Column(name = "card_number_masked", length = 20)
    private String cardNumberMasked;

    @Column(name = "card_type", length = 32)
    private String cardType;

    @Column(name = "card_expiry_year_month", length = 7)
    private String cardExpiryYearMonth;

    @Column(name = "bank_code", length = 32)
    private String bankCode;

    @Column(name = "account_number_masked", length = 32)
    private String accountNumberMasked;

    @Column(name = "accound_holder_name", length = 32)
    private String accountHolderName;

    @Column(name = "account_type", length = 32)
    private String accountType;

    @Column(name = "is_default", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDefault = false;
}
