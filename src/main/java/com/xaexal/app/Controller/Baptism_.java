package com.xaexal.app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Baptism;
import com.xaexal.app.Repository.BaptismRep;

@RestController
@RequestMapping("/baptism")
public class Baptism_ {
	@Autowired private BaptismRep _bapt;

	@LoginCheck
	@LogRequest
	@GetMapping("/one/{member_id}")
	public ResponseEntity<?> getOneBaptism(@PathVariable("member_id") int member_id){
		Baptism bapts = this._bapt.findFirstByMemberId(member_id);
		return ResponseEntity.ok(bapts);
	}
	@LoginCheck
	@LogRequest
	@PostMapping("/")
	public ResponseEntity<?> saveBaptism(@RequestBody Baptism baptism){
		Baptism savedBaptism = this._bapt.save(baptism);
		return ResponseEntity.ok(new Result<Baptism>(1,"success",savedBaptism));
	}
}
