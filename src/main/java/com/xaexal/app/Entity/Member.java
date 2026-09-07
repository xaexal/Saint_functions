package com.xaexal.app.Entity;

import java.time.LocalDateTime;

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
@Table(name = "member")
@Getter @Setter
@DynamicUpdate
public class Member extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(length = 32)
    private String name;

    @Column(length = 10)
    private String birthday;

    @Column(length = 2)
    private String gender;

    @Column(length = 24)
    private String mobile;

    @Column(length = 128)
    private String imagefile;

    @Column(length = 12)
    private String passcode;

    @Column(length = 32)
    private String email;

    @Column(length = 5)
    private String postcode;

    @Column(length = 128)
    private String address;

    @Column(length = 8)
    private String marriage;

    @Column(name = "login_tm")
    private LocalDateTime loginTm;

    @Column(name = "logout_tm")
    private LocalDateTime logoutTm;

    @Column(name = "remember_token", length = 64)
    private String rememberToken;

}
