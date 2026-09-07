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
@Table(name = "navi")
@Getter @Setter
@DynamicUpdate
public class Navi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "par_id", columnDefinition = "INT UNSIGNED")
    private Integer parId; // 부모 메뉴 ID

    @Column(length = 32)
    private String title; // 메뉴명

    @Column(columnDefinition = "ENUM('0', '1')")
    @ColumnDefault("'1'")
    private String active; // 사용 여부

    @ColumnDefault("-1")
    private Integer seqno; // 정렬 순서 (기본값 -1)

    @Column(length = 32)
    private String path; // URL 경로 (예: /church/list)

    @Column(length = 32)
    private String component; // 프론트엔드 컴포넌트명

    @Column(name = "not4general", columnDefinition = "ENUM('0','1')")
    @ColumnDefault("'0'")
    private String not4general;
}