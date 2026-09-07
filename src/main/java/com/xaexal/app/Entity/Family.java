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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "family", uniqueConstraints = @UniqueConstraint(
        name = "uq_family_relation", columnNames = { "member_id", "relation_type" }))
@Getter @Setter
@DynamicUpdate
public class Family extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "member_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer memberId; // 관계의 기준이 되는 회원 (자녀 또는 배우자 등록자)

    @Column(name = "relative_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer relativeId; // 상대 회원 (부/모/배우자)

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", columnDefinition = "ENUM('부','모','배우자')", nullable = false)
    private RelationType relationType;

    public enum RelationType {
        부, 모, 배우자
    }
}
