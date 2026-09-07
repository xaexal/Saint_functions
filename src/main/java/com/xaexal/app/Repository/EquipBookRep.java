package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.Entity.EquipBook;

public interface EquipBookRep extends JpaRepository<EquipBook, Integer> {
    List<EquipBook> findByApplierOrderByIdDesc(Integer applier);

    @Query("SELECT eb FROM EquipBook eb, Equipment e WHERE eb.equipmentId = e.id AND e.churchId = :churchId ORDER BY eb.id DESC")
    List<EquipBook> findByChurchId(@Param("churchId") Integer churchId);
}
