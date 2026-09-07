package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.Schedule;

public interface ScheduleRep extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByChurchIdOrderByIdAsc(Integer churchId);
}
