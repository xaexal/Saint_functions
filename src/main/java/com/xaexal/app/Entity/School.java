package com.xaexal.app.Entity;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "school")
@Getter @Setter
@DynamicUpdate
public class School extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "type_id", columnDefinition = "INT UNSIGNED")
    private Integer typeId;

    @Column(name = "type_name", length = 32)
    private String typeName;

    @Column(length = 64, nullable = false) // NO PRI이므로 필수값 설정
    private String title;

    @Column(name = "pastor_id", columnDefinition = "INT UNSIGNED")
    private Integer pastorId;

    @Column(length = 32)
    private String pastor;

    @Column(length = 128)
    private String staff;

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

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer place;

    @Column(columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer pax; // 모집정원

    @Lob // longtext, text 타입 매핑
    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(columnDefinition = "ENUM('1', '0')")
    @ColumnDefault("'1'")
    private String visible;
}