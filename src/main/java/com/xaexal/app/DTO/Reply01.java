package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Reply01 {
	private String id;
	private String content;
	private Integer writer;
	private String writer_name;
	private LocalDateTime created;
	private LocalDateTime updated;
	private Integer cnt; // 이 댓글이 달린 대댓글의 갯수
}
