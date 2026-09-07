package com.xaexal.app.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "income")
@Getter @Setter
@DynamicUpdate
public class Income extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "church_id", columnDefinition = "INT UNSIGNED")
    private Integer churchId;

    private Integer typeId;

    @Column(precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal amount;

    @Column(name = "issued")
    private LocalDate issued;

    @Column(length = 128)
    private String remark;
}
