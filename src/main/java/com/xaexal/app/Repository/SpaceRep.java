package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.Space;

public interface SpaceRep extends JpaRepository<Space, Integer> {
    List<Space> findByChurchIdOrderByIdAsc(Integer churchId);
}
