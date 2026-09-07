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
@Table(name = "candidate")
@Getter @Setter
@DynamicUpdate
// 교유과정 해당자 명단보관용
public class Candidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;
    
    @Column(name = "school_id", columnDefinition = "INT UNSIGNED")
    private Integer schoolId;

    @Column(name = "candidate_id", columnDefinition = "INT UNSIGNED")
    private Integer candidateId; // member Id of the candidate
}
