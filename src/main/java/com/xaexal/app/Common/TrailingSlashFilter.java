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
 * Spring Framework 6부터 "/xxx"와 "/xxx/"를 별개 경로로 엄격히 구분하는데(과거엔 자동으로
 * trailing slash를 허용해줬음), API Gateway(HTTP API)를 거치는 요청은 끝의 '/'가 자꾸
 * 사라져서 도착한다(실측 확인 — 클라이언트가 슬래시를 보내도 마찬가지, curl로도 재현됨).
 * `@RequestMapping(클래스)` + `@GetMapping("/")` 형태(클래스 기본 경로 자체가 매핑) 컨트롤러
 * 메소드들이 전부 "No static resource" 400으로 실패하던 원인이 이것.
 *
 * HandlerMapping을 런타임에 다시 조회해서 판단하는 방식은 Spring이 파싱한 요청경로를 request
 * attribute에 캐싱해두는 탓에(원본 경로 기준으로 한번 계산되면 래핑해도 그 캐시를 그대로 씀)
 * 이 브릿지(aws-serverless-java-container) 환경에서 실측으로 안 먹혔다. 그래서 대신, 이
 * 패턴을 쓰는 컨트롤러들의 기본 경로를 미리 뽑아 고정 목록으로 두고 정확히 그 경로로만
 * 들어온 요청에 한해 끝에 '/'를 붙여준다. 새 컨트롤러가 같은 패턴(@RequestMapping(클래스)
 * + @GetMapping("/") 등)을 쓰면 이 목록에도 추가해야 한다.
 */
@Component
public class TrailingSlashFilter extends OncePerRequestFilter {

    private static final Set<String> BASE_PATHS = Set.of(
            "/applicant", "/baptism", "/board", "/budget", "/bulletin", "/church", "/church_move",
            "/equip_book", "/equipment", "/expense", "/income", "/member", "/message", "/navi",
            "/offering", "/polity", "/position", "/preferences", "/priority", "/reply",
            "/rolepermissions", "/roles", "/saint", "/schedule", "/school", "/space",
            "/space_book", "/staff", "/student", "/userroles"
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
