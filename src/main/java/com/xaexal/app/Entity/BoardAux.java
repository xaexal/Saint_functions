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
@Table(name = "board_aux") // 실제 테이블 이름으로 수정하세요
@DynamicUpdate
public class BoardAux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // int unsigned 대응

    @Column(name = "type")
    private Integer type;

    @Column(name = "church_id")
    private Integer churchId;

    @Column(name = "level")
    private Integer level;

    @Column(nullable = false, length = 256)
    private String title;

    // MUL(Multiple) 인덱스가 걸려있는 외래키 컬럼
    @Column(name = "board_id")
    private Integer boardId;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean urgent = false;
}