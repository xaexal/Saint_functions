package com.xaexal.app.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_user_roles")
@IdClass(UserRolesId.class)
@Getter @Setter
public class UserRoles {
    @Id
    @Column(name = "member_id", columnDefinition = "INT UNSIGNED")
    private Integer memberId;

    @Id
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer rid;
}
