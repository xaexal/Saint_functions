package com.xaexal.app.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Lov;
import com.xaexal.app.Repository.LovRep;
//import com.xaexal.app.Repository.SchoolRep;
import com.xaexal.app.Repository.PositionRep;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;

@RestController
public class Lov_ {
	@Autowired private LovRep _lov;
	@Autowired private PositionRep _pstn;
//	@Autowired private SchoolRep _crs;

	@GetMapping("/lovtype/{name}/{church_id}")
	public ResponseEntity<?> doGetType(@PathVariable("name") String name,
									   @PathVariable("church_id") int church_id){
		System.out.println("/lovtype/"+name+"/"+church_id+" GET");
		List<Lov> list=this._lov.searchChildrenByNameAndChurchIdAndParId(name, church_id, 0);
//		List<Lov> list = this._lov.findByNameAndChurchId(name, church_id);
//		_Lov_ lovSvc = new _Lov_();
//
//		List<Lov> list = lovSvc.getLovList(name,church_id);
		return ResponseEntity.ok(new Result<List<Lov>>(1,"",list));
	}

	@PostMapping("/lovtype/")
	public ResponseEntity<?> doPostType(@RequestBody Lov req,HttpSession s){
		this._lov.save(req);
		return ResponseEntity.ok(new Result<>(1,"등록성공"));
	}
//	@DeleteMapping("/lov/type/")
//	public ResponseEntity<?> doDeleteType(@RequestBody Map<String,String> req){
//		req.forEach((key, value) -> {
//		    System.out.println("- "+key + " [" + value + "]");
//		});
//		try {
//			if(s.getAttribute("member_id")==null) return ResponseEntity.ok(new Result<>(-99,Message.signIn));
//
//			int n = this._lov.deleteType(Integer.parseInt(req.get("id")));
//			if(n==0) throw new Exception("삭제실패");
//			return ResponseEntity.ok(new Result<>(1,"삭제성공"));
//		} catch(Exception e) {
//			System.out.println(e.getMessage());
//			return ResponseEntity.ok(new Result<>(-1,e.getMessage()));
//		}
//	}

	@LogRequest
	@LoginCheck
	@GetMapping("/lov/{par_id}/{church_id}")
	public ResponseEntity<?> getList(@PathVariable("par_id") int parId,
									 @PathVariable("church_id") int churchId){
		System.out.println("----------->> GET /lov/"+parId+"/"+churchId);
		List<Lov> list=this._lov.findByParIdAndChurchId(parId, churchId);
		System.out.println("list size ["+list.size()+"]");
		return ResponseEntity.ok(list);
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/lov/")
	public ResponseEntity<?> doGet(Lov req){
		System.out.println("/lov/"+req.getChurchId()+"/"+req.getName()+"/"+req.getVisible()+" GET");
		List<Lov> list = this._lov.searchByChurchIdAndNameOrderBySeqno(req.getChurchId(),req.getName());
		System.out.println("list size ["+list.size()+"]...");
		return ResponseEntity.ok(new Result<List<Lov>>(1,"",list));
	}
	@LogRequest
	@LoginCheck
	@PostMapping("/lov/")
	public ResponseEntity<?> doPost(@RequestBody Lov req) {
		System.out.println("POST /lov/");
		if (req.getId() == null && req.getSeqno() == null) {
			Integer max = this._lov.findMaxSeqnoByParId(req.getParId());
			req.setSeqno((max != null ? max : 0) + 10);
		}
		this._lov.save(req);
		return ResponseEntity.ok(new Result<>(1,"등록성공"));
	}
	@PutMapping("/lov/{target_id}/{source_id}/{relation}")  // change the par_id of the node with id to new par_id
	public ResponseEntity<?> doMove(@PathVariable("target_id") int target_id,
									@PathVariable("source_id") int source_id,
									@PathVariable("relation") String relation){
		System.out.println("/lov/"+target_id+"/"+source_id+" PUT");
		Lov target = this._lov.findById(target_id);
		Lov source = this._lov.findById(source_id);
		int n=0;
		if(target==null || source==null) throw new EntityNotFoundException("데이터가 없습니다.");
		int targetSeqno = target.getSeqno()!=null ? target.getSeqno() : 0;
		if(relation.equals("parent")) {
			n=this._lov.updatePlusAll(target.getId(),0);
			n = this._lov.updateParIdAndSeqnoById(target_id, 1, source_id);
		} else if(relation.equals("sibling")) {
			if(target.getParId()==0) throw new IllegalArgumentException("최상위 항목의 형제로 이동할 수 없습니다.");
			int sourceSeqno = source.getSeqno()!=null ? source.getSeqno() : 0;
			if(java.util.Objects.equals(source.getParId(), target.getParId())) {
				if(targetSeqno<sourceSeqno) {
					n=this._lov.updatePlus(target.getParId(),targetSeqno,sourceSeqno);
				} else if(targetSeqno>sourceSeqno) {
					n=this._lov.updateMinus(target.getParId(),sourceSeqno,targetSeqno);
				}
			} else {
				n=this._lov.updatePlusAll(target.getParId(),targetSeqno);
			}
			n = this._lov.updateParIdAndSeqnoById(target.getParId(), targetSeqno, source_id);
		}
		return ResponseEntity.ok(new Result<>(1,"success"));
	}
	// react-arborist용: PUT /lov/{sourceId}/move?parentId=X&index=Y
	@LoginCheck
	@PutMapping("/lov/{sourceId}/move")
	public ResponseEntity<?> doMoveNode(@PathVariable int sourceId,
	                                     @RequestParam int parentId,
	                                     @RequestParam int index) {
		Lov source = this._lov.findById(sourceId);
		if (source == null) throw new EntityNotFoundException("노드가 없습니다.");
		List<Lov> siblings = this._lov.findByParId(parentId).stream()
			.filter(n -> n.getId() != null && n.getId() != sourceId)
			.sorted(Comparator.comparingInt(n -> n.getSeqno() != null ? n.getSeqno() : 0))
			.collect(Collectors.toList());
		int idx = Math.max(0, Math.min(index, siblings.size()));
		siblings.add(idx, source);
		for (int i = 0; i < siblings.size(); i++)
			this._lov.updateParIdAndSeqnoById(parentId, (i + 1) * 10, siblings.get(i).getId());
		return ResponseEntity.ok(new Result<>(1, "success"));
	}

	@LoginCheck
	@DeleteMapping("/lov/{name}/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("name") String name,@PathVariable("id") int id) {
		System.out.println("/lov/"+id+" DELETE");
		if(name.equals("공동체") || name.equals("부서")) {
			int n = this._pstn.countByPolityId(id);
			if(n>0) throw new EntityNotFoundException("이 "+name+"에 속한 직분목록이 있어서 삭제할 수 없습니다.");
		}
		this._lov.deleteById(id);
		return ResponseEntity.ok(new Result<>(1,"삭제성공"));
	}
}
