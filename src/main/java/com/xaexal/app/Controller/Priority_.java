package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Entity.Priority;
import com.xaexal.app.Repository.PriorityRep;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/priority")
public class Priority_ {

	@Autowired private PriorityRep _priority;

	// parId=0 이면 churchId+parId 조건으로 최상위 목록 반환
	// parId!=0 이면 해당 parId의 하위 목록을 grade 내림차순으로 반환
	@LoginCheck
	@GetMapping("/list/{churchId}/{parId}")
	public ResponseEntity<?> getList(@PathVariable("churchId") int churchId,
									 @PathVariable("parId") int parId) {
		System.out.println("GET /priority/" + churchId + "/" + parId);
		List<Priority> list;
		list = _priority.findByChurchIdAndParIdOrderByGradeDesc(churchId, parId);
		System.out.println("list size [" + list.size() + "]");
		return ResponseEntity.ok(list);
	}

	// 저장 (추가)
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Priority req) {
		System.out.println("POST /priority/");
		Priority p1 = this._priority.save(req);
		System.out.println("------------> p1 id ["+p1.getId()+"]");

		return ResponseEntity.ok("등록성공");
	}

	// 수정 (제목, grade)
	@LoginCheck
	@PutMapping("/{id}")
	public ResponseEntity<?> doUpdate(@PathVariable("id") int id, @RequestBody Priority req) {
		System.out.println("PUT /priority/" + id);
		Priority p = _priority.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("id=" + id + " 레코드가 없습니다."));
		p.setTitle(req.getTitle());
		p.setGrade(req.getGrade());
		_priority.save(p);
		return ResponseEntity.ok("수정성공");
	}

	// 두 레코드의 grade 교체
	@LoginCheck
	@PutMapping("/swap/{id1}/{id2}")
	public ResponseEntity<?> doSwap(@PathVariable("id1") int id1, @PathVariable("id2") int id2) {
		System.out.println("PUT /priority/swap/" + id1 + "/" + id2);
		Priority p1 = _priority.findById(id1)
				.orElseThrow(() -> new EntityNotFoundException("id=" + id1 + " 레코드가 없습니다."));
		Priority p2 = _priority.findById(id2)
				.orElseThrow(() -> new EntityNotFoundException("id=" + id2 + " 레코드가 없습니다."));
		int tmp = p1.getGrade();
		p1.setGrade(p2.getGrade());
		p2.setGrade(tmp);
		_priority.save(p1);
		_priority.save(p2);
		return ResponseEntity.ok("교체성공");
	}

	// id로 단건 조회
	@LoginCheck
	@GetMapping("/getone/priority/{id}")
	public ResponseEntity<?> getOne(@PathVariable("id") int id) {
		System.out.println("GET /priority/" + id);
		Priority p = _priority.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("id=" + id + " 레코드가 없습니다."));
		return ResponseEntity.ok(p);
	}

	// id로 삭제
	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
		System.out.println("DELETE /priority/" + id);
		if (!_priority.findById(id).isPresent())
			throw new EntityNotFoundException("id=" + id + " 레코드가 없습니다.");
		_priority.deleteById(id);
		return ResponseEntity.ok("삭제성공");
	}
}
