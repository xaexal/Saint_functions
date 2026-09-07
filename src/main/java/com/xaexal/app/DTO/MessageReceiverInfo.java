package com.xaexal.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReceiverInfo {
    private Integer id;
    private String name;
    private String gender;
    private String mobile;
    private String address;
}
