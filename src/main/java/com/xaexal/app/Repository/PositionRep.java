package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.DTO.iPosition;
import com.xaexal.app.Entity.Position;

public interface PositionRep extends JpaRepository<Position, Integer> {
	@Query(value=
		"with recursive lovTree as ("+
		"select id,name from lov where church_id=:church_id and name='부서' "+
		" union all "+
		"select l.id,l.name from lov l inner join lovTree lt on lt.id=l.par_id) "+
		"select p.id,p.title,p.seqno,0 "+
		"  from lovTree t join position p on t.id=p.polity_id "+
		" order by p.title",
		nativeQuery=true)
	List<iPosition> searchByChurchId(@Param("church_id") int a);

//		하위부서의 내역까지 보면 혼란스러울 수 있음
//		"with recursive Tree as ("+
//		"select id from lov where id=:polity_id "+
//		"union all "+
//		"select l.id from lov l inner join Tree t on l.par_id=t.id) "+
	Optional<Position> findById(int id);
	@Query(value=
		"select p.id, p.polity_id, p.title, p.seqno, count(s.id) as cnt from position p "+
		"left join staff s on p.id = s.pstn_id "+
		"where p.polity_id=:polity_id group by p.id, p.title"
		,nativeQuery=true)
	List<iPosition> searchByPolityId(@Param("polity_id") int a);

	@Query(value="SELECT COALESCE(MAX(p.seqno), 0) FROM position p WHERE p.polity_id = :polity_id"
		,nativeQuery=true)
	Integer searchMaxSeqnoByPolityId(@Param("polity_id") int a);

	@Modifying
	@Transactional
	@Query(value="update position set seqno=seqno+1 "
			+     "where polity_id=:polity and seqno between :new and :old-1",nativeQuery=true)
	int updatePlus(@Param("polity") int x,@Param("new") int a,@Param("old") int b);

	@Modifying
	@Transactional
	@Query(value="update position set seqno=seqno-1 "
			+     "where polity_id=:polity and seqno between :old+1 and :new",nativeQuery=true)
	int updateMinus(@Param("polity") int x,@Param("new") int a,@Param("old") int b);


	// parId의 직계 자식 및 모든 하위 자손 id를 콤마 구분 문자열로 반환
	@Query(value =
		"WITH RECURSIVE lovStep AS ( " +
		"    SELECT id FROM lov WHERE par_id = :id " +
		"    UNION ALL " +
		"    SELECT l.id FROM lov l JOIN lovStep s ON l.par_id = s.id " +
		") select count(*) from position where polity_id in (SELECT id FROM lovStep)",
		nativeQuery = true)
	int countByPolityId(@Param("id") int id);
}
