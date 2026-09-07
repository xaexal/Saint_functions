package com.xaexal.app.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Entity.Applicant;
import com.xaexal.app.Entity.Applicant.Role;
import com.xaexal.app.Repository.ApplicantRep;

@RestController
@RequestMapping("/applicant")
public class Applicant_ {

	@Autowired private ApplicantRep _app;

	@LoginCheck
	@GetMapping("/{memberId}/{churchId}")
	public ResponseEntity<?> get(@PathVariable("memberId") int memberId, @PathVariable("churchId") int churchId) {
		System.out.println("/applicant GET memberId [" + memberId + "] churchId [" + churchId + "]");
		List<Applicant> list = _app.findByMemberIdAndChurchId(memberId, churchId);
		return ResponseEntity.ok(list);
	}

	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> post(@RequestBody Map<String, Object> req) {
		req.forEach((k, v) -> System.out.println(k + " [" + v + "]"));
		int memberId   = Integer.parseInt(req.get("memberId").toString());
		int churchId   = Integer.parseInt(req.get("churchId").toString());
		int priorityId = Integer.parseInt(req.get("priority").toString());
		int grade      = Integer.parseInt(req.get("grade").toString());
		Role role      = Role.valueOf(req.get("role").toString());

		if (_app.existsByChurchIdAndMemberIdAndPriorityId(churchId, memberId, priorityId)) {
			Applicant a = _app.findByChurchIdAndMemberIdAndPriorityId(churchId, memberId, priorityId);
			a.setGrade(grade);
			a.setRole(role);
			_app.save(a);
		} else {
			Applicant a = new Applicant();
			a.setMemberId(memberId);
			a.setChurchId(churchId);
			a.setPriorityId(priorityId);
			a.setGrade(grade);
			a.setRole(role);
			_app.save(a);
		}
		return ResponseEntity.ok("저장완료");
	}

	@LoginCheck
	@Transactional
	@DeleteMapping("/{memberId}")
	public ResponseEntity<?> delete(@PathVariable("memberId") int memberId) {
		System.out.println("/applicant DELETE memberId [" + memberId + "]");
		_app.deleteByMemberId(memberId);
		return ResponseEntity.ok("취소완료");
	}
}
