package com.xaexal.app.Entity;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "one2one")
@Getter
@Setter
@DynamicUpdate
public class One2one extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(name = "church_id")
    private Long churchId;

    @Column(length = 32)
    private String title;

    @Column(length = 7)
    private String operator;

    private Integer grade;

    @Column(name = "par_id", columnDefinition = "INT UNSIGNED")
    private Long parId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String comment;
}
