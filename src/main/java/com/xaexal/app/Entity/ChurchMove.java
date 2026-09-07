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
@Table(name = "church_move")
@Getter @Setter
@DynamicUpdate
public class ChurchMove extends BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

	@Column(name="member_id", columnDefinition="INT UNSIGNED")
	private Integer memberId;
	
	@Column(name="old_church", columnDefinition="INT UNSIGNED")
	private Integer old_church;

	@Column(name="new_church", columnDefinition="INT UNSIGNED")
	private Integer new_church;
	
}
