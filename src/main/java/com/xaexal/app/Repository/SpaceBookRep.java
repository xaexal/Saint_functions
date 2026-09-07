package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.Entity.SpaceBook;

public interface SpaceBookRep extends JpaRepository<SpaceBook, Integer> {
    List<SpaceBook> findByApplierOrderByIdDesc(Integer applier);

    @Query("SELECT sb FROM SpaceBook sb, Space s WHERE sb.spaceId = s.id AND s.churchId = :churchId ORDER BY sb.id DESC")
    List<SpaceBook> findByChurchId(@Param("churchId") Integer churchId);
}
