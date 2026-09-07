package com.xaexal.app.Controller;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.RolePermissions;
import com.xaexal.app.Repository.RolePermissionsRep;
import com.xaexal.app.Repository.RolesRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rolepermissions")
public class RolePermissions_ {
    @Autowired private RolePermissionsRep _rp;
    @Autowired private RolesRep _roles;

    @LogRequest
    @LoginCheck
    @GetMapping("/")
    public ResponseEntity<?> getAll(@RequestParam(name = "churchId", defaultValue = "0") int churchId,
                                     @RequestParam(name = "roleId", defaultValue = "-1") int roleId) {
        if (roleId >= 0) {
            var list = _rp.findByChurchIdAndRoleId(churchId, roleId);
            System.out.println("[RolePermissions] churchId=" + churchId + " roleId=" + roleId + " → " + list.size() + "건");
            return ResponseEntity.ok(new Result<>(1, "", list));
        }
        var list = _rp.findAllWithNames(churchId);
        System.out.println("[RolePermissions] churchId=" + churchId + " roleId=" + roleId + " → " + list.size() + "건");
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody RolePermissions req) {
        if (!_rp.existsByChurchIdAndRidAndPid(req.getChurchId(), req.getRid(), req.getPid())) {
            _rp.save(req);
        }
        for (int descId : _roles.findDescendantIds(req.getRid())) {
            if (!_rp.existsByChurchIdAndRidAndPid(req.getChurchId(), descId, req.getPid())) {
                RolePermissions child = new RolePermissions();
                child.setChurchId(req.getChurchId());
                child.setRid(descId);
                child.setPid(req.getPid());
                _rp.save(child);
            }
        }
        return ResponseEntity.ok(new Result<>(1, "성공"));
    }

    @LoginCheck
    @DeleteMapping("/")
    public ResponseEntity<?> delete(@RequestParam("churchId") int churchId,
                                    @RequestParam("rid") int rid,
                                    @RequestParam("pid") String pid) {
        _rp.deleteByChurchIdAndRidAndPid(churchId, rid, pid);
        for (int descId : _roles.findDescendantIds(rid)) {
            _rp.deleteByChurchIdAndRidAndPid(churchId, descId, pid);
        }
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }

    @LoginCheck
    @PostMapping("/all-churches")
    public ResponseEntity<?> saveForAllChurches(@RequestBody RolePermissions req) {
        _rp.saveForAllChurches(req.getRid(), req.getPid());
        return ResponseEntity.ok(new Result<>(1, "성공"));
    }

    @LoginCheck
    @DeleteMapping("/all-churches")
    public ResponseEntity<?> deleteForAllChurches(@RequestParam("rid") int rid,
                                                   @RequestParam("pid") String pid) {
        _rp.deleteAllChurchesByRidAndPid(rid, pid);
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }

}
