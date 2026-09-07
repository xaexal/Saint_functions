package com.xaexal.app.Common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

// 이 부분이 핵심입니다!
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Aspect
@Component
public class LogAspect {

    // LogRequest 어노테이션의 위치가 다를 경우 전체 패키지 경로를 적어주세요.
    @Before("@annotation(com.xaexal.app.Common.LogRequest)")
    public void logRequestFields(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            // HttpSession, HttpServletRequest, 기본적인 타입들은 제외하고 DTO만 필터링
            if (arg != null && isCustomObject(arg)) {
                System.out.println(">>> Request Object: " + arg.getClass().getSimpleName());
                for (Field f : arg.getClass().getDeclaredFields()) {
//                    f.setAccessible(true);
                    try {
                    	if(!Modifier.isPublic(f.getModifiers())) continue;
                        System.out.printf("- %s: %s%n", f.getName(), f.get(arg));
                    } catch (Exception e) {
                        System.out.printf("- %s: [접근 불가]%n", f.getName());
                    }
                }
            }
        }
    }

    // 로깅에서 제외할 객체들을 걸러내는 헬퍼 메소드
    private boolean isCustomObject(Object arg) {
        return !(arg instanceof HttpSession ||
                 arg instanceof HttpServletRequest ||
                 arg instanceof org.springframework.ui.Model ||
                 arg.getClass().isPrimitive() ||
                 arg instanceof String);
    }
}