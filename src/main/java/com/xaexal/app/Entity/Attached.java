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
@Table(name = "attached")
@Getter @Setter
@DynamicUpdate
public class Attached extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name="source")
    private String source = "b"; // b:공지사항,게시판 등등, w:주보
    
    @Column(name = "board_id", columnDefinition = "INT UNSIGNED")
    private Integer boardId; // 연결된 게시글 ID

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer seqno; // 한 게시글 내 파일 순서

    @Column(name = "org_name", length = 128, nullable = false)
    private String orgName; // 사용자가 올린 실제 파일명

    @Column(name = "new_name", length = 128, nullable = false)
    private String newName; // 서버에 저장된 난수화된 파일명
}