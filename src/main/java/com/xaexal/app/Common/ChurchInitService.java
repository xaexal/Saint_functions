package com.xaexal.app.Common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xaexal.app.Entity.Roles;
import com.xaexal.app.Repository.RolePermissionsRep;
import com.xaexal.app.Repository.RolesRep;

@Component
public class ChurchInitService {
    @Autowired private RolesRep _roles;
    @Autowired private RolePermissionsRep _rolePerms;

    public static final String CHURCH_ADMIN_RNAME = "교회교적관리자";

    public void initChurchTables(Integer churchId) {
        if (churchId == null || churchId <= 0) return;
        try {
            _roles.copyFromDefault(churchId);
            _roles.fixParIds(churchId);
        } catch (Exception e) {
            System.err.println("initChurchRoles 실패: " + e.getMessage());
        }
        try {
            if (!_rolePerms.existsByChurchId(churchId))
                _rolePerms.copyFromDefault(churchId);
        } catch (Exception e) {
            System.err.println("initChurchRolePermissions 실패: " + e.getMessage());
        }
    }

    // 교회의 "교회교적관리자" 역할 id(_roles.id)를 반환. 없으면 기본 역할 세트를 먼저 생성한다.
    public Integer resolveChurchAdminRoleId(int churchId) {
        initChurchTables(churchId);
        return _roles.findByChurchIdAndRname(churchId, CHURCH_ADMIN_RNAME)
                .map(Roles::getId)
                .orElseGet(() -> _roles.findByChurchIdAndRname(0, CHURCH_ADMIN_RNAME)
                        .map(Roles::getId).orElse(null));
    }
}
