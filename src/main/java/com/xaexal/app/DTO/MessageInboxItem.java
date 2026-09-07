package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageInboxItem {
    private Integer id;
    private String title;
    private String senderName;
    private Boolean isRead;
    private LocalDateTime created;
}
