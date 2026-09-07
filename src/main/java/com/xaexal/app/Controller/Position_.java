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
import com.xaexal.app.DTO.iPosition;
import com.xaexal.app.Entity.Position;
import com.xaexal.app.Repository.ChurchRep;
import com.xaexal.app.Repository.PositionRep;
import com.xaexal.app.Repository.StaffRep;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/position")
public class Position_ {
	@Autowired PositionRep _pst;
	@Autowired StaffRep _staff;
	@Autowired ChurchRep _church;

	@LogRequest
	@LoginCheck
	@GetMapping("/polity/{polity_id}")
	public ResponseEntity<?> getPositions(@PathVariable("polity_id") int polity_id) {
		System.out.println("/Position/ GET");
		List<iPosition> lpst=this._pst.searchByPolityId(polity_id);
		return ResponseEntity.ok(new Result<List<iPosition>>(1,"",lpst));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/church/{church_id}")
	public ResponseEntity<?> getPositionsByChurch(@PathVariable("church_id") int church_id){
		System.out.println("GET /position/church/"+church_id);
		List<iPosition> list = this._pst.searchByChurchId(church_id);
		return ResponseEntity.ok(new Result<List<iPosition>>(1,"success",list));
	}

//	@GetMapping("/{polity_id}")
//	@LoginCheck
//	public ResponseEntity<?> getPolities(@PathVariable("polity_id") int polity_id){
//		System.out.println("/position/"+polity_id+" GET");
//		try {
//
//			List<Position> lpst=this._pst.findByPolity(polity_id);
//			return ResponseEntity.ok(new Result<List<Position>>(1,"",lpst));
//		} catch(Exception e) {
//			System.out.println(e.getMessage());
//			return ResponseEntity.ok(new Result<>(0,e.getMessage()));
//		}
//	}

	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Position req) throws Exception {
	    System.out.println("------------>> POST /Position/");

	    // 1. 신규 생성인 경우 (id가 null인 경우)
	    if (req.getId() == null) {
	        if (req.getSeqno() == null) {
	            int maxSeq = _pst.searchMaxSeqnoByPolityId(req.getPolityId());
	            req.setSeqno(maxSeq + 1);
	        }
	        // 신규 생성인데 seqno가 지정되어 들어온 경우,
	        // 해당 순서 이후의 데이터들을 밀어주는 로직이 필요하다면 여기에 추가
	    }
	    // 2. 수정인 경우 (id가 이미 있는 경우)
	    else {
	        Position pstn = _pst.findById(req.getId())
	                            .orElseThrow(() -> new EntityNotFoundException("찾을 수 없습니다."));

	        if (req.getSeqno() != null) {
	            if (pstn.getSeqno() > req.getSeqno()) {
	                _pst.updatePlus(req.getPolityId(), req.getSeqno(), pstn.getSeqno());
	            } else if (pstn.getSeqno() < req.getSeqno()) {
	                _pst.updateMinus(req.getPolityId(), req.getSeqno(), pstn.getSeqno());
	            }
	        }
	    }

	    Position pstnAfter = _pst.save(req);
	    return ResponseEntity.ok(new Result<Position>(1, "success", pstnAfter));
	}

	@LoginCheck
	@GetMapping("/polity-used/{polity_id}")
	public ResponseEntity<?> isPolityUsed(@PathVariable("polity_id") int polity_id) {
		boolean used = !this._pst.searchByPolityId(polity_id).isEmpty();
		return ResponseEntity.ok(new Result<>(1, "", used));
	}

	@LoginCheck
	@DeleteMapping("/{pstn_id}")
	public ResponseEntity<?> delete(@PathVariable("pstn_id") int pstn_id) {
		System.out.println("/Position/ DELETE");
		int n = this._staff.countByPstnId(pstn_id);
		if(n>0) throw new EntityNotFoundException("직분을 지우려면 임영된 직분자를 먼제 삭제하십시오");
        this._pst.deleteById(pstn_id);
        return ResponseEntity.ok(new Result<>(1,"삭제성공"));
	}
}
