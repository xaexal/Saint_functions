package com.xaexal.app.Repository;

import com.xaexal.app.Entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RolesRep extends JpaRepository<Roles, Integer> {
    List<Roles> findByChurchIdOrderByRname(int churchId);
    boolean existsByChurchId(Integer churchId);
    Optional<Roles> findByChurchIdAndRname(int churchId, String rname);

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO _roles (rid, church_id, rname, remark) " +
        "SELECT id, :churchId, rname, remark FROM _roles " +
        "WHERE church_id = 0 AND not4general='0'",
        nativeQuery = true)
    void copyFromDefault(@Param("churchId") int churchId);

    // copyFromDefault()로 복제된 새 교회의 역할들에, 템플릿(church_id=0)의 par_id 계층을
    // 그 교회 자신의 row id로 치환해서 채운다. 템플릿에서 par_id=0(최상위)이면 그대로 0.
    @Modifying
    @Transactional
    @Query(value =
        "UPDATE _roles child " +
        "JOIN _roles tChild ON tChild.id = child.rid AND tChild.church_id = 0 " +
        "LEFT JOIN _roles parent ON parent.rid = tChild.par_id AND parent.church_id = child.church_id " +
        "SET child.par_id = CASE WHEN tChild.par_id IS NULL OR tChild.par_id = 0 THEN 0 ELSE parent.id END " +
        "WHERE child.church_id = :churchId",
        nativeQuery = true)
    void fixParIds(@Param("churchId") int churchId);

    // 템플릿 역할 계층(church_id=0, par_id)에서 rootId의 모든 하위 역할 id를 재귀적으로 조회
    @Query(value =
        "WITH RECURSIVE desc_roles AS ( " +
        "  SELECT id FROM _roles WHERE par_id = :rootId AND church_id = 0 " +
        "  UNION ALL " +
        "  SELECT r.id FROM _roles r INNER JOIN desc_roles d ON r.par_id = d.id WHERE r.church_id = 0 " +
        ") SELECT id FROM desc_roles",
        nativeQuery = true)
    List<Integer> findDescendantIds(@Param("rootId") int rootId);
}
