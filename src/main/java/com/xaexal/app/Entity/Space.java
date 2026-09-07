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
@Table(name = "space")
@Getter @Setter
@DynamicUpdate
public class Space extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name="church_id", columnDefinition = "INT UNSIGNED")
    private Integer churchId; // 교회 ID

    @Column(length = 32)
    private String title; // 메뉴명

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer pax=0; // 수용인원

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer size; // 면적

    @Column(columnDefinition = "TEXT")
    private String remark; // 게시글 본문

    @Column(columnDefinition = "ENUM('0','1')")
    private String bookable;
}