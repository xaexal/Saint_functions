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
@NoArgsConstructor // 기본 생성자 추가
@Table(name = "board_type")
@DynamicUpdate
public class BoardType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer parId;

    @Column(nullable = false, length = 32)
    private String name; // 게시판 이름 (예: 공지사항)
}
