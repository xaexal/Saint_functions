package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.DTO.StudentMember;
import com.xaexal.app.Entity.Student;
import com.xaexal.app.Repository.StudentRep;

@RestController
@RequestMapping("/student")
public class Student_ {
	@Autowired StudentRep _student;

	@LogRequest
	@LoginCheck
	@GetMapping("/list/{school_id}")
	public ResponseEntity<List<StudentMember>> getStudentList(@PathVariable("school_id") int school_id) {
		List<StudentMember> students = this._student.getStudentList(school_id);
		System.out.println("students size ["+students.size()+"]");
		return ResponseEntity.ok(students);
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/one/{school_id}/{member_id}")
	public ResponseEntity<Student> getStudent(@PathVariable("school_id") int school_id,
													@PathVariable("member_id") int member_id){
		Student student = this._student.findBySchoolIdAndMemberId(school_id,member_id);
		return ResponseEntity.ok(student != null ? student : new Student());
	}
	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<String> save(@RequestBody Student student){
		if(this._student.findBySchoolIdAndMemberId(student.getSchoolId(),student.getMemberId()) != null)
			throw new RuntimeException("이미 같은 과정을 신청했습니다. 신청된 과정을 먼저 취소하십시오.");
		int n = this._student.checkDuplicate(student.getSchoolId(),student.getMemberId());
		if(n>0) throw new RuntimeException("이미 같은 과정을 신청했습니다. 신청된 과정을 먼저 취소하십시오.");
		this._student.save(student);
		return ResponseEntity.ok("등록완료");
	}
	@LogRequest
	@LoginCheck
	@PutMapping("/")
	public ResponseEntity<String> update(@RequestBody Student student){
		this._student.save(student);
		return ResponseEntity.ok("변경완료");
	}
	@LogRequest
	@LoginCheck
	@DeleteMapping("/{student_id}")
	public ResponseEntity<String> delete(@PathVariable("student_id") int student_id){
		_student.deleteById(student_id);
		return ResponseEntity.ok("삭제완료");
	}
}
