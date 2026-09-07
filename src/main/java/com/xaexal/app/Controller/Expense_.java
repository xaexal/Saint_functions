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
import com.xaexal.app.DTO.iExpense;
import com.xaexal.app.Entity.Expense;
import com.xaexal.app.Repository.ExpenseRep;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/expense")
public class Expense_ {
	@Autowired private ExpenseRep _expense;

	@LogRequest
	@LoginCheck
	@GetMapping("/get/{church_id}/{start_dt}/{end_dt}")
	public ResponseEntity<?> doGet(@PathVariable("church_id") int churchId,
			@PathVariable("start_dt") String startDt,
			@PathVariable("end_dt") String endDt) {
		List<iExpense> list = this._expense.searchByChurchIdAndIssuedBetween(churchId, startDt, endDt);
		return ResponseEntity.ok(new Result<List<iExpense>>(1, "success", list));
	}

	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Expense req) {
		Expense saved = this._expense.save(req);
		return ResponseEntity.ok(new Result<Expense>(1, "등록성공", saved));
	}

	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
		if (!_expense.existsById(id)) {
			throw new EntityNotFoundException("삭제할 항목이 없습니다.");
		}
		_expense.deleteById(id);
		return ResponseEntity.ok(new Result<>(1, "삭제 성공"));
	}
}
