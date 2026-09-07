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
import com.xaexal.app.DTO.iOrgChart;
import com.xaexal.app.Entity.Lov;
import com.xaexal.app.Repository.LovRep;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/polity")
public class Polity_ {
	@Autowired private LovRep _lov;
//	@Autowired private _Church _church;

	@LogRequest
	@LoginCheck
	@GetMapping("/{church_id}/{visible}")
	public ResponseEntity<?> doGet(@PathVariable("church_id") int church_id,@PathVariable("visible") String visible){
		System.out.println("/polity/ GET");
		List<Lov> polities = this._lov.findByChurchIdAndVisible(church_id,visible);
		System.out.println("polities size ["+polities.size()+"]");
		return ResponseEntity.ok(new Result<List<Lov>>(1,"",polities));
	}
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Lov req) throws Exception{
		System.out.println("-------------> /Polity/ POST");
		if(req.getId()==null) {
			int maxSeq = _lov.findMaxSeqnoByParId(req.getParId());
			req.setSeqno(++maxSeq);
		} else {
			Lov plt = _lov.findById(req.getId()).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다."));
			if(plt.getSeqno()>req.getSeqno()) {
				_lov.updatePlus(req.getParId(),req.getSeqno(),plt.getSeqno());
			} else {
				_lov.updateMinus(req.getParId(), req.getSeqno(), plt.getSeqno());
			}
		}
		_lov.save(req);
		return ResponseEntity.ok(new Result<>(1, "등록성공"));
	}
	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id) throws Exception {
		System.out.println("/Polity/"+id+" DELETE");
		if(id==0) throw new RuntimeException("교회이름은 삭제할 수 없습니다.");

		this.deleteR(id);
		return ResponseEntity.ok(new Result<>(1,"삭제성공"));
	}
	private void deleteR(int id) throws Exception{
		System.out.println("par_id ["+id+"]");

		List<Lov> polities = this._lov.findByParId(id);
		if(polities.size()>0) {
			System.out.println("size="+polities.size());
			for(Lov x : polities){
				this.deleteR(x.getId());
			}
		}
		this._lov.deleteById(id);
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/orgchart/{church_id}/{name}")
	public ResponseEntity<?> getTree(@PathVariable("church_id") int church_id,
									 @PathVariable("name") String name){
		List<iOrgChart> list=this._lov.getTree(church_id,name);
		return ResponseEntity.ok(list);
	}
}
