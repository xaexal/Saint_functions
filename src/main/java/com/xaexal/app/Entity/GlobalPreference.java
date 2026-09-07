package com.xaexal.app.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// 특정 교회에 속하지 않은 교적관리최고책임자(church_id=0)를 위한 전역 환경설정.
// 항상 id=1인 단일 행만 사용한다(church 테이블처럼 여러 교회를 가지지 않으므로).
@Entity
@Table(name = "global_preference")
@Getter @Setter
public class GlobalPreference {

    @Id
    private Integer id;

    @Column(name="timeout")
    private Integer timeout = 0;

    @Column(name="per_page")
    private Integer perPage = 20;

    @Column(name="default_passwd")
    private String defaultPassword;

    @Column(name="community_name", length=32)
    private String communityName;

    @Column(name="show_donation", length=1)
    private String showDonation = "0";
}
