package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.Entity.Priority;

public interface PriorityRep extends JpaRepository<Priority, Integer> {

	List<Priority> findByChurchIdAndParIdOrderByGradeDesc(@Param("churchId") int churchId, @Param("parId") int parId);
	// id로 단건 조회
	Optional<Priority> findById(int id);
	// id로 삭제 (JpaRepository 상속 메소드 명시)
	void deleteById(int id);
	
	@Query(value="with recursive posTree as ("
			+ "select * from saint.lov where par_id=0 and name='공동체' and church_id=8 "
			+ "union all "
			+ "select l.* from saint.lov l inner join posTree p on l.par_id=p.id"
			+ ")"
			+ "select title,seqno from saint.position "
			+ "where polity_id in (select id from posTree) order by seqno",
		nativeQuery=true)
	int insertPositionFromLov(@Param("church_id") int a, @Param("name") String b);
}
