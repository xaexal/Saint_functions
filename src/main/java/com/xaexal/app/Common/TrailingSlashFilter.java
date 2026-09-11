package com.xaexal.app.Common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * API Gateway/Lambda 함수 URL 둘 다, 요청 경로 끝의 '/'를 제거해서 Lambda에 전달한다
 * (클라이언트가 슬래시를 보내도 재현됨). Spring Framework 6부터는 "/xxx"와 "/xxx/"를
 * 별개 경로로 엄격히 구분해서(과거엔 자동으로 trailing slash를 허용해줬음), 끝이 '/'로
 * 매핑된 컨트롤러 메소드가 전부 "No static resource" 400으로 실패한다.
 *
 * 응답을 감싸서 실패 여부를 보고 그때만 재시도하는 동적 방식을 먼저 시도했으나, 이
 * Lambda 서블릿 브릿지(aws-serverless-java-container) 환경에서 실측으로 제대로 동작하지
 * 않았다(RequestDispatcher.forward()도, ContentCachingResponseWrapper로 감싼 뒤 필터
 * 체인을 두 번 태우는 방식도 전부 실패 — 원인 불명, 이 브릿지가 표준 서블릿 스펙을
 * 완전히 구현하지 않는 것으로 추정). 그래서 신뢰할 수 있는 고정 목록 방식으로 되돌리되,
 * 이번엔 클래스 레벨 @RequestMapping과 메소드 레벨 매핑을 전부 조합해서 끝이 '/'로
 * 끝나는 실제 매핑 경로를 컨트롤러 전수조사로 뽑아 빠짐없이 반영했다(2026-09-11).
 * **새 컨트롤러/엔드포인트를 "/xxx/"로 끝나게 추가하면 이 목록에도 반드시 추가해야 한다.**
 */
@Component
public class TrailingSlashFilter extends OncePerRequestFilter {

    private static final Set<String> BASE_PATHS = Set.of(
            "/applicant", "/baptism", "/board", "/board/type_list", "/boardtype/list",
            "/budget", "/bulletin", "/church", "/church/new", "/church/all", "/church_move",
            "/equip_book", "/equipment", "/expense", "/income", "/lov", "/lov/type", "/lovtype",
            "/member", "/message", "/navi", "/offering", "/polity", "/position", "/preferences",
            "/priority", "/reply", "/rolepermissions", "/roles", "/saint", "/schedule", "/school",
            "/space", "/space_book", "/staff", "/student", "/userroles"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!BASE_PATHS.contains(uri)) {
            chain.doFilter(request, response);
            return;
        }
        String withSlash = uri + "/";
        HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(request) {
            @Override public String getRequestURI() { return withSlash; }
            @Override public String getServletPath() { return withSlash; }
        };
        chain.doFilter(wrapped, response);
    }
}
