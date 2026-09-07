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
@Table(name = "attendence_register")
@Getter @Setter
@DynamicUpdate
// 출석부
public class AttendenaceRegister extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;
    
    @Column(name = "member_id", columnDefinition = "INT UNSIGNED")
    private Integer memberId;
    @Column(name = "school_id", columnDefinition = "INT UNSIGNED")
    private Integer schoolId;
    
    @Column(name = "attendent_date", length=10)
    private String  attendenceDate;
    
    @Column(name="attended",columnDefinition="ENUM('출석','결석','지각','조퇴','지각&조퇴')")
    private String  attdended; // 출석,지각,조퇴,결석
    
    @Column(name="attendence_time",length=11)
    private String  attendenceTime; // xx:xx~xx:xx
    
    @Column(name="remark",columnDefinition="TEXT")
    private String  remark;
}
