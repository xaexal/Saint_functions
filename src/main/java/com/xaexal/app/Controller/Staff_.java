package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.StaffMember;
import com.xaexal.app.Entity.Staff;
import com.xaexal.app.Repository.StaffRep;

@RestController
@RequestMapping("/staff")
public class Staff_ {
	@Autowired private StaffRep _stff;

	@LogRequest
	@LoginCheck
	@GetMapping("/position/{pstn_id}")
	public ResponseEntity<?> doGet(@PathVariable("pstn_id") int pstn_id){
		System.out.println("GET /staff/position/"+pstn_id);
		List<StaffMember> lmp=this._stff.searchByPstnId(pstn_id);
		System.out.println("lmp size ["+lmp.size()+"]");
		return ResponseEntity.ok(new Result<List<StaffMember>>(1,"",lmp));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/polity/{polity_id}")
	public ResponseEntity<?> doGetByPolity(@PathVariable("polity_id") int polity_id){
		System.out.println("GET /staff/polity/"+polity_id);
		List<StaffMember> lmp=this._stff.searchByPolityId(polity_id);
		System.out.println("lmp size ["+lmp.size()+"]");
		return ResponseEntity.ok(new Result<List<StaffMember>>(1,"",lmp));
	}
//	@GetMapping("/{polity_id}")
//	public ResponseEntity<?> doGet(@PathVariable int polity_id){
//		System.out.println("GET /staff/"+polity_id);
//		try {
//			if(s.getAttribute("member_id")==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Message.signIn);
//
//			List<Staff01> lmp=this._stff.findByPolity(polity_id);
//			System.out.println("lmp size ["+lmp.size()+"]");
//			return ResponseEntity.ok(new Result<List<Staff01>>(1,"",lmp));
//		} catch(Exception e) {
//			System.out.println(e.getMessage());
//			return ResponseEntity.ok(new Result<>(-1,e.getMessage()));
//		}
//	}
	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Staff req){
		System.out.println("POST /staff");
		this._stff.save(req);
		return ResponseEntity.ok(new Result<>(1,"등록성공"));
	}

	@LoginCheck
	@GetMapping("/member/{member_id}/polities")
	public ResponseEntity<?> getMyPolities(@PathVariable("member_id") int memberId) {
		List<Integer> ids = _stff.findPolityIdsByMemberId(memberId);
		return ResponseEntity.ok(new Result<>(1, "", ids));
	}

	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id){
		System.out.println("/DELETE/"+id+" /staff");
		this._stff.deleteById(id);
		return  ResponseEntity.ok(new Result<>(1,"삭제성공"));
	}
}
