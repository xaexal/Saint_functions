package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.Entity.Bulletin;

public interface BulletinRep extends JpaRepository<Bulletin, Integer> {
    List<Bulletin> findByChurchIdOrderByIdDesc(Integer churchId);

    @Modifying
    @Transactional
    @Query("UPDATE Bulletin b SET b.hit = b.hit + 1 WHERE b.id = :id")
    void addHit(@Param("id") Integer id);
}
