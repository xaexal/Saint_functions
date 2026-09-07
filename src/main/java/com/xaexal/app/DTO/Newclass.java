package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Newclass {
	private Integer id;
	private Integer church_id;
	private String title;
	private String start_dt;
	private String finish_dt;
	private String place;
	private String pastor;
	private String repeat; // none, daily, weekly, monthly
	private String dow; // day of week, '월,수,금', '화,목,토'
	private String nth; // n-th week, '1,3', '2,4',  'last'
	private String start_tm; // 시작시각
	private String finish_tm; // 종료시각
	private Integer occurrence; // 횟수,총회차. default: -1
	private String remark;
	private LocalDateTime created;
	private LocalDateTime updated;
	private Integer writer;
}
