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
@Table(name = "navi_role")
@Getter @Setter
@DynamicUpdate
public class NaviRole extends BaseEntity {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;
    
    @Column(name = "navi_id", columnDefinition = "INT UNSIGNED")
    private Integer naviId;

    @Column(name="church_id", columnDefinition="INT UNSIGNED")
    private Integer churchId;
    
    @Column(name="title", length=32)
    private String title;
    
    @Column(name="permissions", length=128)
    private String permissions;

}
