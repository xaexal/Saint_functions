package com.xaexal.app.Entity;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "church")
@Getter @Setter
@DynamicUpdate
public class Church extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(length = 32)
    private String name;

    @Column(length = 128)
    private String address;

    @Column(length = 5)
    private String postcode;

    @Column(length = 32)
    private String phone;

    @Column(length = 24)
    private String affiliate; // 소속 교단/단체

    @Column(length = 10)
    private String founded;   // 설립일 (String 유지)

    @Column(length = 32)
    private String pastor;    // 담임목사 이름

    @Column(name = "pastor_id", columnDefinition = "INT UNSIGNED")
    private Integer pastorId; // 담임목사 사용자 ID (FK용)

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer creator;
    
    @Column(name="timeout")
    private Integer timeout=10;
    
    @Column(name="default_passwd")
    private String defaultPassword;
    
    @Column(name="per_page")
    private Integer perPage=20;

    @Column(name="community_name", length=32)
    private String communityName;

    @Column(name="show_donation", length=1)
    private String showDonation = "0";

    @Column(name="payday")
    private Integer payday; // 다음 자동결제일의 '일'(day of month). 구독 등록/갱신 시점에 기록

    @Column(name="capita",columnDefinition = "INT UNSIGNED")
    private Integer capita=100;

    @Column(name="expense",columnDefinition = "INT UNSIGNED")
    private Integer expense; // 매일 자정 배치로 계산되는 비용 (등록교인수 x capita)

}