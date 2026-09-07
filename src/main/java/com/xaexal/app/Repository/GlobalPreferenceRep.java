package com.xaexal.app.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xaexal.app.Entity.GlobalPreference;

public interface GlobalPreferenceRep extends JpaRepository<GlobalPreference, Integer> {
}
