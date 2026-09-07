package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.StaffMember;
import com.xaexal.app.Entity.Staff;

public interface StaffRep extends JpaRepository<Staff, Integer> {
	@Query(value="select * from staff f join member m on f.member_id=m.id "+
			"where f.member_id=:member_id",nativeQuery=true)
	List<StaffMember> searchByMemberId(@Param("member_id") int memberId);

	@Query(value="select f.id,f.pstn_id,p.title pstn_name,f.started,f.retired,f.active,"+
			"f.member_id,m.name member_name,m.birthday,m.mobile,m.gender "+
			" from staff f join member m on f.member_id=m.id "+
			" join position p on f.pstn_id=p.id "+
			 "where f.pstn_id=:pstn_id",nativeQuery=true)
	List<StaffMember> searchByPstnId(@Param("pstn_id") int a);

	// 하위부서의 내역까지 보면 혼란스러울 수 있음
//		"with recursive Tree as ("+
//		"select id from lov where id=:polity_id "+
//		"union all "+
//		"select l.id from lov l inner join Tree t on l.par_id=t.id) "+
	@Query(value=
		"select f.id,f.pstn_id,p.title pstn_name,f.started,f.retired,f.active,"+
			"f.member_id,m.name member_name,m.birthday,m.mobile,m.gender "+
			" from position p inner join staff f on p.id=f.pstn_id "+
			" left outer join member m on f.member_id=m.id "+
			"where p.polity_id=:polity_id order by p.seqno",
//			"where p.polity_id in (select id from Tree)",
			nativeQuery=true)
	List<StaffMember> searchByPolityId(@Param("polity_id") int a);

	@Query(value=
		"select p.id,p.name polity_name,p1.id pstn_id,p1.title pstn_name,f.id staff_id,m.name"
		+ "from polity p left outer join position p1 on p.id=p1.polity_id"
		+ "  			 left outer join staff f on p1.id=f.pstn_id"
		+ "  			 join member m on f.member_id=m.id"
		+ " where p.church_id=:church_id order by f.pstn_id",
	nativeQuery=true)
	List<StaffMember> searchByChurchId(@Param("church_id") int churchId);

	@Query(value=
		"SELECT DISTINCT p.polity_id " +
		"FROM staff s " +
		"JOIN position p ON s.pstn_id = p.id " +
		"WHERE s.member_id = :member_id",
		nativeQuery=true)
	List<Integer> findPolityIdsByMemberId(@Param("member_id") int memberId);

	int countByPstnId(int pstnId);
}
