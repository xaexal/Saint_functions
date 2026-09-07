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

// 예산과목 트리 (구조는 lov와 동일하게 church_id/par_id 계층을 쓰지만,
// 예산/지출 전용으로 분리하고 항목별 유효기간(start_date~end_date)을 추가로 관리한다.
@Entity
@Table(name = "budget")
@Getter @Setter
@DynamicUpdate
public class BudgetAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "par_id", columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer parId;

    @Column(name = "church_id", columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer churchId;

    @Column(length = 32)
    private String name;

    @Column(length = 128)
    private String remark;

    @Column(columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer seqno;

    @Column(precision = 20, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal amount;

    @Column(name = "start_date")
    private LocalDate startDate; // 이 예산과목의 유효기간 시작일

    @Column(name = "end_date")
    private LocalDate endDate; // 이 예산과목의 유효기간 종료일
}
