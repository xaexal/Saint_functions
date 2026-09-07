package com.xaexal.app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Church;
import com.xaexal.app.Entity.GlobalPreference;
import com.xaexal.app.Repository.ChurchRep;
import com.xaexal.app.Repository.GlobalPreferenceRep;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/preferences")
public class Preferences_ {

    @Autowired ChurchRep _church;
    @Autowired GlobalPreferenceRep _globalPref;

    // 특정 교회에 속하지 않은 교적관리최고책임자 전용 전역 설정은 항상 id=1인 단일 행에 저장한다.
    private static final int GLOBAL_ID = 1;

    // navbar 전체 '공지사항'/'제안하기'(church=0) 화면은 로그인 여부와 무관하게
    // 최고책임자가 설정한 전역 페이지 크기를 그대로 따라야 하므로 별도의 공개 엔드포인트로 제공한다.
    @GetMapping("/global")
    public ResponseEntity<?> getGlobal() {
        GlobalPreference gp = _globalPref.findById(GLOBAL_ID).orElse(null);
        return ResponseEntity.ok(new Result<>(1, "", Map.of(
            "pageSize", gp != null && gp.getPerPage() != null && gp.getPerPage() > 0 ? gp.getPerPage() : 20
        )));
    }

    @LoginCheck
    @GetMapping("/")
    public ResponseEntity<?> get(HttpSession s) {
        int churchId = (int) s.getAttribute("church_id");
        // 교적관리최고책임자(church_id=0) 등 특정 교회에 속하지 않은 경우 전역 설정을 사용
        if (churchId <= 0) {
            GlobalPreference gp = _globalPref.findById(GLOBAL_ID).orElse(null);
            return ResponseEntity.ok(new Result<>(1, "", Map.of(
                "timeout", gp != null && gp.getTimeout() != null ? gp.getTimeout() : 0,
                "pageSize", gp != null && gp.getPerPage() != null && gp.getPerPage() > 0 ? gp.getPerPage() : 20,
                "defaultPasscode", gp != null && gp.getDefaultPassword() != null ? gp.getDefaultPassword() : "",
                "communityName", gp != null && gp.getCommunityName() != null ? gp.getCommunityName() : "",
                "showDonation", gp != null && gp.getShowDonation() != null ? gp.getShowDonation() : "0"
            )));
        }
        Church church = _church.findById(churchId);
        if (church == null) {
            return ResponseEntity.ok(new Result<>(1, "", Map.of(
                "timeout", 0,
                "pageSize", 20,
                "defaultPasscode", "",
                "communityName", "",
                "showDonation", "0"
            )));
        }
        return ResponseEntity.ok(new Result<>(1, "", Map.of(
            "timeout",        church.getTimeout()         != null ? church.getTimeout()         : 0,
            "pageSize",       church.getPerPage()         != null && church.getPerPage() > 0 ? church.getPerPage() : 20,
            "defaultPasscode",church.getDefaultPassword() != null ? church.getDefaultPassword() : "",
            "communityName",  church.getCommunityName()   != null ? church.getCommunityName()   : "",
            "showDonation",   church.getShowDonation()    != null ? church.getShowDonation()    : "0"
        )));
    }

    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> post(@RequestBody Map<String,Object> req, HttpSession s) {
        int churchId = (int) s.getAttribute("church_id");
        if (churchId <= 0) {
            GlobalPreference gp = _globalPref.findById(GLOBAL_ID).orElse(new GlobalPreference());
            gp.setId(GLOBAL_ID);
            gp.setTimeout(        req.get("timeout")        != null ? (Integer) req.get("timeout")        : gp.getTimeout());
            gp.setPerPage(        req.get("pageSize")       != null ? (Integer) req.get("pageSize")       : gp.getPerPage());
            gp.setDefaultPassword(req.get("defaultPasscode")!= null ? (String)  req.get("defaultPasscode"): gp.getDefaultPassword());
            gp.setCommunityName(  req.get("communityName")  != null ? (String)  req.get("communityName")  : gp.getCommunityName());
            gp.setShowDonation(   req.get("showDonation")   != null ? (String)  req.get("showDonation")   : gp.getShowDonation());
            _globalPref.save(gp);
            return ResponseEntity.ok(new Result<>(1, "저장성공"));
        }
        Church church = _church.findById(churchId);
        if (church == null) {
            return ResponseEntity.badRequest().body(new Result<>(0, "교회 정보를 찾을 수 없습니다."));
        }
        church.setTimeout(       req.get("timeout")        != null ? (Integer) req.get("timeout")        : church.getTimeout());
        church.setPerPage(       req.get("pageSize")       != null ? (Integer) req.get("pageSize")       : church.getPerPage());
        church.setDefaultPassword(req.get("defaultPasscode")!= null ? (String)  req.get("defaultPasscode"): church.getDefaultPassword());
        church.setCommunityName( req.get("communityName")  != null ? (String)  req.get("communityName")  : church.getCommunityName());
        church.setShowDonation(  req.get("showDonation")   != null ? (String)  req.get("showDonation")   : church.getShowDonation());
        _church.save(church);
        return ResponseEntity.ok(new Result<>(1, "저장성공"));
    }
}
