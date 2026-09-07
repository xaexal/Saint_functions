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
import com.xaexal.app.DTO.SchoolResult;
import com.xaexal.app.DTO.SchoolStudentResult;
import com.xaexal.app.Entity.School;
import com.xaexal.app.Repository.SchoolRep;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/school")
public class School_ {
@Autowired private SchoolRep _school;

//	@LoginCheck
//	@GetMapping("/type/{church_id}")
//	public ResponseEntity<?> schoolType(@PathVariable int church_id){
//		System.out.println("/school/"+church_id+" GET");
//		List<Lov> list = this._crs.selectType(church_id);
//		System.out.println("size ["+list.size()+"]");
//		return ResponseEntity.ok(new Result<List<Lov>>(1,"",list));
//	}
	@LogRequest
	@LoginCheck
	@GetMapping("/list/{type_id}")
	public ResponseEntity<?> doGet(@PathVariable("type_id") int type_id){
		System.out.println("------------>> GET /school/"+type_id);

		List<SchoolResult> schools = this._school.getSchoolList(type_id);
		System.out.println("size ["+schools.size()+"]");
		return ResponseEntity.ok(new Result<List<SchoolResult>>(1,"",schools));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/newclass4student/{church_id}/{member_id}")
	public ResponseEntity<?> doGet(@PathVariable("church_id") int church_id,
			@PathVariable("member_id") int member_id){
		System.out.println("------------>> GET /school/list4student/"+church_id+"/"+member_id);

		List<SchoolStudentResult> list = this._school.getSchoolListForStudent(church_id,member_id);
		System.out.println("size ["+list.size()+"]");
		return ResponseEntity.ok(new Result<List<SchoolStudentResult>>(1,"",list));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/{school_id}")
	public ResponseEntity<School> getOne(@PathVariable("school_id") int school_id){
		System.out.println("------------>> GET /school/"+school_id);
		School school = this._school.findById(school_id).orElseThrow(()-> new EntityNotFoundException("해당 과정을 찾을 수 없습니다."));
		return ResponseEntity.ok(school);
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/newcomer/{church_id}")
	public ResponseEntity<?> getNewclassList(@PathVariable ("church_id")int church_id){
		System.out.println("------------>> GET /school/newcomer/"+church_id);
		List<School> schools = _school.getNewclassList(church_id);
		System.out.println("schools ["+schools.size()+"]");
		return ResponseEntity.ok(new Result<List<School>>(1,"",schools));
	}
	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody School school){
		System.out.println("------------>> POST /school/");

		School saved = this._school.save(school);
		return ResponseEntity.ok(new Result<>(1,"등록성공",saved.getId()));
	}
	@LoginCheck
	@GetMapping("/type-used/{type_id}")
	public ResponseEntity<?> isTypeUsed(@PathVariable("type_id") int type_id){
		boolean used = !this._school.getSchoolList(type_id).isEmpty();
		return ResponseEntity.ok(new Result<>(1, "", used));
	}

	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id){
		System.out.println("------------>> GET /school/"+id);
		this._school.deleteById(id);
		return ResponseEntity.ok(new Result<>(1,"삭제성공"));
	}
}
