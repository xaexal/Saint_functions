package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.DTO.iOrgChart;
import com.xaexal.app.Entity.Lov;

public interface LovRep extends JpaRepository<Lov, Integer> {
	Lov findById(int id);
	List<Lov> findByParId(int par_id);

	@Query(value = "SELECT COALESCE(MAX(seqno), 0) FROM lov WHERE par_id = :parId", nativeQuery = true)
    Integer findMaxSeqnoByParId(@Param("parId") int parId);
	List<Lov> findByNameAndChurchId(String name, int churchId);
	Optional<Lov> findByChurchIdAndName(int churchId, String name);
    int countByParId(int parId);
    List<Lov> findByParIdAndChurchId(int parId, int churchId);
	List<Lov> findByChurchIdAndVisible(int churchId, String visible);

	@Modifying
    @Transactional
	@Query(value="update lov set par_id=:parId,seqno=:seqno where id=:id",nativeQuery=true)
    int updateParIdAndSeqnoById(@Param("parId") int parId,@Param("seqno") int seqno,@Param("id") int id);

	@Modifying
    @Transactional
    @Query(value = "INSERT INTO lov (par_id, church_id, name, remark, seqno, visible, writer) " +
                   "SELECT :par_id, :church_id, name, remark, seqno, visible, writer " +
                   "FROM lov WHERE par_id = (SELECT id FROM lov " +
                   "                         WHERE church_id = 0 AND name = :name and par_id=0)",
           nativeQuery = true)
    int createChildren(@Param("church_id") int churchId,
                    @Param("name") String name,
                    @Param("par_id") int parId);


	// 특정 부모 이름과 church_id, par_id 조건을 조합해 하위 목록 조회
    @Query(value = "SELECT * FROM lov " +
                   "WHERE par_id = (SELECT id FROM lov WHERE name = :name " +
                   "                AND church_id = :churchId AND par_id = :parId LIMIT 1) " +
                   "ORDER BY seqno", nativeQuery = true)
    List<Lov> searchChildrenByNameAndChurchIdAndParId(@Param("name") String name,
                                        @Param("churchId") int churchId,
                                        @Param("parId") int parId);

    // 소속교단처럼 church_id 조건이 없는 전역 코드 조회용
    @Query(value = "SELECT * FROM lov " +
                   "WHERE par_id = (SELECT id FROM lov WHERE name = :parentName LIMIT 1) " +
                   "ORDER BY name", nativeQuery = true)
    List<Lov> findByParentName(@Param("parentName") String parentName);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lov SET seqno = seqno + 1 " +
                   "WHERE par_id = :par_id " +
                   "AND seqno BETWEEN :newSeq AND :oldSeq - 1",
           nativeQuery = true)
    int updatePlus(@Param("par_id") int x,@Param("newSeq") int a,@Param("oldSeq") int b);

    @Modifying
    @Transactional
    @Query(value = "update lov set seqno=seqno-1 where par_id=:par_id "+
    				"and seqno between :old+1 and :new",nativeQuery=true)
	int updateMinus(@Param("par_id") int x,@Param("new") int a,@Param("old") int b);

    @Modifying
    @Transactional
    @Query(value="update lov set seqno=seqno+1 where par_id=:par_id and seqno>:seqno",nativeQuery=true)
    int updatePlusAll(@Param("par_id") int x,@Param("seqno") int y);

    @Query(value=
    "WITH RECURSIVE lovStep as ("+
    " select * from lov where church_id=:church_id and name like concat('%',:name,'%') and par_id=0 "+
    " union all "+
    " select l.* from lov l join lovStep s on l.par_id=s.id"+
    ") select * from lovStep order by seqno",nativeQuery=true)
    List<Lov> searchByChurchIdAndNameOrderBySeqno(@Param("church_id") int churchId,
    		@Param("name") String name);


	@Query(value =
	    "WITH RECURSIVE lovTree AS ( " +
	    "  SELECT * FROM lov " +
	    "  WHERE church_id = :churchId AND name = :name AND par_id = 0 " +
	    "  UNION ALL " +
	    "  SELECT l.* FROM lov l " +
	    "  JOIN lovTree lt ON l.par_id = lt.id " +
	    ") " +
	    "SELECT * FROM lovTree",
	    nativeQuery = true)
	List<Lov> findSelfAndAllDescendants(@Param("churchId") int churchId, @Param("name") String name);

	@Query(value =
		    "WITH RECURSIVE LT AS ( " +
		    "    SELECT id, par_id, name, 0 AS pos, CAST(id AS CHAR(200)) AS route " +
		    "    FROM lov " +
		    "    WHERE par_id = 0 AND church_id = :churchID AND name LIKE CONCAT('%', :name, '%') " +
		    "    UNION ALL " +
		    "    SELECT c.id, c.par_id, c.name, r.pos + 1, CONCAT(r.route, '-', c.id) " +
		    "    FROM lov c " +
		    "    JOIN LT r ON c.par_id = r.id " +
		    "    WHERE c.visible = '1' " +
		    "), " +
		    "CombinedData AS ( " +
		    "    SELECT " +
		    "        CONCAT('D', id) AS node_id, " +
		    "        CASE WHEN par_id = 0 THEN NULL ELSE CONCAT('D', par_id) END AS parent_id, " +
		    "        name AS node_name, " +
		    "        :name AS node_position, " +
		    "        route, 0 AS sort_order " +
		    "    FROM LT " +
		    "    UNION ALL " +
		    "    SELECT " +
		    "        CONCAT('S', s.id) AS node_id, " +
		    "        CONCAT('D', LT.id) AS parent_id, " +
		    "        m.name AS node_name, " +
		    "        p.title AS node_position, " +
		    "        LT.route, 1 AS sort_order " +
		    "    FROM LT " +
		    "    JOIN position p ON LT.id = p.polity_id " +
		    "    JOIN staff s ON p.id = s.pstn_id " +
		    "    JOIN member m ON s.member_id = m.id " +
		    ") " +
		    "SELECT node_id AS id, parent_id AS parentId, node_name AS name, node_position AS position " +
		    "FROM CombinedData " +
		    "ORDER BY route, sort_order, node_id",
		    nativeQuery = true)
	List<iOrgChart> getTree(@Param("churchID") int churchID, @Param("name") String name);
}
