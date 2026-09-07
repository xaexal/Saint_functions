package com.xaexal.app.Entity;

import org.hibernate.annotations.ColumnDefault;
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
@Table(name = "baptism")
@Getter @Setter
@DynamicUpdate
public class Baptism extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "member_id", columnDefinition = "INT UNSIGNED")
    private Integer memberId;

    // enum('없음','성인세례','유아세례','입교','학습') 매핑
    @Column(columnDefinition = "ENUM('없음', '성인세례', '유아세례', '입교', '학습')")
    @ColumnDefault("'없음'")
    private String type;

    @Column(name = "bapt_church_id", columnDefinition = "INT UNSIGNED")
    private Integer baptChurchId; // 세례 받은 교회 ID

    @Column(name = "bapt_church_name", length = 64)
    private String baptChurchName; // 세례 받은 교회 이름 (직접 입력 대비)

    @Column(length = 64)
    private String pastor; // 집례자(목사님) 성함

    @Column(length=10)
    private String baptized; // 수세일자(연월일 정확도가 떨어지므로 문자열타입

}