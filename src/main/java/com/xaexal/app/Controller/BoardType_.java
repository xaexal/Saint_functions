package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Entity.BoardType;
import com.xaexal.app.Repository.BoardTypeRep;

@RestController
@RequestMapping("/boardtype")
public class BoardType_ {
	@Autowired private BoardTypeRep _bdtype;

	@LogRequest
	@GetMapping("/getname/{id}")
	public ResponseEntity<String> getTypename(@PathVariable("id") int id){
		BoardType bdtype = this._bdtype.findById(id);
		return ResponseEntity.ok(bdtype.getName());
	}
	@LogRequest
	@GetMapping("/list/")
	public ResponseEntity<List<BoardType>> getTypelist(){
		List<BoardType> list = this._bdtype.findAll();
		return ResponseEntity.ok(list);
	}
}
