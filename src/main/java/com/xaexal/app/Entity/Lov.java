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
@Table(name = "lov")
@Getter @Setter
@DynamicUpdate
public class Lov extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "par_id", columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer parId; // 부모 항목 ID (대분류 등)

    @Column(name = "church_id", columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer churchId;

    @Column(length = 32)
    private String name;

    @Column(length = 128)
    private String remark;

    @Column(columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer seqno; // 정렬 순서

    @Column(columnDefinition = "ENUM('1', '0')")
    @ColumnDefault("'1'")
    private String visible; // 노출 여부

}