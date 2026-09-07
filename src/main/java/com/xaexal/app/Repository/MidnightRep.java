package com.xaexal.app.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.Midnight;

public interface MidnightRep extends JpaRepository<Midnight, Integer> {
}
