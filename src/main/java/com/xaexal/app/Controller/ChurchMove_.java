package com.xaexal.app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.ChurchMove;
import com.xaexal.app.Repository.ChurchMoveRep;
import com.xaexal.app.Repository.SaintRep;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/church_move")
public class ChurchMove_ {

    @Autowired private ChurchMoveRep _move;
    @Autowired private SaintRep _saint;

    @LoginCheck
    @Transactional
    @PostMapping("/")
    public ResponseEntity<?> doPost(@RequestBody ChurchMove req) {
        if (req.getNew_church() == null && req.getOld_church() != null && req.getMemberId() != null) {
            _saint.deleteByMemberIdAndChurchId(req.getMemberId(), req.getOld_church());
        }
        _move.save(req);
        return ResponseEntity.ok(new Result<>(1, "저장성공"));
    }
}
