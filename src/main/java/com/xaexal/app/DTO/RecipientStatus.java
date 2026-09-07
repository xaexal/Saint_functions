package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipientStatus {
    private Integer receiverId;
    private String receiverName;
    private Boolean isRead;
    private LocalDateTime readAt;
}
