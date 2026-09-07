package com.xaexal.app.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.BoardAux;

import jakarta.transaction.Transactional;

public interface BoardAuxRep extends JpaRepository<BoardAux, Integer> {
    Optional<BoardAux> findByBoardId(Integer boardId);

    @Transactional
    void deleteByBoardId(Integer boardId);

    @Transactional
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query(
        value="UPDATE board_aux SET title = :title, type = :type, urgent = :urgent WHERE board_id = :boardId",
        nativeQuery=true)
    int updateByBoardId(@org.springframework.data.repository.query.Param("boardId") int boardId,
                        @org.springframework.data.repository.query.Param("title") String title,
                        @org.springframework.data.repository.query.Param("type") int type,
                        @org.springframework.data.repository.query.Param("urgent") boolean urgent);
}
