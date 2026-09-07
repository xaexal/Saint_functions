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
@Table(name = "offering")
@Getter @Setter
@DynamicUpdate
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "income_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer incomeId;

    @Column(name = "member_id", columnDefinition = "INT UNSIGNED")
    private Integer memberId; // 헌금자 회원 ID

    @Column(length = 24)
    private String mobile;

    @Column(length = 24)
    private String name; // 헌금자 성함 (비회원인 경우 대비)
}