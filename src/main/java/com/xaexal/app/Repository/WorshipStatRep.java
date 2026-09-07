package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.WorshipStat;

public interface WorshipStatRep extends JpaRepository<WorshipStat, Integer> {
    List<WorshipStat> findByServed(String served);
    void deleteByServedAndWorshipIdIn(String served, List<Integer> worshipIds);
    void deleteByServedAndWorshipId(String served, Integer worshipId);
    List<WorshipStat> findByServedBetweenAndWorshipIdIn(String start, String end, List<Integer> worshipIds);
}
