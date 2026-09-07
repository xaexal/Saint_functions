package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.Attached;

public interface AttachedRep extends JpaRepository<Attached, Integer> {
	Optional<Attached> findById(int id);
	Optional<Attached> findByNewName(String newName);
	List<Attached> findByBoardIdOrderBySeqno(int board_id);
	List<Attached> findByBoardIdAndSourceOrderBySeqno(int boardId, String source);
}
