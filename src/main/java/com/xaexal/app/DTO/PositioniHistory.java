package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PositioniHistory {
	private int id;

	private int saint_id;

	private int pos_id;

	private String assigned;

	private LocalDateTime created;

	private LocalDateTime updated;

	private int writer;
}
