package com.xaexal.app.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Reply {
	private String id;
	private String par_id;
	private int writer;
	private String content;
	private LocalDateTime created;
	private LocalDateTime updated;
	private List<Reply> children;
}
