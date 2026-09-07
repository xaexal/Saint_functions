package com.xaexal.app.Repository;

import com.xaexal.app.DTO.iUserRoles;
import com.xaexal.app.Entity.UserRoles;
import com.xaexal.app.Entity.UserRolesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRolesRep extends JpaRepository<UserRoles, UserRolesId> {

    @Query(value =
        "SELECT s.id AS id, s.member_id AS memberId, s.church_id AS churchId, s.role AS roleId, " +
        "m.name, IFNULL(m.mobile, '') AS mobile, IFNULL(s.registered, '') AS registered, " +
        "IFNULL(c.name, '') AS churchName, IFNULL(s.active, '0') AS active, IFNULL(COALESCE(r.rname, rg.rname), '') AS rname " +
        "FROM saint s " +
        "JOIN member m ON m.id = s.member_id " +
        "LEFT JOIN church c ON c.id = s.church_id " +
        "LEFT JOIN _roles r ON r.id = s.role AND r.church_id = s.church_id " +
        "LEFT JOIN _roles rg ON rg.id = s.role AND rg.church_id = 0 " +
        "WHERE s.active = '1' " +
        "ORDER BY m.name, rname",
        nativeQuery = true)
    List<iUserRoles> findAllWithNames();

    @Query(value =
        "SELECT s.id AS id, s.member_id AS memberId, s.church_id AS churchId, s.role AS roleId, " +
        "m.name, IFNULL(m.mobile, '') AS mobile, IFNULL(s.registered, '') AS registered, " +
        "IFNULL(c.name, '') AS churchName, IFNULL(s.active, '0') AS active, IFNULL(COALESCE(r.rname, rg.rname), '') AS rname " +
        "FROM saint s " +
        "JOIN member m ON m.id = s.member_id " +
        "LEFT JOIN church c ON c.id = s.church_id " +
        "LEFT JOIN _roles r ON r.id = s.role AND r.church_id = s.church_id " +
        "LEFT JOIN _roles rg ON rg.id = s.role AND rg.church_id = 0 " +
        "WHERE s.id = :saintId AND s.active = '1'",
        nativeQuery = true)
    iUserRoles findOneBySaintId(@Param("saintId") int saintId);
}
