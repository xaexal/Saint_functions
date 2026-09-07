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
import com.xaexal.app.Entity.SpaceBook;
import com.xaexal.app.Repository.SpaceBookRep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/space_book")
public class SpaceBook_ {
    @Autowired private SpaceBookRep _spaceBook;

    @LogRequest
    @LoginCheck
    @GetMapping("/applier/{applier}")
    public ResponseEntity<?> getByApplier(@PathVariable("applier") int applier) {
        List<SpaceBook> list = _spaceBook.findByApplierOrderByIdDesc(applier);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LogRequest
    @LoginCheck
    @GetMapping("/church/{churchId}")
    public ResponseEntity<?> getByChurch(@PathVariable("churchId") int churchId) {
        List<SpaceBook> list = _spaceBook.findByChurchId(churchId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LogRequest
    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> doPost(@RequestBody SpaceBook spaceBook) {
        SpaceBook saved = _spaceBook.save(spaceBook);
        return ResponseEntity.ok(new Result<>(1, "신청완료", saved.getId()));
    }

    @Transactional
    @LogRequest
    @LoginCheck
    @PutMapping("/{id}")
    public ResponseEntity<?> doPut(@PathVariable("id") int id, @RequestBody SpaceBook spaceBook) {
        SpaceBook target = _spaceBook.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 신청 내역을 찾을 수 없습니다."));
        target.setSpaceId(spaceBook.getSpaceId());
        target.setStartDt(spaceBook.getStartDt());
        target.setCloseDt(spaceBook.getCloseDt());
        target.setStartTm(spaceBook.getStartTm());
        target.setCloseTm(spaceBook.getCloseTm());
        target.setDow(spaceBook.getDow());
        target.setNthday(spaceBook.getNthday());
        target.setNthweek(spaceBook.getNthweek());
        target.setIteration(spaceBook.getIteration());
        target.setPurpose(spaceBook.getPurpose());
        target.setPax(spaceBook.getPax());
        target.setApplierName(spaceBook.getApplierName());
        target.setApplierMobile(spaceBook.getApplierMobile());
        target.setApprover(spaceBook.getApprover());
        target.setApproved(spaceBook.getApproved());
        target.setApproval(spaceBook.getApproval());
        target.setRemark(spaceBook.getRemark());
        _spaceBook.save(target);
        return ResponseEntity.ok(new Result<>(1, "수정성공"));
    }

    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
        _spaceBook.deleteById(id);
        return ResponseEntity.ok(new Result<>(1, "취소성공"));
    }
}
