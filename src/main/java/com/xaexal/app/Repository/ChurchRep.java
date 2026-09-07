package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.Entity.Church;

public interface ChurchRep extends JpaRepository<Church, Integer> {
	List<Church> findAll();
	Church findById(int id);
	List<Church> findByNameLike(String name);

	@Query(value = "SELECT COUNT(*) FROM church WHERE creator = :#{#c.creator} " +
            "AND STR_TO_DATE(created, '%Y-%m-%d %H:%i:%s') > DATE_SUB(NOW(), INTERVAL 6 MONTH)",
    nativeQuery = true)
	int checkRecentRegistration(@Param("c") Church church);

	@Query(value = "SELECT * FROM church "
			+ "WHERE created <= DATE_SUB(NOW(), INTERVAL 7 DAY) "
			+ "AND NOT EXISTS (SELECT 1 FROM saint WHERE saint.church_id = church.id)",
			nativeQuery = true)
	List<Church> findInactiveChurches();
}
