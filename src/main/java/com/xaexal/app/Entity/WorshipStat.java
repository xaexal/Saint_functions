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
@Table(name = "worship_stat")
@Getter @Setter
@DynamicUpdate
public class WorshipStat extends BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

	@Column(name="served")
	private String served;
	
	@Column(name="worship_id")
    private Integer worshipId;
	
	@Column(name="attendant",columnDefinition="INT UNSIGNED")
	private Integer attendant;
	
	@Column(name="offering")
	private Double offering;
}
