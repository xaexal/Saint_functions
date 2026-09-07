package com.xaexal.app.DTO;

import java.time.LocalDateTime;

public interface BoardResult { // Board+BoardAux+BoardType+Member
    // board a
    Integer getId();
    String getContent();
    Integer getWriter();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    Integer getHit();
    Integer getParId();

    // board_aux x
    Integer getType();     // x.id type 매핑
    Integer getLevel();
    String getTitle();
    Boolean getUrgent();

    // board_type b
    String getTypename();  // b.name typename 매핑

    // member c
    String getWriterName(); // c.name writer_name 매핑
}