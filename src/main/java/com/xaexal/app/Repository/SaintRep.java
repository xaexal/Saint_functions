package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.DailyStatResult;
import com.xaexal.app.DTO.MemberBaptismSaint;
import com.xaexal.app.DTO.MonthStatResult;
import com.xaexal.app.DTO.PolityMember;
import com.xaexal.app.DTO.SaintAndChurch;
import com.xaexal.app.DTO.StatisticResult;
import com.xaexal.app.Entity.Saint;

public interface SaintRep extends JpaRepository<Saint, Integer> {
	Optional<Saint> findById(Integer id);
	Optional<Saint> findByMemberIdAndActive(Integer memberId, String active);
	int countByChurchIdAndRole(int churchId, int role);
	int countByChurchIdAndActive(int churchId, String active);
	void deleteByMemberIdAndChurchId(int memberId, int churchId);

	@Query(value = "SELECT COUNT(*) FROM saint " +
			"WHERE church_id=:churchId AND active='1' AND registered IS NOT NULL AND registered != ''",
			nativeQuery = true)
	int countRegisteredByChurchIdAndActive(@Param("churchId") int churchId);

	@Query(value="SELECT m.id member_id, s.id saint_id, m.name, s.church_id, "
			+ "'-' AS church_name, m.mobile, s.role, m.gender, m.birthday,"
			+ "s.registered, r.rname AS rname,"
			+ "GROUP_CONCAT(n.title ORDER BY f.pstn_id SEPARATOR ', ') positions "
			+ "FROM saint s "
			+ "LEFT OUTER JOIN member m ON s.member_id=m.id "
			+ "LEFT OUTER JOIN _roles r ON s.role=r.id "
			+ "LEFT OUTER JOIN staff f ON s.member_id=f.member_id and f.active='1' "
			+ "LEFT OUTER JOIN position n ON f.pstn_id=n.id "
			+ "WHERE s.church_id=:church_id AND s.active='1' "
			+ "GROUP BY m.id, s.id, s.role, r.rname ORDER BY m.name"
			,nativeQuery=true)
	List<PolityMember> searchByChurchId(@Param("church_id") int churchID);

	@Query(value="select s.id,"+
			"s.church_id,c.name church_name,s.registered,"+
			"s.role,ifnull(coalesce(r.rid,rg.rid),0) rid,DATE_FORMAT(s.created,'%Y-%m-%d') created "+
			"from saint s left outer join church c on s.church_id=c.id "+
			"left outer join _roles r on s.role=r.id and s.church_id=r.church_id "+
			"left outer join _roles rg on s.role=rg.id and rg.church_id=0 "+
			"where s.member_id=:member_id and s.active=:active limit 1"
			,nativeQuery=true)
	SaintAndChurch searchByMemberIdAndActive(@Param("member_id") int id,
										@Param("active") String active);

	List<Saint> findByIdAndActive(Integer id, String active);

	// 기간 내 일자별 누적 등록교인 수 (해당 일자 기준 registered <= day 인 active 교인 수)
	@Query(value = "WITH RECURSIVE DailyCalendar AS (" +
            "SELECT DATE(:startDt) AS day " +
            "UNION ALL " +
            "SELECT DATE_ADD(day, INTERVAL 1 DAY) FROM DailyCalendar WHERE day < DATE(:endDt)" +
            ") " +
            "SELECT DATE_FORMAT(c.day, '%Y-%m-%d') AS date, " +
            "(SELECT COUNT(*) FROM saint s WHERE s.church_id=:churchId AND s.active='1' " +
            "  AND s.registered IS NOT NULL AND s.registered <> '' " +
            "  AND s.registered <= DATE_FORMAT(c.day, '%Y-%m-%d')) AS count " +
            "FROM DailyCalendar c ORDER BY c.day", nativeQuery = true)
	List<DailyStatResult> getDailyRegisteredCount(@Param("churchId") int churchId,
	                                              @Param("startDt") String startDt,
	                                              @Param("endDt") String endDt);

	@Query(value="select a.id saint_id,b.id member_id,b.name,b.birthday,"
			+ "b.gender,b.mobile,a.active,a.registered,a.created,"
			+ "a.updated,a.writer from saint a left outer join member b "
			+ "on a.id=b.id where a.church_id=:church_id "
			+ "and a.role=:role_id order by b.name",nativeQuery=true)
	List<Saint> adminListByChurchId(@Param("church_id") int a, @Param("role_id") int roleId);

	@Query(value = "SELECT COUNT(*) as attendent, COUNT(registered) as total " +
            "FROM saint WHERE church_id = :churchId and active='1'", nativeQuery = true)
	StatisticResult getStatistic(@Param("churchId") int churchId);

	@Query(value = "WITH RECURSIVE MonthCalendar AS (" +
            "SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL :period MONTH), '%Y-%m-01') "+
            " AS month_date " +
            "UNION ALL " +
            "SELECT DATE_ADD(month_date, INTERVAL 1 MONTH) " +
            "  FROM MonthCalendar " +
            " WHERE month_date < DATE_FORMAT(CURDATE(), '%Y-%m-01') " +
            ") " +
            "SELECT DATE_FORMAT(c.month_date, '%Y-%m') AS month," +
            "       COUNT(s.registered) AS count " +
            "FROM MonthCalendar c " +
            "LEFT JOIN saint s ON SUBSTRING(s.registered, 1, 7) = DATE_FORMAT(c.month_date, '%Y-%m') " +
            "    AND s.church_id = :church_id and s.active='1' " +
            "GROUP BY month ORDER BY month", nativeQuery = true)
	List<MonthStatResult> get6Month(@Param("church_id") int churchId, @Param("period") int period);

	@Query(value = "WITH RECURSIVE MonthCalendar AS (" +
            "SELECT STR_TO_DATE(CONCAT(:start_month, '-01'), '%Y-%m-%d') AS month_date " +
            "UNION ALL " +
            "SELECT DATE_ADD(month_date, INTERVAL 1 MONTH) " +
            "  FROM MonthCalendar " +
            " WHERE month_date < STR_TO_DATE(CONCAT(:end_month, '-01'), '%Y-%m-%d') " +
            ") " +
            "SELECT DATE_FORMAT(c.month_date, '%Y-%m') AS month," +
            "       COUNT(s.registered) AS count " +
            "FROM MonthCalendar c " +
            "LEFT JOIN saint s ON SUBSTRING(s.registered, 1, 7) = DATE_FORMAT(c.month_date, '%Y-%m') " +
            "    AND s.church_id = :church_id and s.active='1' " +
            "GROUP BY month ORDER BY month", nativeQuery = true)
	List<MonthStatResult> getByMonthRange(@Param("church_id") int churchId,
	                                      @Param("start_month") String startMonth,
	                                      @Param("end_month") String endMonth);

	@Query(value="select a.id,b.name,b.mobile,a.applied,b.gender "
		    + "from saint a left join member b on a.id=b.id "
			+ "where a.church_id=:church_id "
			+ "and a.registered is null "
			+ "order by a.applied,b.name",nativeQuery=true)
	List<MemberBaptismSaint> newcomerList(@Param("church_id") int churchId);
}
