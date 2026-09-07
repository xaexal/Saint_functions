package com.xaexal.app.Function;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import com.fnproject.fn.api.httpgateway.HTTPGatewayContext;
import com.xaexal.app.SaintApplication;

/**
 * OCI Functions 진입점("Lift & Adapt" 브릿지).
 *
 * 41개 Controller와 170여 곳의 @LoginCheck를 재작성하지 않기 위해, Controller를
 * 개별 함수로 쪼개는 대신 기존 Spring MVC DispatcherServlet을 그대로 구동하고
 * OCI Functions의 HTTPGatewayContext(HTTP raw 트리거)로 들어온 요청을 그 위에
 * MockHttpServletRequest/Response로 위임한다. Controller/Service 코드는 무변경.
 *
 * 알려진 한계: Spring Security의 서블릿 필터 체인(FilterChainProxy)은 여기서
 * 호출되지 않는다. 이 프로젝트는 인가를 @LoginCheck AOP로 처리하므로 일반
 * REST 엔드포인트는 영향이 없지만, OAuth2 로그인(/oauth2/authorization/*,
 * /login/oauth2/code/*)은 Spring Security 필터가 처리하는 엔드포인트라
 * 이 브릿지만으로는 동작하지 않는다 — 별도 후속 작업 필요.
 */
public class OciFunctionHandler {

    private static volatile ConfigurableApplicationContext springContext;
    private static volatile DispatcherServlet dispatcherServlet;

    public OciFunctionHandler() {
        ensureStarted();
    }

    /**
     * Fn 컨테이너가 warm 상태로 재사용될 때 Spring ApplicationContext를 한 번만 띄우기 위한
     * 지연 초기화. 첫 요청에서만 콜드스타트 비용이 발생한다.
     */
    private static synchronized void ensureStarted() {
        if (dispatcherServlet != null) {
            return;
        }
        System.setProperty("spring.profiles.active", "oci");
        springContext = new SpringApplicationBuilder(SaintApplication.class).run();
        dispatcherServlet = springContext.getBean(DispatcherServlet.class);
    }

    public byte[] handleRequest(HTTPGatewayContext hctx, byte[] body) throws Exception {
        String path = URI.create(hctx.getRequestURL()).getRawPath();

        MockHttpServletRequest request = new MockHttpServletRequest(
                dispatcherServlet.getServletContext(), hctx.getMethod(), path);

        Map<String, List<String>> queryParams = hctx.getQueryParameters().getAll();
        queryParams.forEach((name, values) ->
                values.forEach(value -> request.addParameter(name, value)));

        Map<String, List<String>> headers = hctx.getHeaders().asMap();
        headers.forEach((name, values) ->
                values.forEach(value -> request.addHeader(name, value)));

        if (body != null && body.length > 0) {
            request.setContent(body);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        dispatcherServlet.service(request, response);

        hctx.setStatusCode(response.getStatus());
        for (String headerName : response.getHeaderNames()) {
            List<String> values = response.getHeaderValues(headerName).stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            if (values.isEmpty()) {
                continue;
            }
            String first = values.get(0);
            String[] rest = values.subList(1, values.size()).toArray(new String[0]);
            hctx.setResponseHeader(headerName, first, rest);
        }

        return response.getContentAsByteArray();
    }
}
