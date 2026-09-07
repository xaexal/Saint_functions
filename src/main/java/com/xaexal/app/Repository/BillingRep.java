package com.xaexal.app.Repository;

import com.xaexal.app.Entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRep extends JpaRepository<Billing, Integer> {
    Billing findByMemberIdAndActive(Integer memberId, String active);
}
