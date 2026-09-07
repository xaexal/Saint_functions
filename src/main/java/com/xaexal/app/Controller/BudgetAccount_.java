package com.xaexal.app.Controller;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.iBudgetOverview;
import com.xaexal.app.Entity.BudgetAccount;
import com.xaexal.app.Repository.BudgetAccountRep;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/budget")
public class BudgetAccount_ {
	@Autowired private BudgetAccountRep _ba;

	@LogRequest
	@LoginCheck
	@GetMapping("/overview/{church_id}/{start_fyear}/{end_fyear}")
	public ResponseEntity<?> getOverview(@PathVariable("church_id") int churchId,
			@PathVariable("start_fyear") String startFyear,
			@PathVariable("end_fyear") String endFyear) {
		List<iBudgetOverview> list = this._ba.getOverview(churchId, startFyear, endFyear);
		return ResponseEntity.ok(new Result<List<iBudgetOverview>>(1, "", list));
	}

	// 지출입력 드롭다운용: 자식이 없는 최말단 과목 목록
	@LoginCheck
	@GetMapping("/leaves/{church_id}")
	public ResponseEntity<?> getLeafNodes(@PathVariable("church_id") int churchId) {
		List<BudgetAccount> list = this._ba.findLeafNodesByChurchId(churchId);
		return ResponseEntity.ok(new Result<List<BudgetAccount>>(1, "", list));
	}

	// react-select용: 최말단 과목 + 전체 경로
	@LoginCheck
	@GetMapping("/leaves-with-path/{church_id}")
	public ResponseEntity<?> getLeafNodesWithPath(@PathVariable("church_id") int churchId) {
		List<com.xaexal.app.DTO.iLeafBudget> list = this._ba.findLeafNodesWithPathByChurchId(churchId);
		return ResponseEntity.ok(new Result<>(1, "", list));
	}

	// 드롭다운용: 루트(name) 하위 자식 목록
	@LoginCheck
	@GetMapping("/type/{name}/{church_id}")
	public ResponseEntity<?> doGetType(@PathVariable("name") String name,
			@PathVariable("church_id") int churchId) {
		List<BudgetAccount> list = this._ba.searchChildrenByNameAndChurchIdAndParId(name, churchId, 0);
		return ResponseEntity.ok(new Result<List<BudgetAccount>>(1, "", list));
	}

	@LoginCheck
	@GetMapping("/type-used/{id}")
	public ResponseEntity<?> typeUsed(@PathVariable("id") int id) {
		return ResponseEntity.ok(new Result<>(1, "", _ba.countUsageByAccountId(id) > 0));
	}

	@LogRequest
	@LoginCheck
	@GetMapping("/")
	public ResponseEntity<?> doGet(BudgetAccount req) {
		List<BudgetAccount> list = this._ba.findAllByChurchIdOrderBySeqno(req.getChurchId());
		return ResponseEntity.ok(new Result<List<BudgetAccount>>(1, "", list));
	}

	@LogRequest
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doPost(@RequestBody BudgetAccount req) {
		if (req.getId() == null && req.getSeqno() == null) {
			req.setSeqno(this._ba.maxSeqnoByParId(req.getParId()) + 10);
		}
		BudgetAccount saved = this._ba.save(req);
		updateParentAmountIfZero(saved.getParId());
		return ResponseEntity.ok(new Result<BudgetAccount>(1, "등록성공", saved));
	}

	// 상위항목 amount가 0(또는 null)이면 하위항목 합계로 자동 갱신 (n단계 상위까지 재귀)
	// par_id=0인 항목(최상위 루트 직속)은 제외
	private void updateParentAmountIfZero(Integer parId) {
		if (parId == null || parId == 0) return;
		BudgetAccount parent = _ba.findOneById(parId);
		if (parent == null) return;
		if (parent.getParId() == null || parent.getParId() == 0) return;
		if (parent.getAmount() == null || parent.getAmount().compareTo(BigDecimal.ZERO) == 0) {
			BigDecimal childSum = _ba.sumAmountByParId(parId);
			parent.setAmount(childSum != null ? childSum : BigDecimal.ZERO);
			_ba.save(parent);
			updateParentAmountIfZero(parent.getParId());
		}
	}

