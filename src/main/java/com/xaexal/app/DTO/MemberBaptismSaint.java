package com.xaexal.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.xaexal.app.Entity.Member;
import com.xaexal.app.Entity.Baptism;
import com.xaexal.app.Entity.Saint;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberBaptismSaint {
	private Member member;
	private Baptism bapt;
	private Saint saint;
}
