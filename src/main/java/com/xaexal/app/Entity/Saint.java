package com.xaexal.app.Entity;

import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "saint")
@Getter @Setter
@DynamicUpdate
public class Saint extends BaseEntity{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

	@Column(name="member_id", columnDefinition="INT UNSIGNED")
	private Integer memberId;

    @Column(name = "church_id", columnDefinition = "INT UNSIGNED")
    private Integer churchId;

    // enum('0','1') 매핑. String으로 처리하거나 별도의 Enum 클래스 활용 가능
    @Column(columnDefinition = "ENUM('0', '1')")
    @ColumnDefault("'0'")
    private String active;

    @Column(length = 10)
    private String registered;

    // enum('신청','과정중','수료') 매핑
    @Column(columnDefinition = "ENUM('신청', '과정중', '수료')")
    private String newcomer;

    private LocalDate applied; // date 타입은 LocalDate와 매핑

    @Column(name = "newclass_id", columnDefinition = "INT UNSIGNED")
    private Integer newclassId;

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer role;
}