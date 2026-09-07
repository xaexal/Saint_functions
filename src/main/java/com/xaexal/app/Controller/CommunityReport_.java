package com.xaexal.app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.CommunityReportDetail;
import com.xaexal.app.DTO.CommunityReportList;
import com.xaexal.app.Entity.CommunityReport;
import com.xaexal.app.Repository.CommunityReportRep;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/community_report")
public class CommunityReport_ {

    @Autowired private CommunityReportRep _rep;

    @GetMapping("/list")
    public ResponseEntity<?> getList(@RequestParam("polity_id") int polityId) {
        List<CommunityReportList> list = _rep.findByPolityId(polityId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LoginCheck
    @GetMapping("/view/{id}")
    public ResponseEntity<?> getDetail(@PathVariable("id") int id) {
        CommunityReportDetail detail = _rep.findDetailById(id);
        return ResponseEntity.ok(new Result<>(1, "", detail));
    }

    @LoginCheck
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody CommunityReport req, HttpSession session) {
        boolean isNew = req.getId() == null;
        if (isNew) req.setWriter((Integer) session.getAttribute("member_id"));
        _rep.save(req);
        return ResponseEntity.ok(new Result<>(1, isNew ? "등록성공" : "수정성공"));
    }
}
