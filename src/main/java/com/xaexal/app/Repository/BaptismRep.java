package com.xaexal.app.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.Baptism;

public interface BaptismRep extends JpaRepository<Baptism, Integer> {
	Optional<Baptism> findById(Integer id);
	Baptism findFirstByMemberId(Integer member_id);
}
