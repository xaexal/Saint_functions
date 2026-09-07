package com.xaexal.app.Entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@MappedSuperclass // 실제 테이블로 생성되지 않고 매핑 정보만 상속해주는 클래스
public abstract class BaseEntity {

    @Column(insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime created;

    @Column(insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime updated;

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer writer;
}