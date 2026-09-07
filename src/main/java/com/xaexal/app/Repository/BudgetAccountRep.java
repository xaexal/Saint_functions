package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.DTO.iBudgetOverview;
import com.xaexal.app.Entity.BudgetAccount;

public interface BudgetAccountRep extends JpaRepository<BudgetAccount, Integer> {
	BudgetAccount findOneById(int id);
	List<BudgetAccount> findByParId(int parId);

	@Modifying
	@Transactional
	@Query(value = "update budget set par_id=:parId,seqno=:seqno where id=:id", nativeQuery = true)
	int updateParIdAndSeqnoById(@Param("parId") int parId, @Param("seqno") int seqno, @Param("id") int id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE budget SET seqno = seqno + 1 " +
			"WHERE par_id = :par_id AND seqno BETWEEN :newSeq AND :oldSeq - 1", nativeQuery = true)
	int updatePlus(@Param("par_id") int x, @Param("newSeq") int a, @Param("oldSeq") int b);

	@Modifying
	@Transactional
	@Query(value = "update budget set seqno=seqno-1 where par_id=:par_id " +
			"and seqno between :old+1 and :new", nativeQuery = true)
	int updateMinus(@Param("par_id") int x, @Param("new") int a, @Param("old") int b);

	@Modifying
	@Transactional
	@Query(value = "update budget set seqno=seqno+1 where par_id=:par_id and seqno>:seqno", nativeQuery = true)
	int updatePlusAll(@Param("par_id") int x, @Param("seqno") int y);

	// 특정 부모 이름과 church_id, par_id 조건을 조합해 하위 목록 조회 (드롭다운용)
	@Query(value = "SELECT * FROM budget " +
			"WHERE par_id = (SELECT id FROM budget WHERE name = :name " +
			"                AND church_id = :churchId AND par_id = :parId LIMIT 1) " +
			"ORDER BY seqno", nativeQuery = true)
	List<BudgetAccount> searchChildrenByNameAndChurchIdAndParId(@Param("name") String name,
			@Param("churchId") int churchId, @Param("parId") int parId);

	@Query(value =
			"WITH RECURSIVE step AS (" +
			" SELECT * FROM budget WHERE church_id=:church_id AND name LIKE CONCAT('%',:name,'%') AND par_id=0 " +
			" UNION ALL " +
			" SELECT b.* FROM budget b JOIN step s ON b.par_id = s.id" +
			") SELECT * FROM step ORDER BY seqno", nativeQuery = true)
	List<BudgetAccount> searchByChurchIdAndNameOrderBySeqno(@Param("church_id") int churchId,
			@Param("name") String name);

	// 가상루트 모드: name 필터 없이 church_id의 전체 계층 반환
	@Query(value =
			"WITH RECURSIVE step AS (" +
			" SELECT * FROM budget WHERE church_id=:church_id AND par_id=0 " +
			" UNION ALL " +
			" SELECT b.* FROM budget b JOIN step s ON b.par_id = s.id" +
			") SELECT * FROM step ORDER BY seqno", nativeQuery = true)
	List<BudgetAccount> findAllByChurchIdOrderBySeqno(@Param("church_id") int churchId);

	// 상위과목(id)의 시작일/종료일이 바뀌었을 때, 모든 하위과목(n단계 전체)에 필드 단위로 채운다.
	// 하위과목에 이미 값이 있는 필드는 그대로 두고, 비어있는 필드만 상위과목의 값으로 채운다.
	@Modifying
	@Transactional
	@Query(value =
			"WITH RECURSIVE descendants AS (" +
			" SELECT id FROM budget WHERE par_id = :id " +
			" UNION ALL " +
			" SELECT c.id FROM budget c JOIN descendants d ON c.par_id = d.id" +
			") " +
			"UPDATE budget b JOIN descendants d ON b.id = d.id " +
			"SET b.start_date = CASE WHEN b.start_date IS NULL THEN :startDate ELSE b.start_date END, " +
			"    b.end_date = CASE WHEN b.end_date IS NULL THEN :endDate ELSE b.end_date END",
			nativeQuery = true)
	void cascadeFillDates(@Param("id") int id, @Param("startDate") String startDate, @Param("endDate") String endDate);

	// 특정 이름의 레코드 하위 자식들의 seqno를 생성순서 기준으로 10,20,30... 재배정
	@Modifying
	@Transactional
	@Query(value =
		"UPDATE budget ba " +
		"JOIN ( " +
		"  SELECT id, ROW_NUMBER() OVER (ORDER BY id) * 10 AS new_seqno " +
		"  FROM budget " +
		"  WHERE par_id = (SELECT id FROM budget WHERE name = :name LIMIT 1) " +
		") ranked ON ba.id = ranked.id " +
		"SET ba.seqno = ranked.new_seqno",
		nativeQuery = true)
	int resequenceChildrenByParentName(@Param("name") String name);

