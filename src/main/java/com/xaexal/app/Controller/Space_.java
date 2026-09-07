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
import com.xaexal.app.Entity.Space;
import com.xaexal.app.Repository.SpaceRep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/space")
public class Space_ {
    @Autowired private SpaceRep _space;

    @LogRequest
    @LoginCheck
    @GetMapping("/church/{churchId}")
    public ResponseEntity<?> getByChurch(@PathVariable("churchId") int churchId) {
        List<Space> list = _space.findByChurchIdOrderByIdAsc(churchId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LogRequest
    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> doPost(@RequestBody Space space) {
        Space saved = _space.save(space);
        return ResponseEntity.ok(new Result<>(1, "등록성공", saved.getId()));
    }

    @Transactional
    @LogRequest
    @LoginCheck
    @PutMapping("/{id}")
    public ResponseEntity<?> doPut(@PathVariable("id") int id, @RequestBody Space space) {
        Space target = _space.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 공간을 찾을 수 없습니다."));
        target.setTitle(space.getTitle());
        target.setPax(space.getPax());
        target.setSize(space.getSize());
        target.setRemark(space.getRemark());
        target.setBookable(space.getBookable());
        _space.save(target);
        return ResponseEntity.ok(new Result<>(1, "수정성공"));
    }

    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
        _space.deleteById(id);
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }
}
