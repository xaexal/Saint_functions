package com.xaexal.app.DTO;

import java.time.LocalDateTime;

public interface SaintMember {
	Integer getMemberId();
	Integer getSaintId();
	String 	getMemberName();
	Integer getChurchId();
	String	getChurchName();
	String	getMobile();
	Integer	getRoleId();
	String	getRoleName();
	LocalDateTime getLoginTm();
}
