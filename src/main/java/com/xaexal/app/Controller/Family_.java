package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.FamilyMember;
import com.xaexal.app.Entity.Member;
import com.xaexal.app.Repository.FamilyRep;

@RestController
@RequestMapping("/family")
public class Family_ {
	@Autowired private FamilyRep _fml;

	// 가족목록 (부/모/배우자/자녀를 합쳐서 조회, 각 행에 relation 표시)
	@LoginCheck
	@GetMapping("/{member_id}")
	public ResponseEntity<?> getFamily(@PathVariable("member_id") int member_id){
		System.out.println("/family/"+member_id+" GET");

		List<FamilyMember> list = this._fml.findFamilyMembers(member_id);
		return ResponseEntity.ok(new Result<List<FamilyMember>>(1,"",list));
	}
	// 이름 또는 모바일번호로 후보 검색 (본인 제외)
	@LoginCheck
	@GetMapping("/candidate/{member_id}/{keyword}")
	public ResponseEntity<?> getCandicate(@PathVariable("member_id") int member_id,
			@PathVariable("keyword") String keyword){
		System.out.println("/family/candidate/"+member_id+"/"+keyword);

		List<Member> list=this._fml.findCandidateByKeyword(member_id, keyword);
		return ResponseEntity.ok(new Result<List<Member>>(1,"",list));
	}
	// ===== 부모/자녀/배우자 관계 =====

	// 부모 등록 (relation: 부 또는 모). 이미 등록되어 있으면 교체됨.
	@LoginCheck
	@LogRequest
	@PostMapping("/parent/{child_id}/{parent_id}/{relation}")
	public ResponseEntity<?> registerParent(@PathVariable("child_id") int childId,
			@PathVariable("parent_id") int parentId,
			@PathVariable("relation") String relation){
		System.out.println("/family/parent/"+childId+"/"+parentId+"/"+relation+" POST");

		if(!relation.equals("부") && !relation.equals("모")) {
			throw new RuntimeException("relation은 '부' 또는 '모'만 가능합니다.");
		}
		this._fml.registerParent(childId, parentId, relation);
		return ResponseEntity.ok(new Result<String>(1,"success","부모 등록완료"));
	}

	// 부모 관계 삭제
	@LoginCheck
	@DeleteMapping("/parent/{child_id}/{relation}")
	public ResponseEntity<?> deleteParent(@PathVariable("child_id") int childId,
			@PathVariable("relation") String relation){
		System.out.println("/family/parent/"+childId+"/"+relation+" DELETE");

		if(!relation.equals("부") && !relation.equals("모")) {
			throw new RuntimeException("relation은 '부' 또는 '모'만 가능합니다.");
		}
		this._fml.deleteParent(childId, relation);
		return ResponseEntity.ok(new Result<>(1,"부모관계 삭제 성공"));
	}

	// 부모 조회
	@LoginCheck
	@GetMapping("/parents/{member_id}")
	public ResponseEntity<?> getParents(@PathVariable("member_id") int member_id){
		System.out.println("/family/parents/"+member_id+" GET");

		List<Member> list = this._fml.findParents(member_id);
		return ResponseEntity.ok(new Result<List<Member>>(1,"",list));
	}

	// 부 또는 모 단건 조회
	@LoginCheck
	@GetMapping("/parent/{member_id}/{relation}")
	public ResponseEntity<?> getParentByType(@PathVariable("member_id") int member_id,
			@PathVariable("relation") String relation){
		System.out.println("/family/parent/"+member_id+"/"+relation+" GET");

		if(!relation.equals("부") && !relation.equals("모")) {
			throw new RuntimeException("relation은 '부' 또는 '모'만 가능합니다.");
		}
		Member m = this._fml.findParentByType(member_id, relation);
		return ResponseEntity.ok(new Result<Member>(1,"",m));
	}

	// 자녀 조회
	@LoginCheck
	@GetMapping("/children/{member_id}")
	public ResponseEntity<?> getChildren(@PathVariable("member_id") int member_id){
		System.out.println("/family/children/"+member_id+" GET");

		List<Member> list = this._fml.findChildren(member_id);
		return ResponseEntity.ok(new Result<List<Member>>(1,"",list));
	}

	// 배우자 등록. 이미 등록되어 있으면 교체됨.
	@LoginCheck
	@LogRequest
	@PostMapping("/spouse/{member_id}/{spouse_id}")
	public ResponseEntity<?> registerSpouse(@PathVariable("member_id") int memberId,
			@PathVariable("spouse_id") int spouseId){
		System.out.println("/family/spouse/"+memberId+"/"+spouseId+" POST");

		this._fml.registerSpouse(memberId, spouseId);
		return ResponseEntity.ok(new Result<String>(1,"success","배우자 등록완료"));
	}

	// 배우자 관계 삭제
	@LoginCheck
	@DeleteMapping("/spouse/{member_id}")
	public ResponseEntity<?> deleteSpouse(@PathVariable("member_id") int memberId){
		System.out.println("/family/spouse/"+memberId+" DELETE");

		this._fml.deleteSpouse(memberId);
		return ResponseEntity.ok(new Result<>(1,"배우자관계 삭제 성공"));
	}

	// 배우자 조회
	@LoginCheck
	@GetMapping("/spouse/{member_id}")
	public ResponseEntity<?> getSpouse(@PathVariable("member_id") int member_id){
		System.out.println("/family/spouse/"+member_id+" GET");

		List<Member> list = this._fml.findSpouse(member_id);
		return ResponseEntity.ok(new Result<List<Member>>(1,"",list));
	}
}
