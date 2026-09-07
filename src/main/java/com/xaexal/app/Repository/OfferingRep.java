package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.OfferResult;
import com.xaexal.app.DTO.iOffer;
import com.xaexal.app.Entity.Offering;

public interface OfferingRep extends JpaRepository<Offering, Integer> {
	List<Offering> getByMobile(String mobile);
	void deleteByIncomeId(int incomeId);

	@Query(value =
			"WITH RECURSIVE offering_nodes AS ( " +
			"    SELECT id, par_id, name FROM budget WHERE church_id = :church_id AND name = '헌금' " +
			"    UNION ALL " +
			"    SELECT b.id, b.par_id, b.name FROM budget b JOIN offering_nodes o ON b.par_id = o.id " +
			") " +
			"SELECT a.name as name, b.offered as offered, SUM(b.total) as total " +
            "FROM (SELECT id, name FROM offering_nodes WHERE par_id IN (SELECT id FROM offering_nodes WHERE name = '헌금')) a " +
            "LEFT OUTER JOIN " +
            "     (SELECT i.type_id, i.issued as offered, SUM(i.amount) as total FROM income i " +
            "      WHERE i.church_id = :church_id AND i.issued BETWEEN :start_dt AND :end_dt " +
            "      GROUP BY i.type_id, i.issued) b " +
            "ON a.id = b.type_id " +
            "GROUP BY a.name, b.offered " +
            "ORDER BY a.name, b.offered", nativeQuery = true)
	List<OfferResult> getStatistic(@Param("church_id") int churchId,
                               	   @Param("start_dt") String startDt,
                               	   @Param("end_dt") String endDt);

	@Query(value="select o.id,i.id as income_id,i.church_id,c.name as church_name,i.type_id,b.name typename,i.amount,"+
				 "o.member_id,o.mobile,o.name,i.issued as offered,i.updated "+
				 " from income i left outer join offering o on o.income_id=i.id "+
				 " left outer join budget b on i.type_id=b.id "+
				 " left outer join church c on i.church_id=c.id "+
				 " where i.church_id=:churchId and i.issued between :startDt and :endDt "+
				 "   and i.type_id in (select bn.id from budget bn where bn.par_id in "+
				 "       (select id from budget where church_id=:churchId and name='헌금')) "+
				 " order by i.issued desc"
		,nativeQuery=true)
	List<iOffer> searchByChurchIdAndOfferedBetweenOrderByOfferedDesc(
			@Param("churchId") int churchId,
			@Param("startDt") String startDt,
			@Param("endDt") String endDt);

	@Query(value="select o.id,i.id as income_id,i.church_id,c.name as church_name,i.type_id,b.name typename,i.amount,"+
				 "o.member_id,o.mobile,o.name,i.issued as offered,i.updated "+
				 " from offering o left outer join income i on o.income_id=i.id "+
				 " left outer join budget b on i.type_id=b.id "+
				 " left outer join church c on i.church_id=c.id "+
				 " where o.member_id=:memberId and i.issued between :startDt and :endDt "+
				 " order by i.issued desc"
		,nativeQuery=true)
	List<iOffer> searchByMemberIdAndOfferedBetween(
			@Param("memberId") int memberId,
			@Param("startDt") String startDt,
			@Param("endDt") String endDt);
}
