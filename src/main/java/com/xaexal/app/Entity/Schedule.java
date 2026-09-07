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
@Table(name = "schedule")
@Getter @Setter
@DynamicUpdate
public class Schedule extends BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;
	
	@Column(name="church_id")
    private Integer churchId;
	
	@Column(length=32,nullable=false)
	private String title;
	
	@Column(length=32)
	private String iteration;

	@Column(length=32)
	private String dow;

	@Column(length=64)
	private String nthweek;

	private Integer nthday;

    @Column(name = "start_tm", length = 10)
    private String startTm;

    @Column(name="place", columnDefinition = "INT UNSIGNED")
    private Integer place;

    @Column(name="countable")
    @ColumnDefault("'1'")
    private String countable; // 참여인원 통계을 낼 것인지 여부
    
    @Column(columnDefinition = "ENUM('1', '0')")
    @ColumnDefault("'1'")
    private String visible;
}
