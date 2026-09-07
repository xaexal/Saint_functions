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
@Table(name = "position")
@Getter @Setter
@DynamicUpdate
public class Position extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "polity_id", columnDefinition = "INT UNSIGNED")
    private Integer polityId; // 소속 규정 ID (어떤 정관에 근거한 직분인지)

    @Column(length = 32)
    private String title; // 직분명 (예: 담임목사, 시무장로)

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer seqno; // 출력 순서
}