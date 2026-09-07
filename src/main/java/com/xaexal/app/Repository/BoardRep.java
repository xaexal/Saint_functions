package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.DTO.BoardList;
import com.xaexal.app.DTO.BoardResult;
import com.xaexal.app.Entity.Board;
import com.xaexal.app.Entity.BoardType;

public interface BoardRep extends JpaRepository<Board, Integer> {
	@Query(value="select b.id,x.title,m.name writer_name,b.hit,b.created,b.updated,x.urgent "+
				 " from board_aux x left outer join board b on b.id=x.board_id "+
				 "			left outer join member m on b.writer=m.id "+
				 "where x.type=:type_id "+
				 "  and ((:church_id = 0 and (x.church_id is null or x.church_id = 0)) or (:church_id > 0 and x.church_id = :church_id)) "+
				 "order by b.created desc",
		   countQuery="select count(*) " +
				 " from board_aux x left join board b on x.board_id=b.id "+
		   		 "left outer join member m on b.writer=m.id "+
		   		 "where x.type=:type_id "+
		   		 "  and ((:church_id = 0 and (x.church_id is null or x.church_id = 0)) or (:church_id > 0 and x.church_id = :church_id))",
		   nativeQuery=true)
	Page<BoardList> getList(@Param("church_id") int churchId, @Param("type_id") int typeId,
			@Param("keyword") String keyword, Pageable pageable);

	@Query(value =
		    "select b.id, b.content, b.writer, b.created created, b.updated, b.hit, b.par_id," +
		    	  " x.type, x.level,x.title, x.urgent, t.name typename, m.name as writer_name "+
		    "from board_aux x left outer join board b on b.id = x.board_id " +
		    			 "left outer join board_type t on x.type=t.id "+
		    			 "left outer join member m on b.writer=m.id "+
		    "where x.type = :type_id and x.title like concat('%', :keyword, '%') " +
		    "order by b.created desc", // Pageable의 Sort가 아닌 쿼리 내 정렬을 우선시할 때
		    countQuery = "select count(*) from board b left outer join board_aux x on b.id = x.board_id " +
		                 "where x.type = :type_id and x.title like concat('%', :keyword, '%')",
		    nativeQuery = true)
	Page<BoardResult> findByTypeIdAndTitleContaining(@Param("type_id") int typeId, @Param("keyword") String keyword,
													 Pageable pageable);

	@Query(value=
			"select b.id, b.content, b.writer , b.created, b.updated, b.hit, b.par_id," +
				  " x.type, x.level,x.title, x.urgent, t.name typename, m.name as writer_name "+
			  "from board_aux x join board b on b.id = x.board_id " +
				    	   "left outer join board_type t on x.type=t.id "+
				    	   "left outer join member m on b.writer=m.id "+
			" where b.id=:id",nativeQuery=true)
	BoardResult searchById(@Param("id") int id);
	@Query(value = "SELECT a.id, a.content, a.writer, c.name AS writer_name, "
            + "       a.created, a.updated, b.cnt "
            + "FROM board a "
            + "LEFT JOIN ("
            + "    SELECT parent_id, COUNT(*) AS cnt FROM board "
            + "    WHERE parent_id IN (SELECT id FROM board WHERE parent_id = :parent_id) "
            + "    GROUP BY parent_id"
            + ") b ON a.id = b.parent_id " // 여기서 b 별칭을 인식하게 됨
            + "LEFT OUTER JOIN member c ON a.writer = c.id "
            + "WHERE a.parent_id = :parent_id",
      nativeQuery = true)
	List<BoardResult> getList(@Param("parent_id") String parentId);
	@Query(value="select count(*) from board_aux x left outer join board a on a.id=x.board_id "+
			     "where x.type=:type "+
			     "  and ((:church_id = 0 and (x.church_id is null or x.church_id = 0)) or (:church_id > 0 and x.church_id = :church_id))",
		   nativeQuery=true)
	int countPost(@Param("church_id") int churchId, @Param("type") int type);

	@Transactional
    @Modifying(clearAutomatically = true)
    @Query(value="UPDATE board SET hit = hit + 1 WHERE id = :id", nativeQuery=true)
    int addHit(@Param("id") int id);

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value="UPDATE board SET content = :content WHERE id = :id", nativeQuery=true)
	int updateContent(@Param("id") int id, @Param("content") String content);

	@Query(value="select * from board_type order by id",nativeQuery=true)
	List<BoardType> selectType();

	@Query(value=
			"select b.id, b.content, b.writer, b.created, b.updated, b.hit, b.par_id," +
				  " x.type, x.level, x.title, x.urgent, t.name typename, m.name as writer_name "+
			"from board_aux x join board b on b.id = x.board_id " +
			"left outer join board_type t on x.type = t.id " +
			"left outer join member m on b.writer = m.id " +
			"where x.type = 1 and x.urgent = 1 " +
			"  and (x.church_id is null or x.church_id = 0 or x.church_id = :church_id) " +
			"order by b.created desc",
		   nativeQuery=true)
	List<BoardResult> getUrgentNotices(@Param("church_id") int churchId);

}
