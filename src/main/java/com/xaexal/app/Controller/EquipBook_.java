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
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.EquipBook;
import com.xaexal.app.Repository.EquipBookRep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/equip_book")
public class EquipBook_ {
    @Autowired private EquipBookRep _equipBook;

    @LogRequest
    @LoginCheck
    @GetMapping("/applier/{applier}")
    public ResponseEntity<?> getByApplier(@PathVariable("applier") int applier) {
        List<EquipBook> list = _equipBook.findByApplierOrderByIdDesc(applier);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LogRequest
    @LoginCheck
    @GetMapping("/church/{churchId}")
    public ResponseEntity<?> getByChurch(@PathVariable("churchId") int churchId) {
        List<EquipBook> list = _equipBook.findByChurchId(churchId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LogRequest
    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> doPost(@RequestBody EquipBook equipBook) {
        EquipBook saved = _equipBook.save(equipBook);
        return ResponseEntity.ok(new Result<>(1, "신청완료", saved.getId()));
    }

    @Transactional
    @LogRequest
    @LoginCheck
    @PutMapping("/{id}")
    public ResponseEntity<?> doPut(@PathVariable("id") int id, @RequestBody EquipBook equipBook) {
        EquipBook target = _equipBook.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 신청 내역을 찾을 수 없습니다."));
        target.setEquipmentId(equipBook.getEquipmentId());
        target.setStartDt(equipBook.getStartDt());
        target.setCloseDt(equipBook.getCloseDt());
        target.setStartTm(equipBook.getStartTm());
        target.setCloseTm(equipBook.getCloseTm());
        target.setDow(equipBook.getDow());
        target.setNthday(equipBook.getNthday());
        target.setNthweek(equipBook.getNthweek());
        target.setIteration(equipBook.getIteration());
        target.setPurpose(equipBook.getPurpose());
        target.setQty(equipBook.getQty());
        target.setApplierName(equipBook.getApplierName());
        target.setApplierMobile(equipBook.getApplierMobile());
        target.setApprover(equipBook.getApprover());
        target.setApproved(equipBook.getApproved());
        target.setApproval(equipBook.getApproval());
        target.setRemark(equipBook.getRemark());
        _equipBook.save(target);
        return ResponseEntity.ok(new Result<>(1, "수정성공"));
    }

    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
        _equipBook.deleteById(id);
        return ResponseEntity.ok(new Result<>(1, "취소성공"));
    }
}
