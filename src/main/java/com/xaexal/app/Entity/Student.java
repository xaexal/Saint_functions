package com.xaexal.app.Entity;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student", uniqueConstraints = @UniqueConstraint(name = "uk_student_school_member", columnNames = {"school_id", "member_id"}))
@Getter @Setter
@DynamicUpdate
public class Student extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "school_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer schoolId;

    @Column(name = "member_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer memberId;

    @Column(columnDefinition = "ENUM('미처리', '접수완료')")
    @ColumnDefault("'미처리'")
    private String confirmed;

    @Column(columnDefinition = "ENUM('미수료', '수료')")
    @ColumnDefault("'미수료'")
    private String graduated;

    @Column(columnDefinition = "ENUM('신청', '수강중', '수료', '중도포기', '미참석')")
    @ColumnDefault("'신청'")
    private String status;

}