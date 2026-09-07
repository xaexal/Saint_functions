package com.xaexal.app.Common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Aspect
@Component
public class LoginAspect {

    @Autowired
    private HttpServletRequest request; // 현재 요청의 세션을 가져오기 위함

    // @LoginCheck 어노테이션이 붙은 모든 메소드를 대상으로 함
    @Around("@annotation(LoginCheck)")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpSession session = request.getSession();

        if (session.getAttribute("member_id") == null) {
            // 로그인이 안 되어 있다면 401 에러와 메시지 반환
            // 컨트롤러의 반환 타입이 ResponseEntity라고 가정
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("로그인이 필요한 서비스입니다.");
        }

        // 로그인 되어 있다면 원래 실행하려던 메소드(컨트롤러)를 계속 실행
        return joinPoint.proceed();
    }
}