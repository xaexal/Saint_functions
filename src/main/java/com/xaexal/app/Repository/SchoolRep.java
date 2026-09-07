package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.SchoolResult;
import com.xaexal.app.DTO.SchoolStudentResult;
import com.xaexal.app.Entity.School;

public interface SchoolRep extends JpaRepository<School, Integer> {
	Optional<School> findById(Integer id);

	@Query(value = "WITH RECURSIVE lov_tree AS (" +
            "    SELECT id, name, par_id, church_id " +
            "    FROM lov " +
            "    WHERE id = :type_id " +
            "    UNION ALL " +
            "    SELECT l.id, l.name, l.par_id, l.church_id " +
            "    FROM lov l " +
            "    INNER JOIN lov_tree th ON l.par_id = th.id " +
            ") " +
            "SELECT s.*, th.name as typeName " +
            "FROM lov_tree th JOIN school s ON th.id = s.type_id " +
            "ORDER BY th.id ASC, s.created DESC",
    nativeQuery = true)
	List<SchoolResult> getSchoolList(@Param("type_id") int typeId);

	@Query(value = "WITH RECURSIVE NewclassTree AS (" +
            "    SELECT id, par_id, name FROM lov " +
            "    WHERE id IN (SELECT id FROM lov WHERE church_id = :church_id AND name = '새신자등록과정') " +
            "    UNION ALL " +
            "    SELECT l.id, l.par_id, l.name FROM lov l " +
            "    INNER JOIN NewclassTree nt ON l.par_id = nt.id " +
            ") " +
            "SELECT s.*, t.id as studentId, nt.name as typeName,t.status,t.graduated,t.confirmed " +
            "FROM NewclassTree nt " +
            "JOIN school s ON nt.id = s.type_id " +
            "LEFT OUTER JOIN student t ON s.id = t.school_id AND t.member_id = :member_id " +
            "ORDER BY nt.id ASC, s.created DESC",
	    nativeQuery = true)
	List<SchoolStudentResult> getSchoolListForStudent(@Param("church_id") int churchId,
												 	  @Param("member_id") int memberId
													);
	@Query(value="SELECT s FROM School s " +
	           "WHERE s.typeId IN (SELECT l.id FROM Lov l " +
	           "                   WHERE l.churchId = :churchId " +
	           "                   AND l.name = '새신자등록과정')",nativeQuery=true)
	    List<School> getNewclassList(@Param("churchId") int churchId);
}
