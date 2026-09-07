package com.xaexal.app.Entity;

import org.hibernate.annotations.ColumnDefault;
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
@Table(name = "board")
@Getter @Setter
@DynamicUpdate
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "par_id", columnDefinition = "INT UNSIGNED")
    private Integer parId; // 답글인 경우 부모 게시글의 ID

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content; // 게시글 본문

    @Column(columnDefinition = "INT UNSIGNED")
    @ColumnDefault("0")
    private Integer hit; // 조회수
}