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
@Table(name = "reply")
@Getter @Setter
@DynamicUpdate
public class Reply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "par_id", columnDefinition = "INT UNSIGNED")
    private Integer parId; // 부모 댓글 ID (대댓글 구현용)

    @Column(name = "old_id", length = 13)
    private String oldId; // 구 시스템 참조용 ID

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content; // 댓글 본문
}