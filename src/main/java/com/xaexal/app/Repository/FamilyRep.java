package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.DTO.FamilyMember;
import com.xaexal.app.Entity.Family;
import com.xaexal.app.Entity.Member;

public interface FamilyRep extends JpaRepository<Family, Integer> {

	// ===== 부모/자녀/배우자 관계 (family 테이블 = 관계 테이블) =====

	// 부/모/배우자/자녀를 한번에 합쳐서 조회 (가족목록 화면용)
	// 순서: 부 -> 모 -> 배우자 -> 자녀(생년월일이 빠른 사람 먼저)
	@Query(value = "SELECT m.id AS id, m.name AS name, m.birthday AS birthday, "
			+ "m.gender AS gender, m.mobile AS mobile, "
			+ "CASE WHEN f.relation_type = '배우자' THEN '배우자' "
			+ "     WHEN f.member_id = :memberId THEN f.relation_type "
			+ "     ELSE '자녀' END AS relation "
			+ "FROM family f JOIN member m ON m.id = IF(f.member_id = :memberId, f.relative_id, f.member_id) "
			+ "WHERE f.member_id = :memberId OR f.relative_id = :memberId "
			+ "ORDER BY CASE WHEN f.relation_type = '배우자' THEN 2 "
			+ "              WHEN f.member_id = :memberId THEN "
			+ "                   CASE f.relation_type WHEN '부' THEN 0 WHEN '모' THEN 1 END "
			+ "              ELSE 3 END, "
			+ "m.birthday",
			nativeQuery = true)
	List<FamilyMember> findFamilyMembers(@Param("memberId") int memberId);

	// 부모 등록/변경 (자녀당 부/모 각 1건, 이미 있으면 교체)
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO family (member_id, relative_id, relation_type) "
			+ "VALUES (:childId, :parentId, :relationType) "
			+ "ON DUPLICATE KEY UPDATE relative_id = VALUES(relative_id)",
			nativeQuery = true)
	int registerParent(@Param("childId") int childId, @Param("parentId") int parentId,
			@Param("relationType") String relationType);

	// 배우자 등록/변경 (등록자 기준 1건, 이미 있으면 교체)
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO family (member_id, relative_id, relation_type) "
			+ "VALUES (:memberId, :spouseId, '배우자') "
			+ "ON DUPLICATE KEY UPDATE relative_id = VALUES(relative_id)",
			nativeQuery = true)
	int registerSpouse(@Param("memberId") int memberId, @Param("spouseId") int spouseId);

	// 부모 조회 (부/모)
	@Query(value = "SELECT m.* FROM family f JOIN member m ON m.id = f.relative_id "
			+ "WHERE f.member_id = :memberId AND f.relation_type IN ('부','모')",
			nativeQuery = true)
	List<Member> findParents(@Param("memberId") int memberId);

	// 부 또는 모 단건 조회 (relationType: '부' 또는 '모')
	@Query(value = "SELECT m.* FROM family f JOIN member m ON m.id = f.relative_id "
			+ "WHERE f.member_id = :memberId AND f.relation_type = :relationType",
			nativeQuery = true)
	Member findParentByType(@Param("memberId") int memberId, @Param("relationType") String relationType);

	// 자녀 조회 (부/모 관계의 역방향)
	@Query(value = "SELECT m.* FROM family f JOIN member m ON m.id = f.member_id "
			+ "WHERE f.relative_id = :memberId AND f.relation_type IN ('부','모')",
			nativeQuery = true)
	List<Member> findChildren(@Param("memberId") int memberId);

	// 배우자 조회 (등록 방향에 상관없이 검색)
	@Query(value = "SELECT m.* FROM family f "
			+ "JOIN member m ON m.id = IF(f.member_id = :memberId, f.relative_id, f.member_id) "
			+ "WHERE f.relation_type = '배우자' AND (f.member_id = :memberId OR f.relative_id = :memberId)",
			nativeQuery = true)
	List<Member> findSpouse(@Param("memberId") int memberId);

	// 부모 관계 삭제
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM family WHERE member_id = :childId AND relation_type = :relationType",
			nativeQuery = true)
	int deleteParent(@Param("childId") int childId, @Param("relationType") String relationType);

	// 배우자 관계 삭제 (등록 방향에 상관없이, 이 회원과 관련된 배우자 관계를 삭제)
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM family WHERE relation_type = '배우자' "
			+ "AND (member_id = :memberId OR relative_id = :memberId)",
			nativeQuery = true)
	int deleteSpouse(@Param("memberId") int memberId);

	// 이름 또는 모바일번호로 후보 검색 (본인 제외)
	@Query(value = "SELECT * FROM member WHERE id != :memberId "
			+ "AND (name LIKE CONCAT('%',:keyword,'%') OR mobile LIKE CONCAT('%',:keyword,'%')) "
			+ "ORDER BY birthday",
			nativeQuery = true)
	List<Member> findCandidateByKeyword(@Param("memberId") int memberId, @Param("keyword") String keyword);
}
