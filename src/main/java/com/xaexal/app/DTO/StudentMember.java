package com.xaexal.app.DTO;

import java.time.LocalDate;

public interface StudentMember {
	// student(s) 필드
    Integer getId();
    Integer	getSchoolId();
    Integer getMemberId();
    String getConfirmed();
    String getGraduated();
    String getStatus();

    // member(m) 필드
    String getMemberName(); // m.name
    String getMobile();
    LocalDate getBirthday();
    String getGender();
}
