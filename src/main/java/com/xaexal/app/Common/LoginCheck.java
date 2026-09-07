package com.xaexal.app.Common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메소드에 붙이는 어노테이션
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
public @interface LoginCheck {
}