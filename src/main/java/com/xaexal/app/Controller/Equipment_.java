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
import com.xaexal.app.Entity.Equipment;
import com.xaexal.app.Repository.EquipmentRep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/equipment")
public class Equipment_ {
    @Autowired private EquipmentRep _equipment;

    @LogRequest
    @LoginCheck
    @GetMapping("/church/{churchId}")
    public ResponseEntity<?> getByChurch(@PathVariable("churchId") int churchId) {
        List<Equipment> list = _equipment.findByChurchIdOrderByIdAsc(churchId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LogRequest
    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> doPost(@RequestBody Equipment equipment) {
        Equipment saved = _equipment.save(equipment);
        return ResponseEntity.ok(new Result<>(1, "등록성공", saved.getId()));
    }

    @Transactional
    @LogRequest
    @LoginCheck
    @PutMapping("/{id}")
    public ResponseEntity<?> doPut(@PathVariable("id") int id, @RequestBody Equipment equipment) {
        Equipment target = _equipment.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 장비를 찾을 수 없습니다."));
        target.setTitle(equipment.getTitle());
        target.setTotal(equipment.getTotal());
        target.setSpare(equipment.getSpare());
        target.setRemark(equipment.getRemark());
        _equipment.save(target);
        return ResponseEntity.ok(new Result<>(1, "수정성공"));
    }

    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
        _equipment.deleteById(id);
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }
}
