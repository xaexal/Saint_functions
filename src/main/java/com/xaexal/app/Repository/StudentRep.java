package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.StudentMember;
import com.xaexal.app.Entity.Student;

public interface StudentRep extends JpaRepository<Student, Integer> {
	@Query(value = "SELECT s.id, s.school_id,s.member_id, s.confirmed, s.graduated, s.status, " +
            "       m.name as memberName, m.mobile, m.birthday, m.gender " +
            "FROM student s LEFT OUTER JOIN member m ON s.member_id = m.id " +
            "WHERE s.school_id = :school_id " +
            "ORDER BY m.name DESC",
    nativeQuery = true)
	List<StudentMember> getStudentList(@Param("school_id") int schoolId);

	@Query(value =
		      "SELECT COUNT(*) FROM student s " +
		      "WHERE s.member_id = :member_id AND s.school_id != :school_id " +
		      "AND s.school_id IN (" +
		      "    SELECT id FROM school WHERE type_id IN (" +
		      "        WITH recursive typeTree AS (" +
		      "            SELECT par_id AS type_id FROM lov " +
		      "            WHERE id = (SELECT type_id FROM school WHERE id = :school_id) " +
		      "            UNION ALL " +
		      "            SELECT l.id FROM lov l " +
		      "            INNER JOIN typeTree t ON l.par_id = t.type_id" +
		      "        ) " +
		      "        SELECT type_id FROM typeTree" +
		      "    )" +
		      ")", nativeQuery = true)
	int checkDuplicate(@Param("school_id") int schoolId, @Param("member_id") int memberId);

	Student findBySchoolIdAndMemberId(int schoolId,int MemberId);
}
