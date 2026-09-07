package com.xaexal.app.Entity;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff")
@DynamicUpdate
public class Staff extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // int unsigned, PRI, auto_increment

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "pstn_id")
    private Integer pstnId; // MUL (인덱스 존재)

    @Column(length=10)
    private String started; // date

    @Column(length=10)
    private String retired; // date

    @Column(columnDefinition = "ENUM('0', '1')")
    private String active = "1";
}