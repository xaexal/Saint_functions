package com.xaexal.app.DTO;

public interface SchoolStudentResult {
	 // School 엔티티의 필드들
    Integer getId();
    Integer getTypeId();
    String 	getTypeName();
    String 	getTitle();
    Integer getPastorId();
    String	getPastor();
    String	getStaff();
    String	getStartDt();
    String	getCloseDt();
    String	getStartTm();
    String	getCloseTm();
    String	getDow();
    String	getNthday();
    String	getNthweek();
    String	getIteration();
    String	getPlace();
    String	getRemark();
    String	getVisible();
    // Student (t) 관련 (member_id 조건으로 조인된 결과)
    Integer getStudentId(); // t.id
    String 	getStatus();     // 수강 상태 등
    String	getConfirmed();
    String	getGraduated();

}