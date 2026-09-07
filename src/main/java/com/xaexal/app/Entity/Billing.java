package com.xaexal.app.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "billing")
@Getter @Setter
public class Billing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "member_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer memberId;

    @Column(name = "customer_key", length = 64, nullable = false)
    private String customerKey;

    @Column(name = "billing_key", length = 256, nullable = false)
    private String billingKey;

    @Column(length = 20)
    private String method;

    @Column(name = "card_company", length = 32)
    private String cardCompany;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(columnDefinition = "ENUM('0','1') DEFAULT '1'")
    private String active = "1";
}
