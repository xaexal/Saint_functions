package com.xaexal.app.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Church {
	private Integer id;

	private String name;

	private Integer pastor_id;
	private String pastor;

	private String phone;
	private String postcode;
	private String address;
	private String founded; // 시분초까지 필요없으므로 또 날짜를 모를 수 있으므로.
	private String affiliate; // 소속교단명

	private LocalDateTime created;

	private LocalDateTime updated;
	private Integer creator;
	private Integer writer;
}
