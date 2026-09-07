package com.xaexal.app.Service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.Entity.Lov;
import com.xaexal.app.Entity.School;
import com.xaexal.app.Repository.LovRep;
import com.xaexal.app.Repository.SchoolRep;

import jakarta.persistence.EntityNotFoundException;

@Service
public class _Lov_ {
	private LovRep _lov;
	private SchoolRep _school;

	@Transactional
	public void insert(Lov lov) throws Exception {
		int maxSeq = this._lov.findMaxSeqnoByParId(lov.getParId());
		maxSeq++;
		lov.setSeqno(maxSeq);
		Lov l = this._lov.save(lov);
	}
	@Transactional
	public void update(Lov lov) throws Exception {
		int n=0;
		Lov lovOld = new Lov();
		lovOld.setId(lov.getId());
		lovOld = this._lov.findById(lov.getId()).orElseThrow(()->new EntityNotFoundException("ID에 해당하는 LOV가 없음") );
		if(lovOld.getSeqno()>lov.getSeqno()) {
			//new~old-1룰 모두1씩증가
			n = this._lov.updatePlus(lov.getParId(),lov.getSeqno(),lovOld.getSeqno());
		} else if(lovOld.getSeqno()<lov.getSeqno()) {
			// old+1~new를 모두1씩 감소
			n = this._lov.updateMinus(lov.getParId(),lov.getSeqno(),lovOld.getSeqno());
		}
		// 같으면 그대로.
		lov = this._lov.save(lov);

	}
	@Transactional
	public void delete(int id) throws Exception {
		School school = this._school.findById(id).orElseThrow(()->new EntityNotFoundException("ID에 해당하는 course를 찾을 수 없습니다."));

		if(school!=null) throw new RuntimeException("교육과정이 등록된 과정분류는 지울 수 없습니다. 등록된 교육과정들을 먼저 삭제하십시오.");

		int cnt = this._lov.countByParId(id);
		System.out.println("child count ["+cnt+"]");
		if(cnt>0) throw new RuntimeException("하위항목이 있는 항목은 지울 수 없습니다. 하위항목을 먼저 삭제하십시오.");

		this._lov.deleteById(id);
	}
	public List<Lov> getLovList(String type, int churchId) {
	    switch (type) {
	        case "baptism":
	            return _lov.searchChildrenByNameAndChurchIdAndParId("세례구분", churchId, 0);
	        case "offering":
	            return _lov.searchChildrenByNameAndChurchIdAndParId("헌금", churchId, 0);
	        case "education":
	            return _lov.searchChildrenByNameAndChurchIdAndParId("교육과정", churchId, 0);
	        case "affiliate":
	            return _lov.findByParentName("소속교단");
	        default:
	            return Collections.emptyList();
	    }
	}
}
