package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.Entity.Applicant;

public interface ApplicantRep extends JpaRepository<Applicant, Integer> {
	boolean existsByChurchIdAndMemberIdAndPriorityId(@Param("churchId") int churchId, @Param("memberId") int memberId, @Param("priorityId") int priorityId);
	Applicant findByChurchIdAndMemberIdAndPriorityId(@Param("churchId") int churchId, @Param("memberId") int memberId, @Param("priorityId") int priorityId);
	List<Applicant> findByMemberId(int memberId);
	List<Applicant> findByMemberIdAndChurchId(@Param("memberId") int memberId, @Param("churchId") int churchId);
	void deleteByMemberId(int memberId);
}
