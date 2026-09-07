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
@Table(name = "bulletin")
@Getter @Setter
@DynamicUpdate
public class Bulletin extends BaseEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

	@Column(name="church_id",columnDefinition="INT UNSIGNED")
	private Integer churchId;
	
	@Column(name="title")
	private String title;
	
	@Column(name="hit")
	private Integer hit = 0;
}