	// 금액 저장 전 상하위 합계 초과 여부 검증
	@LoginCheck
	@GetMapping("/{id}/validate-amount")
	public ResponseEntity<?> validateAmount(@PathVariable("id") int id,
			@RequestParam("amount") String amountStr) {
		BigDecimal amount;
		try {
			amount = new BigDecimal(amountStr);
		} catch (NumberFormatException e) {
			return ResponseEntity.ok(new Result<>(0, "금액이 올바르지 않습니다."));
		}
		// 1. 하위항목 합계 > 입력금액 이면 거부
		BigDecimal childSum = _ba.sumAmountByParId(id);
		if (childSum != null && childSum.compareTo(BigDecimal.ZERO) > 0 && childSum.compareTo(amount) > 0) {
			return ResponseEntity.ok(new Result<>(0,
				"하위항목 합계(" + childSum.stripTrailingZeros().toPlainString() + ")가 입력금액을 초과합니다."));
		}
		// 2. 형제항목 합계 + 이 항목 > 상위항목 금액 이면 거부 (상위항목 금액이 0이면 제약 없음)
		BudgetAccount node = _ba.findOneById(id);
		if (node != null && node.getParId() != null && node.getParId() != 0) {
			BudgetAccount parent = _ba.findOneById(node.getParId());
			if (parent != null && parent.getAmount() != null && parent.getAmount().compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal sibSum = _ba.sumAmountByParIdExcluding(node.getParId(), id);
				BigDecimal total = (sibSum != null ? sibSum : BigDecimal.ZERO).add(amount);
				if (total.compareTo(parent.getAmount()) > 0) {
					return ResponseEntity.ok(new Result<>(0,
						"하위항목 합계(" + total.stripTrailingZeros().toPlainString() +
						")가 상위항목 금액(" + parent.getAmount().stripTrailingZeros().toPlainString() + ")을 초과합니다."));
				}
			}
		}
		return ResponseEntity.ok(new Result<>(1, ""));
	}

	// 상위과목의 시작일/종료일이 바뀌었을 때 하위과목(n단계 전체)에 필드 단위로 채운다 (이미 값이 있는 필드는 유지)
	@LoginCheck
	@PostMapping("/{id}/cascade-dates")
	public ResponseEntity<?> cascadeDates(@PathVariable("id") int id, @RequestBody Map<String, String> req) {
		this._ba.cascadeFillDates(id, req.get("startDate"), req.get("endDate"));
		return ResponseEntity.ok(new Result<>(1, "성공"));
	}

	// react-arborist용: PUT /budget/{sourceId}/move?parentId=X&index=Y
	@LoginCheck
	@PutMapping("/{sourceId}/move")
	public ResponseEntity<?> doMoveNode(@PathVariable int sourceId,
	                                     @RequestParam int parentId,
	                                     @RequestParam int index) {
		BudgetAccount source = _ba.findOneById(sourceId);
		if (source == null) throw new EntityNotFoundException("노드가 없습니다.");
		List<BudgetAccount> siblings = _ba.findByParId(parentId).stream()
			.filter(n -> n.getId() != null && n.getId() != sourceId)
			.sorted(Comparator.comparingInt(n -> n.getSeqno() != null ? n.getSeqno() : 0))
			.collect(Collectors.toList());
		int idx = Math.max(0, Math.min(index, siblings.size()));
		siblings.add(idx, source);
		for (int i = 0; i < siblings.size(); i++)
			_ba.updateParIdAndSeqnoById(parentId, (i + 1) * 10, siblings.get(i).getId());
		return ResponseEntity.ok(new Result<>(1, "success"));
	}

	@LoginCheck
	@PutMapping("/{target_id}/{source_id}/{relation}") // change the par_id of the node with id to new par_id
	public ResponseEntity<?> doMove(@PathVariable("target_id") int targetId,
			@PathVariable("source_id") int sourceId, @PathVariable("relation") String relation) {
		BudgetAccount target = this._ba.findOneById(targetId);
		BudgetAccount source = this._ba.findOneById(sourceId);
		if (target == null || source == null) throw new EntityNotFoundException("데이터가 없습니다.");
		int tSeq = target.getSeqno() != null ? target.getSeqno() : 0;
		int sSeq = source.getSeqno() != null ? source.getSeqno() : 0;
		if (relation.equals("parent")) {
			this._ba.updatePlusAll(target.getId(), 0);
			this._ba.updateParIdAndSeqnoById(targetId, 1, sourceId);
		} else if (relation.equals("sibling")) {
			if (source.getParId().equals(target.getParId())) {
				if (tSeq < sSeq) {
					this._ba.updatePlus(target.getParId(), tSeq, sSeq);
				} else if (tSeq > sSeq) {
					this._ba.updateMinus(target.getParId(), sSeq, tSeq);
				}
			} else {
				this._ba.updatePlusAll(target.getParId(), tSeq);
			}
			this._ba.updateParIdAndSeqnoById(target.getParId(), tSeq, sourceId);
		}
		return ResponseEntity.ok(new Result<>(1, "success"));
	}

	@LoginCheck
	@PostMapping("/resequence/{name}")
	public ResponseEntity<?> resequence(@PathVariable("name") String name) {
		int count = this._ba.resequenceChildrenByParentName(name);
		return ResponseEntity.ok(new Result<>(1, count + "건 seqno 재배정 완료"));
	}

	@LoginCheck
	@DeleteMapping("/{name}/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("name") String name, @PathVariable("id") int id) {
		if (this._ba.countUsageByAccountId(id) > 0) {
			throw new EntityNotFoundException("이 과목으로 등록된 예산 또는 지출내역이 있어서 삭제할 수 없습니다.");
		}
		this._ba.deleteById(id);
		return ResponseEntity.ok(new Result<>(1, "삭제성공"));
	}
}
