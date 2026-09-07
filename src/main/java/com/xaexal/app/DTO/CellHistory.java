package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CellHistory {

	private int id;

	private int member_id;

	private int pos_id;

	private String assigned;
	private String finished;

	private LocalDateTime created;

	private LocalDateTime updated;

	private int writer;

}
