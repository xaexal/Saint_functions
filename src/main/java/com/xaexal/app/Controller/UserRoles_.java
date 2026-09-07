package com.xaexal.app.Controller;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Repository.SaintRep;
import com.xaexal.app.Repository.UserRolesRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/userroles")
public class UserRoles_ {
    @Autowired private UserRolesRep _ur;
    @Autowired private SaintRep _snt;

    @LogRequest
    @LoginCheck
    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(new Result<>(1, "", _ur.findAllWithNames()));
    }

    @LoginCheck
    @GetMapping("/saint/{saintId}")
    public ResponseEntity<?> findBySaintId(@PathVariable("saintId") int saintId) {
        return ResponseEntity.ok(new Result<>(1, "", _ur.findOneBySaintId(saintId)));
    }

    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody Map<String, Integer> req) {
        _snt.findById(req.get("saintId")).ifPresent(s -> {
            s.setRole(req.get("roleId"));
            _snt.save(s);
        });
        return ResponseEntity.ok(new Result<>(1, "성공"));
    }

    @LoginCheck
    @DeleteMapping("/{saintId}")
    public ResponseEntity<?> delete(@PathVariable("saintId") int saintId) {
        _snt.findById(saintId).ifPresent(s -> {
            s.setRole(null);
            _snt.save(s);
        });
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }
}
