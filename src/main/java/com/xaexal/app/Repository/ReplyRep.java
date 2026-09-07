package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.xaexal.app.Entity.Reply;

public interface ReplyRep extends JpaRepository<Reply, Integer> {

	@Query(value="with recursive ReplyTree as ("
			+ "select * from reply where par_id=:board_id "
			+ "union all "
			+ "select r.* from reply r inner join ReplyTree t on r.par_id=t.id)"
			+ "select * from ReplyTree order by par_id, id"
	,nativeQuery=true)
	List<Reply> getList(@Param("board_id") int a);

	<Optional>Reply findById(int id);

	@Query(value="SELECT HEX(UNIX_TIMESTAMP(NOW(6)) * 1000000 + MICROSECOND(NOW(6))) AS hex_time"
			,nativeQuery=true)
	String genKey();


}
