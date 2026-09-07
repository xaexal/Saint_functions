package com.xaexal.app.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.MessageDetail;
import com.xaexal.app.DTO.MessageInboxItem;
import com.xaexal.app.DTO.MessageOutboxItem;
import com.xaexal.app.DTO.MessageReceiverInfo;
import com.xaexal.app.Service._Message_;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/message")
public class Message_ {

    @Autowired private _Message_ _svc;

    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> send(HttpSession session, @RequestBody Map<String, Object> req) {
        Integer senderId = (Integer) session.getAttribute("member_id");
        String title = req.get("title").toString();
        String content = req.get("content").toString();
        List<Integer> receiverIds = ((List<?>) req.get("receiverIds")).stream()
            .map(o -> Integer.parseInt(o.toString()))
            .collect(Collectors.toList());
        _svc.send(senderId, title, content, receiverIds);
        return ResponseEntity.ok(new Result<>(1, "전송완료"));
    }

    @LoginCheck
    @GetMapping("/inbox")
    public ResponseEntity<?> inbox(HttpSession session) {
        Integer receiverId = (Integer) session.getAttribute("member_id");
        List<MessageInboxItem> list = _svc.inbox(receiverId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LoginCheck
    @GetMapping("/outbox")
    public ResponseEntity<?> outbox(HttpSession session) {
        Integer senderId = (Integer) session.getAttribute("member_id");
        List<MessageOutboxItem> list = _svc.outbox(senderId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LoginCheck
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Integer id, HttpSession session) {
        Integer currentUserId = (Integer) session.getAttribute("member_id");
        MessageDetail detail = _svc.detail(id, currentUserId);
        return ResponseEntity.ok(new Result<>(1, "", detail));
    }

    @LoginCheck
    @DeleteMapping("/inbox/{id}")
    public ResponseEntity<?> deleteInbox(@PathVariable Integer id, HttpSession session) {
        Integer receiverId = (Integer) session.getAttribute("member_id");
        _svc.deleteInbox(id, receiverId);
        return ResponseEntity.ok(new Result<>(1, "삭제완료"));
    }

    @LoginCheck
    @DeleteMapping("/outbox/{id}")
    public ResponseEntity<?> deleteOutbox(@PathVariable Integer id, HttpSession session) {
        Integer senderId = (Integer) session.getAttribute("member_id");
        _svc.deleteOutbox(id, senderId);
        return ResponseEntity.ok(new Result<>(1, "삭제완료"));
    }

    @LoginCheck
    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(HttpSession session) {
        Integer receiverId = (Integer) session.getAttribute("member_id");
        long count = _svc.unreadCount(receiverId);
        return ResponseEntity.ok(new Result<>(1, "", count));
    }

    @LoginCheck
    @GetMapping("/members")
    public ResponseEntity<?> searchMembers(
            @RequestParam(name = "name", defaultValue = "") String name,
            HttpSession session) {
        Integer churchId = (Integer) session.getAttribute("church_id");
        List<MessageReceiverInfo> list = _svc.searchReceivers(churchId, name);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }
}
