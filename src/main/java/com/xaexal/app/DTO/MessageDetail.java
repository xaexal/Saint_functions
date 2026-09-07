package com.xaexal.app.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class MessageDetail {
    private Integer id;
    private String title;
    private String content;
    private Integer senderId;
    private String senderName;
    private LocalDateTime created;
    // 받은 사람 뷰
    private Boolean isRead;
    private LocalDateTime readAt;
    // 보낸 사람 뷰
    private List<RecipientStatus> recipients;
}
