package com.xaexal.app.DTO;

import java.time.LocalDateTime;

public interface BoardList {
	Integer getId();
	String getTitle();
	String getWriterName();
	Integer getHit();
	LocalDateTime getCreated();
	LocalDateTime getUPdated();
	Boolean getUrgent();
}
