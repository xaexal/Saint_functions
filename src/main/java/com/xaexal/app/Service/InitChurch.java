package com.xaexal.app.Service;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.Entity.Church;
import com.xaexal.app.Entity.Lov;
import com.xaexal.app.Repository.ChurchRep;
import com.xaexal.app.Repository.LovRep;

@Service
public class InitChurch {
	private ChurchRep _church;
	private LovRep _lov;

	@Transactional
	public int createLOV(int church_id) {

		ArrayList<String> alItem = new ArrayList<>(Arrays.asList("세례구분","헌금","교육과정"));
		int n = 0;
		for(String item : alItem) {
			// 교육과정은 단순히 값의 목록이 아니기 때문에, 따로 course테이블이 있어야 함
			// 단, course테이블의 type은 LOV에 교육과정으로 등록해야 함.
			// LOV테이블에 church_id+name으로 unique index를 만들기 때문에,
			// 이미 있는지 검사할 필요없이 바로 만들어서 실패하면 skip.
			// 새로 만들어지면, 없었던 거라서 ok.
//			n = this._lov.createRoot(church_id, item);

			// 1. Optional에서 값을 꺼내거나, 없으면 예외를 던집니다.
			Lov lov = this._lov.findByChurchIdAndName(church_id, item)
			                   .orElseThrow(() -> new RuntimeException(item + " 항목이 생성되지 않았습니다."));

			// 2. 이제 lov는 Optional이 아닌 Lov 객체이므로 getId() 호출이 가능합니다.
			int parID = lov.getId();
			n = this._lov.createChildren(church_id,item,parID);
		}
		return n;
	}
}
