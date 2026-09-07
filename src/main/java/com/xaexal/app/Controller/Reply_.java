package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Reply;
import com.xaexal.app.Repository.ReplyRep;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/reply")
public class Reply_ {
	@Autowired private ReplyRep _rpl;

	@GetMapping("/list/{id}")
	public ResponseEntity<?> getList(@PathVariable("id") int id) {
		System.out.println("------------>> GET /reply/list/"+id);
		List<Reply> replies = this._rpl.getList(id);
		return ResponseEntity.ok(new Result<List<Reply>>(1, "", replies));
	}
	@GetMapping("/{id}")
	public ResponseEntity<?> doGet(@PathVariable("id") int id) {
		System.out.println("------------>> GET /reply/"+id);

		Reply reply = this._rpl.findById(id);
		if(reply==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("아이디에 해당하는 게시물이 없습니다.");
		return ResponseEntity.ok(new Result<Reply>(1, "", reply));
	}
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Reply reply, HttpSession s) {
		System.out.println("------------>> POST /reply/");
		if (reply.getWriter() == null)
			reply.setWriter((Integer) s.getAttribute("member_id"));
		reply = this._rpl.save(reply);
		System.out.println("new ID ["+reply.getId()+"]");
		return ResponseEntity.ok(new Result<>(1, ""));
	}
	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
		System.out.println("/reply/ DELETE");
		this._rpl.deleteById(id);
		return ResponseEntity.ok(new Result<>(1, "댓글삭제성공"));
	}
}
