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
@Table(name = "manager")
@Getter @Setter
@DynamicUpdate
public class Manager extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "member_id", columnDefinition = "INT UNSIGNED")
    private Integer memberId; // 부모 메뉴 ID

    @Column(name="church_id", columnDefinition = "INT UNSIGNED")
    private Integer churchId; // 교회 ID

}
/*
교적관리자의 member_id와 관리하는 교회의 church_id 
*/
