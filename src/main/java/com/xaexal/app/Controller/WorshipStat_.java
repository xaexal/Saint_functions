package com.xaexal.app.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Schedule;
import com.xaexal.app.Entity.WorshipStat;
import com.xaexal.app.Repository.ScheduleRep;
import com.xaexal.app.Repository.WorshipStatRep;

@RestController
@RequestMapping("/worship_stat")
public class WorshipStat_ {
    @Autowired private WorshipStatRep _ws;
    @Autowired private ScheduleRep _sc;

    @LoginCheck
    @GetMapping("/served/{served}")
    public ResponseEntity<?> getByServed(@PathVariable("served") String served) {
        List<WorshipStat> list = _ws.findByServed(served);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LoginCheck
    @GetMapping("/range/{churchId}/{start}/{end}")
    public ResponseEntity<?> getByRange(
            @PathVariable("churchId") Integer churchId,
            @PathVariable("start") String start,
            @PathVariable("end") String end) {
        List<Integer> ids = _sc.findByChurchIdOrderByIdAsc(churchId)
                .stream().map(Schedule::getId).collect(Collectors.toList());
        if (ids.isEmpty()) return ResponseEntity.ok(new Result<>(1, "", List.of()));
        List<WorshipStat> list = _ws.findByServedBetweenAndWorshipIdIn(start, end, ids);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @Transactional
    @LoginCheck
    @DeleteMapping("/{worshipId}/{served}")
    public ResponseEntity<?> deleteOne(
            @PathVariable("worshipId") Integer worshipId,
            @PathVariable("served") String served) {
        _ws.deleteByServedAndWorshipId(served, worshipId);
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }

    @Transactional
    @LoginCheck
    @PostMapping("/batch")
    public ResponseEntity<?> saveBatch(@RequestBody List<WorshipStat> list) {
        if (list.isEmpty()) return ResponseEntity.ok(new Result<>(1, "저장성공"));
        String served = list.get(0).getServed();
        List<Integer> ids = list.stream().map(WorshipStat::getWorshipId).collect(Collectors.toList());
        _ws.deleteByServedAndWorshipIdIn(served, ids);
        _ws.saveAll(list);
        return ResponseEntity.ok(new Result<>(1, "저장성공"));
    }
}
