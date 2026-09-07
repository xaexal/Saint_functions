package com.xaexal.app.DTO;

public interface StaffMember {
    // p.id
    Integer getId();

    // p1.id AS pstn_id
    Integer getPstnId();
    String	getPstnName();
    String	getStarted();
    String	getRetired();

    Integer getMemberId();
    String getMemberName();
    String getBirthday();
    String getMobile();
    String getGender();
    String getActive();
}