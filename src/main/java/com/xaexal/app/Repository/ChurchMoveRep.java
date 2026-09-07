package com.xaexal.app.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.ChurchMove;

public interface ChurchMoveRep extends JpaRepository<ChurchMove, Integer> {
}
