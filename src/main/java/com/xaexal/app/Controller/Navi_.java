package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.iNaviSub;
import com.xaexal.app.Entity.Navi;
import com.xaexal.app.Repository.NaviRep;


@RestController
@RequestMapping("/navi")
public class Navi_ {
	@Autowired private NaviRep _navi;

//	@GetMapping("/link/{level}")
//	public ResponseEntity<?> getLink(@PathVariable("level") int level,HttpSession s){
//		try {
//			List<Navi> links = this._navi.getLink(level);
//			return ResponseEntity.ok(new Result<List<Navi>>(1,"",links));
//		} catch(Exception e) {
//			return ResponseEntity.ok(new Result<>(-1,e.getMessage()));
//		}
//	}
	@LogRequest
	@LoginCheck
	@GetMapping("/all")
	public ResponseEntity<?> getAll() {
		List<Navi> list = this._navi.findAll(Sort.by("parId", "seqno"));
		return ResponseEntity.ok(new Result<List<Navi>>(1, "", list));
	}

	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> save(@RequestBody Navi req) {
		this._navi.save(req);
		return ResponseEntity.ok(new Result<>(1, "성공"));
	}

	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") int id) {
		this._navi.deleteById(id);
		return ResponseEntity.ok(new Result<>(1, "삭제성공"));
	}

	// 로그인 여부와 상관없이 현재 사용자가 접근 가능한 전체 메뉴(최상위+하위)를 한번에 조회
	@LogRequest
	@GetMapping("/link/{roleId}")
	public ResponseEntity<?> getLink(@PathVariable("roleId") int roleId,
	                                  @RequestParam(name = "churchId", defaultValue = "0") int churchId) {
		if (roleId == -1) {
			List<iNaviSub> links = this._navi.getPublicMenu();
			return ResponseEntity.ok(new Result<List<iNaviSub>>(1, "", links));
		}
		List<iNaviSub> links = roleId == 1
			? this._navi.getMenuAdmin(1)
			: this._navi.getMenu(roleId, churchId);
		return ResponseEntity.ok(new Result<List<iNaviSub>>(1,"",links));
	}
}