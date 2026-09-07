package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.CommunityReportDetail;
import com.xaexal.app.DTO.CommunityReportList;
import com.xaexal.app.Entity.CommunityReport;

public interface CommunityReportRep extends JpaRepository<CommunityReport, Integer> {

    @Query(value =
        "SELECT cr.id, cr.meeting_dt, m.name writer_name " +
        "FROM community_report cr " +
        "LEFT JOIN member m ON cr.writer = m.id " +
        "WHERE cr.polity_id = :polity_id " +
        "ORDER BY cr.meeting_dt DESC",
        nativeQuery = true)
    List<CommunityReportList> findByPolityId(@Param("polity_id") int polityId);

    @Query(value =
        "SELECT cr.id, cr.polity_id, cr.meeting_dt, cr.meetting_tm meeting_tm, " +
        "cr.meeting_place, cr.leader, cr.attendants, cr.fellowship, cr.prayer_topics, " +
        "m.name writer_name " +
        "FROM community_report cr " +
        "LEFT JOIN member m ON cr.writer = m.id " +
        "WHERE cr.id = :id",
        nativeQuery = true)
    CommunityReportDetail findDetailById(@Param("id") int id);
}
