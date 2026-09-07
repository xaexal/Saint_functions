package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.iNaviSub;
import com.xaexal.app.Entity.Navi;

public interface NaviRep extends JpaRepository<Navi, Integer> {
	// 비로그인 사용자에게 공개된 전체 메뉴(최상위 + 하위) 한번에 조회
	@Query(value =
        "SELECT n.id, n.par_id, n.title, n.path, n.component, n.seqno, NULL AS pname " +
        "FROM navi n " +
        "WHERE n.active='1' " +
        "  AND (n.id IN (SELECT id FROM navi WHERE par_id=0 AND title IN ('공지사항', '자유게시판')) " +
        "       OR n.par_id IN (SELECT id FROM navi WHERE par_id=0 AND title IN ('공지사항', '자유게시판'))) " +
        "ORDER BY n.par_id, n.seqno", nativeQuery = true)
	List<iNaviSub> getPublicMenu();

	// 일반 사용자용: not4general='1' 항목 제외
	@Query(value =
        "SELECT n.id, n.par_id, n.title, n.path, n.component, n.seqno, n.not4general, " +
        "  (SELECT GROUP_CONCAT(DISTINCT rp2.pid SEPARATOR ', ') " +
        "   FROM _role_permissions rp2 " +
        "   WHERE rp2.rid = :roleId AND rp2.church_id IN (0, :churchId) " +
        "     AND rp2.pid IN (CONCAT(n.id, '_R'), CONCAT(n.id, '_CRUD'))) AS pname " +
        "FROM navi n " +
        "WHERE n.active='1' " +
        "  AND (n.not4general IS NULL OR n.not4general = '0') " +
        "  AND n.id IN (" +
        "    SELECT CAST(SUBSTRING_INDEX(rp.pid, '_', 1) AS UNSIGNED) " +
        "    FROM _role_permissions rp WHERE rp.rid = :roleId AND rp.church_id IN (0, :churchId)" +
        "  ) " +
        "ORDER BY n.par_id, n.seqno", nativeQuery = true)
	List<iNaviSub> getMenu(@Param("roleId") int roleId, @Param("churchId") int churchId);

	// 총책임자용: not4general 제한 없음
	@Query(value =
        "SELECT n.id, n.par_id, n.title, n.path, n.component, n.seqno, n.not4general, " +
        "  (SELECT GROUP_CONCAT(DISTINCT rp2.pid SEPARATOR ', ') " +
        "   FROM _role_permissions rp2 " +
        "   WHERE rp2.rid = :roleId AND rp2.church_id = 0 " +
        "     AND rp2.pid IN (CONCAT(n.id, '_R'), CONCAT(n.id, '_CRUD'))) AS pname " +
        "FROM navi n " +
        "WHERE n.active='1' " +
        "  AND n.id IN (" +
        "    SELECT CAST(SUBSTRING_INDEX(rp.pid, '_', 1) AS UNSIGNED) " +
        "    FROM _role_permissions rp WHERE rp.rid = :roleId AND rp.church_id = 0" +
        "  ) " +
        "ORDER BY n.par_id, n.seqno", nativeQuery = true)
	List<iNaviSub> getMenuAdmin(@Param("roleId") int roleId);
}
