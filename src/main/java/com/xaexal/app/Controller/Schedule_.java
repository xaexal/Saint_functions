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
import com.xaexal.app.Entity.Schedule;
import com.xaexal.app.Repository.ScheduleRep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/schedule")
public class Schedule_ {
    @Autowired private ScheduleRep _schedule;

    @LogRequest
    @LoginCheck
    @GetMapping("/church/{churchId}")
    public ResponseEntity<?> getByChurch(@PathVariable("churchId") int churchId) {
        List<Schedule> list = _schedule.findByChurchIdOrderByIdAsc(churchId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LogRequest
    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> doPost(@RequestBody Schedule schedule) {
        Schedule saved = _schedule.save(schedule);
        return ResponseEntity.ok(new Result<>(1, "등록성공", saved.getId()));
    }

    @Transactional
    @LogRequest
    @LoginCheck
    @PutMapping("/{id}")
    public ResponseEntity<?> doPut(@PathVariable("id") int id, @RequestBody Schedule schedule) {
        Schedule target = _schedule.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 예배를 찾을 수 없습니다."));
        target.setTitle(schedule.getTitle());
        target.setIteration(schedule.getIteration());
        target.setDow(schedule.getDow());
        target.setNthweek(schedule.getNthweek());
        target.setNthday(schedule.getNthday());
        target.setStartTm(schedule.getStartTm());
        target.setPlace(schedule.getPlace());
        target.setCountable(schedule.getCountable());
        target.setVisible(schedule.getVisible());
        _schedule.save(target);
        return ResponseEntity.ok(new Result<>(1, "수정성공"));
    }

    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
        _schedule.deleteById(id);
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }
}
