package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.Equipment;

public interface EquipmentRep extends JpaRepository<Equipment, Integer> {
    List<Equipment> findByChurchIdOrderByIdAsc(Integer churchId);
}
