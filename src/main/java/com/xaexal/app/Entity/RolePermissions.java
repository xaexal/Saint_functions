package com.xaexal.app.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_role_permissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"church_id", "rid", "pid"}))
@Getter @Setter
public class RolePermissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "rid", columnDefinition = "INT UNSIGNED")
    private Integer rid;

    @Column(columnDefinition = "VARCHAR(20)", nullable = false)
    private String pid;

    @Column(name = "church_id", columnDefinition = "INT UNSIGNED")
    private Integer churchId = 0;
}
