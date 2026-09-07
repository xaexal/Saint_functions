package com.xaexal.app.Entity;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "applicant")
@Getter @Setter
@DynamicUpdate
public class Applicant extends BaseEntity {

    public enum Role { mentee, mentor }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name="church_id")
    private Integer churchId;

    @Column(name="priority_id")
    private Integer priorityId;

    @Column(name="grade")
    private Integer grade;

    @Enumerated(EnumType.STRING)
    @Column(name="role", columnDefinition = "ENUM('mentee','mentor')")
    private Role role;

    @Column(name="applied",length=14)
    private String applied;

    @Column(name="matched",length=14)
    private String matched;
}
