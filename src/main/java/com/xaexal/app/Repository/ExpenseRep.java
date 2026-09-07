package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.iExpense;
import com.xaexal.app.Entity.Expense;

public interface ExpenseRep extends JpaRepository<Expense, Integer> {

	@Query(value = "SELECT a.id,a.church_id,a.type_id,b.name typename,a.amount," +
				 "a.issued,a.remark,a.updated " +
				 " FROM expense a LEFT OUTER JOIN budget b ON a.type_id=b.id " +
				 " WHERE a.church_id=:churchId AND a.issued BETWEEN :startDt AND :endDt " +
				 " ORDER BY a.issued DESC"
			, nativeQuery = true)
	List<iExpense> searchByChurchIdAndIssuedBetween(@Param("churchId") int churchId,
			@Param("startDt") String startDt, @Param("endDt") String endDt);
}
