package com.xaexal.app.Controller;

import java.time.LocalDate;
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
import com.xaexal.app.DTO.OfferResult;
import com.xaexal.app.DTO.iOffer;
import com.xaexal.app.Entity.Offering;
import com.xaexal.app.Repository.OfferingRep;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/offering")
public class Offering_ {
	@Autowired private OfferingRep _ofr;

	@LogRequest
	@LoginCheck
	@GetMapping("/statistic/{church_id}/{start_dt}/{end_dt}")
	public ResponseEntity<?> getStatistic(@PathVariable("church_id") int church_id,
			@PathVariable("start_dt") String start_dt, @PathVariable("end_dt") String end_dt){
		System.out.println("GET /offering/statistic/"+church_id+"/"+start_dt+"/"+end_dt);

		List<OfferResult> list = this._ofr.getStatistic(church_id,start_dt,end_dt);
		System.out.println("list size ["+list.size()+"]...");
		return ResponseEntity.ok(new Result<List<OfferResult>>(1,"",list));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/get/{church_id}/{start_dt}/{end_dt}")
	public ResponseEntity<?> doGet(
	        @PathVariable("church_id") int churchId,
	        @PathVariable("start_dt") String startDt,
	        @PathVariable("end_dt") String endDt) {

	    System.out.println("Converted Dates: " + startDt + " ~ " + endDt);

	    // 2. 리포지토리 호출 (LocalDate 타입 전달)
	    List<iOffer> list = this._ofr.searchByChurchIdAndOfferedBetweenOrderByOfferedDesc(churchId, startDt, endDt);
	    System.out.println("------------->>> size ["+list.size()+"]");
	    return ResponseEntity.ok(new Result<List<iOffer>>(1, "success", list));
	}

	@LoginCheck
	@GetMapping("/my/{member_id}/{start_dt}/{end_dt}")
	public ResponseEntity<?> doGetMy(
	        @PathVariable("member_id") int memberId,
	        @PathVariable("start_dt") String startDt,
	        @PathVariable("end_dt") String endDt) {
	    List<iOffer> list = this._ofr.searchByMemberIdAndOfferedBetween(memberId, startDt, endDt);
	    return ResponseEntity.ok(new Result<List<iOffer>>(1, "success", list));
	}

	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Offering req){
		System.out.println("/offering/ POST");

		Offering o = this._ofr.save(req);
		return ResponseEntity.ok(new Result<>(1,"등록성공"));
	}
	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id){
		System.out.println("/offering/"+id+" DELETE");
		// 1. 존재 여부 확인 후 삭제 (안정적인 방법)
	    if (!_ofr.existsById(id)) {
	        throw new EntityNotFoundException("삭제할 항목이 없습니다.");
	    }

	    _ofr.deleteById(id);

	    return ResponseEntity.ok(new Result<>(1, "삭제 성공"));
	}
}
