package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.DTO.SaintMember;
import com.xaexal.app.Entity.Member;

public interface MemberRep extends JpaRepository<Member, Integer> {
	Optional<Member> findById(Integer id);
	List<Member> findAll();
	List<Member> findByNameLike(String name);
	List<Member> findByNameLikeOrderByBirthday(String name);

	List<Member> findByNameLikeAndBirthdayAndGender(@Param("name") String a,
			@Param("birthday") String b, @Param("gender") String c);

	// name/birthday/gender/mobile 중 null이 아닌(=입력된) 항목만으로 일치하는 member를 찾는다
	@Query(value = "SELECT * FROM member WHERE " +
			"(:name IS NULL OR name = :name) AND " +
			"(:birthday IS NULL OR birthday = :birthday) AND " +
			"(:gender IS NULL OR gender = :gender) AND " +
			"(:mobile IS NULL OR mobile = :mobile)",
			nativeQuery = true)
	List<Member> findMatching(@Param("name") String name, @Param("birthday") String birthday,
			@Param("gender") String gender, @Param("mobile") String mobile);

	@Query(value="select m.id member_id,s.id saint_id,m.name member_name,"+
			"c.id church_id,c.name church_name,m.mobile,"+
			"ifnull(coalesce(r.rid,rg.rid),0) role_id,ifnull(coalesce(r.rname,rg.rname),'') role_name "+
			"from member m left outer join saint s on m.id=s.member_id and s.active='1'"+
						"left join church c on s.church_id=c.id "+
						"left join _roles r on s.role=r.id and s.church_id=r.church_id "+
						"left join _roles rg on s.role=rg.id and rg.church_id=0 "+
			"where m.mobile=:mobile and m.passcode=:passcode",
			nativeQuery=true)
	SaintMember findByMobileAndPasscode(@Param("mobile") String mobile,@Param("passcode") String passcode);

	@Query(value="select m.id member_id,s.id saint_id,m.name member_name,"+
			"c.id church_id,c.name church_name,m.mobile,"+
			"ifnull(coalesce(r.rid,rg.rid),0) role_id,ifnull(coalesce(r.rname,rg.rname),'') role_name "+
			"from member m left outer join saint s on m.id=s.member_id and s.active='1'"+
						"left join church c on s.church_id=c.id "+
						"left join _roles r on s.role=r.id and s.church_id=r.church_id "+
						"left join _roles rg on s.role=rg.id and rg.church_id=0 "+
			"where m.email=:email",
			nativeQuery=true)
	SaintMember searchByEmail(@Param("email") String email);

	Member findByMobile(String mobile);

	@Query(value=
		"select a.* from member a left outer join saint b "+
			"on a.name like concat('%',:name,'%') and a.id=b.member_id "+
		" order by a.name",nativeQuery=true
	)
	List<Member> findByChurchIdAndMemberNameLike(@Param("church_id") int a,
												 @Param("name") String name);
	@Modifying
	@Transactional
	@Query(value = "UPDATE member SET login_tm = CURRENT_TIMESTAMP WHERE id = :id",
		   nativeQuery = true)
	void setLogin(@Param("id") int id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE member SET logout_tm = CURRENT_TIMESTAMP WHERE id = :id",
		   nativeQuery = true)
	void setLogout(@Param("id") int id);

	@Query(value="select m.id member_id,s.id saint_id,m.name member_name,"+
			"c.id church_id,c.name church_name,m.mobile,m.login_tm,"+
			"ifnull(coalesce(r.rid,rg.rid),0) role_id,ifnull(coalesce(r.rname,rg.rname),'') role_name "+
			"from member m left outer join saint s on m.id=s.member_id and s.active='1'"+
						"left join church c on s.church_id=c.id "+
						"left join _roles r on s.role=r.id and s.church_id=r.church_id "+
						"left join _roles rg on s.role=rg.id and rg.church_id=0 "+
			"where m.remember_token=:token",
			nativeQuery=true)
	SaintMember findByRememberToken(@Param("token") String token);

	@Modifying
	@Transactional
	@Query(value = "UPDATE member SET remember_token = :token WHERE id = :id", nativeQuery = true)
	void updateRememberToken(@Param("id") int id, @Param("token") String token);

	@Modifying
	@Transactional
	@Query(value = "UPDATE member SET remember_token = NULL WHERE id = :id", nativeQuery = true)
	void clearRememberToken(@Param("id") int id);
}
