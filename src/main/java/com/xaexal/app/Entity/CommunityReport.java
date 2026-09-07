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
@Table(name = "community_report")
@Getter @Setter
@DynamicUpdate
public class CommunityReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;
    
    @Column(name="polity_id",columnDefinition="INT UNSIGNED")
    private Integer polityId;
    
    @Column(name="meeting_dt",length=10)	// 모인날짜
    private String meetingDt;
    
    @Column(name="meetting_tm",length=5)	// 모인시간
    private String meetingTm;
    
    @Column(name="meeting_place",length=32)	// 모임장소
    private String meetingPlace;
    
    @Column(name="leader",length=32)	// 인도자
    private String leader;

    @Column(name="attendants",columnDefinition="TEXT")	// 참석자
    private String attendants;
    
    @Column(name="fellowship",columnDefinition="TEXT") // 교제내용
    private String fellowship;
    
    @Column(name="prayer_topics",columnDefinition="TEXT") // 기도제목나눔
    private String prayerTopics;
    
}
