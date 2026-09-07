package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Cell {

	private int id;

	private String name;

	private String started;
	private String closed;

	private LocalDateTime created;

	private LocalDateTime updated;

	private int writer;
}
