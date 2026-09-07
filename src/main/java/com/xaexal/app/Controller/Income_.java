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
import com.xaexal.app.DTO.iIncome;
import com.xaexal.app.Entity.Income;
import com.xaexal.app.Repository.IncomeRep;
import com.xaexal.app.Repository.OfferingRep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/income")
public class Income_ {
	@Autowired private IncomeRep _income;
	@Autowired private OfferingRep _offering;

	@LogRequest
	@LoginCheck
	@GetMapping("/get/{church_id}/{start_dt}/{end_dt}")
	public ResponseEntity<?> doGet(@PathVariable("church_id") int churchId,
			@PathVariable("start_dt") String startDt,
			@PathVariable("end_dt") String endDt) {
		List<iIncome> list = this._income.searchByChurchIdAndIssuedBetween(churchId, startDt, endDt);
		return ResponseEntity.ok(new Result<List<iIncome>>(1, "success", list));
	}

	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Income req) {
		Income saved = this._income.save(req);
		return ResponseEntity.ok(new Result<Income>(1, "등록성공", saved));
	}

	@Transactional
	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
		if (!_income.existsById(id)) {
			throw new EntityNotFoundException("삭제할 항목이 없습니다.");
		}
		_offering.deleteByIncomeId(id);
		_income.deleteById(id);
		return ResponseEntity.ok(new Result<>(1, "삭제 성공"));
	}
}
