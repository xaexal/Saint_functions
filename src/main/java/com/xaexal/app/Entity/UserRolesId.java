package com.xaexal.app.Entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserRolesId implements Serializable {
    private Integer memberId;
    private Integer rid;
}
