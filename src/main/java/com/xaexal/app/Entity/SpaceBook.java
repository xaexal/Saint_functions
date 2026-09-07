package com.xaexal.app.Entity;

import java.time.LocalDateTime;

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
@Table(name = "space_book")
@Getter @Setter
@DynamicUpdate
public class SpaceBook extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name="space_id",columnDefinition="INT UNSIGNED")
    private Integer spaceId;

    @Column(name = "start_dt", columnDefinition = "CHAR(10)")
    private String startDt;

    @Column(name = "close_dt", columnDefinition = "CHAR(10)")
    private String closeDt;

    @Column(name = "start_tm", length = 10)
    private String startTm;

    @Column(name = "close_tm", length = 10)
    private String closeTm;

    @Column(length = 32)
    private String dow; // Day of Week (요일)

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer nthday;

    @Column(length = 32)
    private String nthweek;

    @Column(length = 20)
    private String iteration; // 반복 주기

    @Column(columnDefinition="INT UNSIGNED")
    private Integer applier;  // applier's member id

    @Column(columnDefinition="INT UNSIGNED")
    private Integer approver;  // approver's member id

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime approved; // 처리(승인/거부)한 시각

    @Column(columnDefinition = "ENUM('1','0')")
    private String approval; // 1=승인, 0=거부

    @Column(name = "applier_name", length = 24)
    private String applierName;

    @Column(name = "applier_mobile", length = 24)
    private String applierMobile;

    @Column(columnDefinition="TEXT")
    private String purpose; // 용도/사용목적

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer pax = 0; // 예상인원

    @Column(columnDefinition = "TEXT")
    private String remark; // 게시글 본문
}