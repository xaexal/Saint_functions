package com.xaexal.app.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.BoardType;

public interface BoardTypeRep extends JpaRepository<BoardType, Integer> {
	BoardType findById(int Id);
}
