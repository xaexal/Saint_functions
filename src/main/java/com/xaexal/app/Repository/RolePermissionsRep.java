package com.xaexal.app.Repository;

import com.xaexal.app.DTO.iRolePermissions;
import com.xaexal.app.Entity.RolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RolePermissionsRep extends JpaRepository<RolePermissions, Integer> {
    boolean existsByChurchId(Integer churchId);
    boolean existsByChurchIdAndRidAndPid(Integer churchId, Integer rid, String pid);

    @Query(value =
        "SELECT rp.rid AS rid, rp.pid, " +
        "  CONCAT(n.title, ' ', CASE WHEN rp.pid LIKE '%\\_CRUD' THEN '관리' ELSE '읽기' END) AS pname " +
        "FROM _role_permissions rp " +
        "JOIN navi n ON n.id = CAST(SUBSTRING_INDEX(rp.pid, '_', 1) AS UNSIGNED) " +
        "WHERE rp.church_id = :churchId " +
        "ORDER BY rp.rid, n.title",
        nativeQuery = true)
    List<iRolePermissions> findAllWithNames(@Param("churchId") int churchId);

    @Query(value =
        "SELECT DISTINCT rp.pid " +
        "FROM _role_permissions rp " +
        "WHERE rp.church_id IN (0, :churchId) AND rp.rid = :roleId " +
        "ORDER BY rp.pid",
        nativeQuery = true)
    List<String> findByChurchIdAndRoleId(@Param("churchId") int churchId, @Param("roleId") int roleId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM _role_permissions WHERE church_id IN (0, :churchId) AND rid = :rid AND pid = :pid",
        nativeQuery = true)
    void deleteByChurchIdAndRidAndPid(@Param("churchId") int churchId, @Param("rid") int rid, @Param("pid") String pid);

    @Modifying
    @Transactional
    @Query(value =
        "INSERT IGNORE INTO _role_permissions (rid, pid, church_id) " +
        "SELECT rp.rid, rp.pid, :churchId " +
        "FROM _role_permissions rp " +
        "WHERE rp.church_id = 0",
        nativeQuery = true)
    void copyFromDefault(@Param("churchId") int churchId);

    @Modifying
    @Transactional
    @Query(value =
        "INSERT IGNORE INTO _role_permissions (rid, pid, church_id) " +
        "SELECT DISTINCT rid, :pid, church_id FROM _role_permissions WHERE rid = :rid",
        nativeQuery = true)
    void saveForAllChurches(@Param("rid") int rid, @Param("pid") String pid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM _role_permissions WHERE rid = :rid AND pid = :pid",
        nativeQuery = true)
    void deleteAllChurchesByRidAndPid(@Param("rid") int rid, @Param("pid") String pid);
}
