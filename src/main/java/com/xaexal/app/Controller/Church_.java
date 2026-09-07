package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Church;
import com.xaexal.app.Entity.Lov;
import com.xaexal.app.DTO.iPaymentMethod;
import com.xaexal.app.Repository.ChurchRep;
import com.xaexal.app.Repository.LovRep;
import com.xaexal.app.Repository.MemberRep;
import com.xaexal.app.Repository.PaymentMethodRep;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/church")
public class Church_ {
	@Autowired ChurchRep _church;
	@Autowired MemberRep _member;
	@Autowired LovRep _lov;
	@Autowired PaymentMethodRep _pm;

	@LogRequest
	@LoginCheck
	@PostMapping("/new/")
	public ResponseEntity<?> create(@RequestBody Church req,HttpSession s){
		Church c = this._church.save(req);
		if (c==null) {
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("신규등록 실패");
		} else {
			int church_id=c.getId();
			Lov node = this._lov.findByChurchIdAndName(church_id, "세례구분").orElse(null);
			if(node==null) {
				node = new Lov();
				node.setName("세례구분");
				node.setChurchId(church_id);
				node.setParId(0);
				node.setWriter((int)s.getAttribute("member_id"));
				node = this._lov.save(node);

				this._lov.createChildren(church_id, "세례구분", node.getId());
			}

			// 결제(자동결제 구독) 단계에서 사용할 신규 교회 ID. 현재 로그인 사용자를 이 교회의 교인으로 등록하지는 않는다.
			s.setAttribute("new_church_id", church_id);
		    return ResponseEntity.ok(church_id);
		}
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/all/")
	public ResponseEntity<Result<?>> doGet(@RequestParam(name="name", required=false) String name){
		System.out.println("------------> GET /church/all name=["+name+"]");
		List<Church> churches = (name!=null && !name.isBlank())
			? this._church.findByNameLike("%"+name+"%")
			: this._church.findAll();

		return ResponseEntity.ok(new Result<List<Church>>(1,"success",churches));
	}
	// @RequestParam Church req <= 이렇게 하면 안됨. @RequestParam빼야돼.
	@LogRequest
	@LoginCheck
	@GetMapping("/{church_id}")
	public ResponseEntity<?> get(@PathVariable("church_id") int church_id){
		System.out.println("/church/ GET ");

		Church church = this._church.findById(church_id);
		return ResponseEntity.ok(new Result<Church>(1,"",church));
	}
	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> post(@RequestBody Church req, HttpSession s) {
		System.out.println("/church/ POST");

		req.setWriter((int)s.getAttribute("member_id"));
		Church c = this._church.save(req);
		if(c==null) throw new RuntimeException("교회등록실패");
		return ResponseEntity.ok(new Result<>(1,"등록성공"));
	}
	@LoginCheck
	@GetMapping("/{church_id}/paymentmethods")
	public ResponseEntity<?> getPaymentMethods(@PathVariable("church_id") int churchId) {
		return ResponseEntity.ok(new Result<List<iPaymentMethod>>(1, "", _pm.findActiveByChurchId(churchId)));
	}

	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") int id) {

        this._church.deleteById(id);
        return ResponseEntity.ok(new Result<>(1,"삭제성공"));
	}
	@LoginCheck
	@Transactional
	@DeleteMapping("/new/rollback")
	public ResponseEntity<?> rollbackNew(HttpSession s) {
		Integer churchId = (Integer) s.getAttribute("new_church_id");
		if (churchId == null) return ResponseEntity.ok(new Result<>(1, "취소됨"));

		_church.deleteById(churchId);
		s.removeAttribute("new_church_id");

		return ResponseEntity.ok(new Result<>(1, "취소됨"));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/init/{church_id}/{name}/{writer}")
	public ResponseEntity<Integer> doInit(@PathVariable("church_id") int church_id,
									@PathVariable("name") String name,
									@PathVariable("writer") int writer) {
		System.out.println("POST /church/init/"+church_id+"/"+name);

		Lov node = this._lov.findByChurchIdAndName(church_id, name).orElse(null);
		if(node==null) {
			node = new Lov();
			node.setName(name);
			node.setChurchId(church_id);
			node.setParId(0);
			node.setWriter(writer);
			node = this._lov.save(node);
		}
		System.out.println("id ["+node.getId()+"]");
		return ResponseEntity.ok(node.getId());
	}
}
