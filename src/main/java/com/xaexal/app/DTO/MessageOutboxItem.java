package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageOutboxItem {
    private Integer id;
    private String title;
    private Long recipientCount;
    private Long unreadCount;
    private LocalDateTime created;
}
