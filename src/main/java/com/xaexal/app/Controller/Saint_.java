package com.xaexal.app.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.ChurchInitService;
import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.DailyStatResult;
import com.xaexal.app.DTO.MemberBaptismSaint;
import com.xaexal.app.DTO.MonthStatResult;
import com.xaexal.app.DTO.PolityMember;
import com.xaexal.app.DTO.SaintAndChurch;
import com.xaexal.app.DTO.StatisticResult;
import com.xaexal.app.Entity.Saint;
import com.xaexal.app.Repository.SaintRep;

@RestController
@RequestMapping("/saint")
public class Saint_ {
	@Autowired private SaintRep _snt;
	@Autowired private ChurchInitService _churchInit;
//	@Autowired private _Church _church;

//	@LoginCheck
//	@LogRequest
//	@GetMapping("/")
//	public ResponseEntity<?> doGet(Saint saint){
//		List<Saint> list = this._snt.select(saint);
//		System.out.println("size ["+list.size()+"]");
//		return ResponseEntity.ok(new Result<List<Saint>>(1,"",list));
//	}
	@LoginCheck
	@GetMapping("/church/{church_id}")
	public ResponseEntity<?> doGetChurch(@PathVariable("church_id") int church_id){
		System.out.println("/saint/church/"+church_id);
		List<PolityMember> members = this._snt.searchByChurchId(church_id);
		System.out.println("saints.size="+members.size());
		return ResponseEntity.ok(new Result<List<PolityMember>>(1,"",members));
	}
	@LoginCheck
	@GetMapping("/member/{id}")
	public ResponseEntity<?> doGetMember(@PathVariable("id") int id){
		System.out.println("/saint/member/ ["+id+"]");
		SaintAndChurch saint = this._snt.searchByMemberIdAndActive(id,"1");
		if (saint == null) return ResponseEntity.ok(null);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", saint.getId());
		result.put("churchId", saint.getChurchId());
		result.put("churchName", saint.getChurchName());
		result.put("registered", saint.getRegistered());
		result.put("role", saint.getRole());
		result.put("created", saint.getCreated());
		System.out.println("/saint/member/ created="+saint.getCreated());
		return ResponseEntity.ok(result);
	}
	@LoginCheck
	@LogRequest
	@GetMapping("/newcomer/{church_id}")
	public ResponseEntity<?> newcomerList(@PathVariable("church_id") int church_id){
		System.out.println("/newcomer/"+church_id+"  GET");
		List<MemberBaptismSaint> list = this._snt.newcomerList(church_id);
		System.out.println("size ["+list.size()+"]");
		return ResponseEntity.ok(new Result<List<MemberBaptismSaint>>(1,"",list));
	}
	@LoginCheck
	@LogRequest
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody Saint saint) {
		if(saint.getMemberId()==null) throw new RuntimeException("Member ID is not given");
		if(saint.getChurchId()==null) throw new RuntimeException("Church ID is not given");
		if(saint.getId()!=null) {
			Integer adminRoleId = _churchInit.resolveChurchAdminRoleId(saint.getChurchId());
			if(adminRoleId!=null && !adminRoleId.equals(saint.getRole())) {
				int n = this._snt.countByChurchIdAndRole(saint.getChurchId(), adminRoleId);
				if(n==1) {
					Saint person = this._snt.findById(saint.getId()).orElse(null);
					if(person==null) throw new RuntimeException("해당하는 사람을 찾을 수 없습니다.");
					if(person.getId().longValue()==saint.getId().longValue() && adminRoleId.equals(person.getRole()))
						throw new RuntimeException("본교회에 교적관리자가 최소한 1인은 있어야 합니다. 역할을 변경할 수 없습니다.");
				}
			}
		}
		this._snt.findByMemberIdAndActive(saint.getMemberId(), "1")
            .ifPresent(man -> {
            	if(man.getChurchId().equals(saint.getChurchId())) {
            		saint.setId(man.getId());
            	} else {
            		man.setActive("0");
            		this._snt.save(man);
            		saint.setRole(null);
            	}
            });
		saint.setActive("1");
		this._snt.save(saint);
		return ResponseEntity.ok(new Result<>(1,"등록완료"));
	}
	@LoginCheck
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id){
		this._snt.deleteById(id);
		return ResponseEntity.ok(new Result<>(1,"삭제됐습니다."));
	}

	// 각 교회 교적관리자 명단
	@LoginCheck
	@LogRequest
	@GetMapping("/admin/{church_id}")
	public ResponseEntity<?> doGetAdmin(@PathVariable("church_id") int church_id) {
		Integer adminRoleId = _churchInit.resolveChurchAdminRoleId(church_id);
		List<Saint> list = adminRoleId != null
				? this._snt.adminListByChurchId(church_id, adminRoleId)
				: List.of();
		return ResponseEntity.ok(new Result<List<Saint>>(1, "", list));
	}
	@LoginCheck
	@LogRequest
	@GetMapping("/statistic/six/{church_id}/{period}")
	public ResponseEntity<?> getStatistic(@PathVariable("church_id") int church_id,
										  @PathVariable("period") int period){
	    // 2. 6개월 통계 데이터 가져오기 (리스트)
	    // 주의: get6Month의 반환 타입은 List<Map<String, Object>> 여야 합니다.
		List<MonthStatResult> six = this._snt.get6Month(church_id,period);

		// 4. 반환
		return ResponseEntity.ok(new Result<List<MonthStatResult>>(1, "success", six));
	}
	@LoginCheck
	@LogRequest
	@GetMapping("/statistic/range/{church_id}/{start_month}/{end_month}")
	public ResponseEntity<?> getStatisticRange(@PathVariable("church_id") int church_id,
	                                           @PathVariable("start_month") String start_month,
	                                           @PathVariable("end_month") String end_month){
		List<MonthStatResult> list = this._snt.getByMonthRange(church_id, start_month, end_month);
		return ResponseEntity.ok(new Result<List<MonthStatResult>>(1, "success", list));
	}
	@LoginCheck
	@LogRequest
	@GetMapping("/statistic/total/{church_id}")
	public ResponseEntity<?> getTotal(@PathVariable("church_id") int church_id){
		StatisticResult stats = this._snt.getStatistic(church_id);

		return ResponseEntity.ok(new Result<StatisticResult>(1, "success", stats));
	}
	@LoginCheck
	@LogRequest
	@GetMapping("/statistic/daily/{church_id}/{start_dt}/{end_dt}")
	public ResponseEntity<?> getDailyStatistic(@PathVariable("church_id") int church_id,
	                                           @PathVariable("start_dt") String start_dt,
	                                           @PathVariable("end_dt") String end_dt){
		List<DailyStatResult> list = this._snt.getDailyRegisteredCount(church_id, start_dt, end_dt);
		return ResponseEntity.ok(new Result<List<DailyStatResult>>(1, "success", list));
	}
}