	// 특정 부모 하위의 최대 seqno 조회 (신규 추가 시 seqno 자동 배정용)
	@Query(value = "SELECT COALESCE(MAX(seqno), 0) FROM budget WHERE par_id = :parId", nativeQuery = true)
	int maxSeqnoByParId(@Param("parId") int parId);

	// 하위항목 amount 합계 (금액 초과 검증용)
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM budget WHERE par_id = :parId", nativeQuery = true)
	java.math.BigDecimal sumAmountByParId(@Param("parId") int parId);

	// 형제항목 amount 합계 - 자기 자신 제외 (상위항목 금액 초과 검증용)
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM budget WHERE par_id = :parId AND id <> :excludeId", nativeQuery = true)
	java.math.BigDecimal sumAmountByParIdExcluding(@Param("parId") int parId, @Param("excludeId") int excludeId);

	// 예산과목별 배정액(budget.amount)과 집행액(expense+income 합계) — 재귀 CTE로 트리 순서 + depth 반환
	@Query(value =
			"WITH RECURSIVE tree AS (" +
			" SELECT id, par_id, name, amount, COALESCE(seqno,0) seqno, 0 depth," +
			"        CAST(LPAD(COALESCE(seqno,0),6,'0') AS CHAR(1000)) sort_key" +
			" FROM budget WHERE par_id=0 AND church_id=:churchId" +
			" UNION ALL" +
			" SELECT b.id, b.par_id, b.name, b.amount, COALESCE(b.seqno,0)," +
			"        t.depth+1, CONCAT(t.sort_key,'.',LPAD(COALESCE(b.seqno,0),6,'0'))" +
			" FROM budget b JOIN tree t ON b.par_id=t.id WHERE b.church_id=:churchId" +
			")" +
			"SELECT t.id typeId, t.par_id parId, t.depth, t.name typename," +
			"       COALESCE(t.amount,0) budgetAmount, COALESCE(e.spent,0) spentAmount" +
			" FROM tree t" +
			" LEFT OUTER JOIN (" +
			"   SELECT type_id, SUM(spent) spent FROM (" +
			"     SELECT type_id, SUM(amount) spent FROM expense" +
			"     WHERE church_id=:churchId AND issued BETWEEN :startFyear AND :endFyear GROUP BY type_id" +
			"     UNION ALL" +
			"     SELECT type_id, SUM(amount) FROM income" +
			"     WHERE church_id=:churchId AND issued BETWEEN :startFyear AND :endFyear GROUP BY type_id" +
			"   ) combined GROUP BY type_id" +
			" ) e ON e.type_id=t.id" +
			" WHERE t.par_id!=0" +
			" ORDER BY t.sort_key",
			nativeQuery = true)
	List<iBudgetOverview> getOverview(@Param("churchId") int churchId,
			@Param("startFyear") String startFyear, @Param("endFyear") String endFyear);

	// 자식이 없는 최말단 과목 목록 (지출입력 드롭다운용)
	@Query(value = "SELECT * FROM budget b WHERE b.church_id = :churchId AND b.par_id != 0 " +
			"AND NOT EXISTS (SELECT 1 FROM budget c WHERE c.par_id = b.id) ORDER BY b.seqno", nativeQuery = true)
	List<BudgetAccount> findLeafNodesByChurchId(@Param("churchId") int churchId);

	// 최말단 과목 + 전체 경로 (예: 2026년 > 지출 > 급여 > 교역자급여) — react-select용
	@Query(value =
		"WITH RECURSIVE tree AS (" +
		"  SELECT id, par_id, name, CAST(name AS CHAR(500)) AS full_path" +
		"  FROM budget WHERE par_id = 0 AND church_id = :churchId" +
		"  UNION ALL" +
		"  SELECT b.id, b.par_id, b.name, CONCAT(t.full_path, ' > ', b.name)" +
		"  FROM budget b JOIN tree t ON b.par_id = t.id" +
		"  WHERE b.church_id = :churchId" +
		")" +
		"SELECT id, name, full_path AS path FROM tree" +
		" WHERE id NOT IN (SELECT DISTINCT par_id FROM budget WHERE church_id = :churchId AND par_id != 0)" +
		" ORDER BY full_path",
		nativeQuery = true)
	List<com.xaexal.app.DTO.iLeafBudget> findLeafNodesWithPathByChurchId(@Param("churchId") int churchId);

	// 이 과목(및 하위과목)이 expense에서 실제로 쓰이고 있는지 확인 (삭제 차단용)
	@Query(value =
			"WITH RECURSIVE step AS (" +
			" SELECT :id AS id " +
			" UNION ALL " +
			" SELECT b.id FROM budget b JOIN step s ON b.par_id = s.id" +
			") SELECT (SELECT COUNT(*) FROM expense WHERE type_id IN (SELECT id FROM step))",
			nativeQuery = true)
	int countUsageByAccountId(@Param("id") int id);
}
