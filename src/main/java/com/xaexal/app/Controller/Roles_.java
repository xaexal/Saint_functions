package com.xaexal.app.Controller;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Roles;
import com.xaexal.app.Repository.RolesRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class Roles_ {
    @Autowired private RolesRep _roles;

    @LogRequest
    @LoginCheck
    @GetMapping("/")
    public ResponseEntity<?> getAll(@RequestParam(name = "churchId", defaultValue = "0") int churchId) {
        List<Roles> list = _roles.findByChurchIdOrderByRname(churchId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody Roles req) {
        if (req.getChurchId() == null) req.setChurchId(0);
        Roles saved = _roles.save(req);
        return ResponseEntity.ok(new Result<>(1, "성공", saved));
    }

    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int id) {
        _roles.deleteById(id);
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }

    @LoginCheck
    @PutMapping("/{id}/parent/{parId}")
    public ResponseEntity<?> updateParent(@PathVariable("id") int id, @PathVariable("parId") int parId) {
        if (id == parId) throw new IllegalArgumentException("자기 자신을 부모로 지정할 수 없습니다.");
        Roles role = _roles.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("해당 역할을 찾을 수 없습니다."));
        role.setParId(parId);
        _roles.save(role);
        return ResponseEntity.ok(new Result<>(1, "이동성공"));
    }
}
