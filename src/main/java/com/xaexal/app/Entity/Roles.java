package com.xaexal.app.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "_roles", uniqueConstraints = {
    @UniqueConstraint(name = "uq_church_rname", columnNames = {"church_id", "rid"})
})
@Getter @Setter
@DynamicUpdate
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name="par_id", columnDefinition = "INT UNSIGNED")
    private Integer parId;

    @Column(name="rid", columnDefinition = "INT UNSIGNED")
    private Integer rid;

    @Column(name = "church_id", columnDefinition = "INT UNSIGNED")
    private Integer churchId = 0;

    @Column(length = 32)
    private String rname;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "not4general", columnDefinition = "ENUM('0','1')")
    @ColumnDefault("'0'")
    private String not4general;
}
