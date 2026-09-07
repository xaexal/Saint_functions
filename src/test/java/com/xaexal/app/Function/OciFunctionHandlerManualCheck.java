package com.xaexal.app.Function;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fnproject.fn.api.Headers;
import com.fnproject.fn.api.InvocationContext;
import com.fnproject.fn.api.QueryParameters;
import com.fnproject.fn.api.httpgateway.HTTPGatewayContext;

/**
 * OciFunctionHandler(DispatcherServlet 브릿지)만 단독으로 검증하기 위한 수동 테스트 도구.
 * 실제 OCI Functions/Docker/Fn CLI 없이, Fn Java FDK가 만들어 줄 HTTPGatewayContext를
 * 최소 구현으로 직접 흉내 내서 GET / 요청을 흘려보고 응답을 확인한다.
 *
 * 실행: ./gradlew runOciHandlerCheck (build.gradle 참고)
 * DB는 tunnel 모드로 붙도록 APP_DATASOURCE_MODE=tunnel 등 환경변수를 넘겨서 실행해야 한다.
 */
public class OciFunctionHandlerManualCheck {

    public static void main(String[] args) throws Exception {
        FakeHTTPGatewayContext ctx = new FakeHTTPGatewayContext("GET", "http://localhost/");

        OciFunctionHandler handler = new OciFunctionHandler();
        byte[] body = handler.handleRequest(ctx, new byte[0]);

        System.out.println("=====================================");
        System.out.println("STATUS: " + ctx.statusCode);
        System.out.println("HEADERS: " + ctx.responseHeaders);
        System.out.println("BODY: " + new String(body, StandardCharsets.UTF_8));
        System.out.println("=====================================");

        if (ctx.statusCode == 200) {
            System.out.println("OciFunctionHandler 수동 검증 성공");
            // Tomcat/HikariCP/SSH 터널이 non-daemon 스레드를 띄워두므로 명시적으로 종료한다.
            System.exit(0);
        } else {
            System.out.println("OciFunctionHandler 수동 검증 실패 (status != 200)");
            System.exit(1);
        }
    }

    private static class FakeHTTPGatewayContext implements HTTPGatewayContext {
        private final String method;
        private final String requestURL;
        int statusCode = -1;
        final Map<String, List<String>> responseHeaders = new HashMap<>();

        FakeHTTPGatewayContext(String method, String requestURL) {
            this.method = method;
            this.requestURL = requestURL;
        }

        @Override
        public InvocationContext getInvocationContext() {
            return null;
        }

        @Override
        public Headers getHeaders() {
            return Headers.emptyHeaders();
        }

        @Override
        public String getRequestURL() {
            return requestURL;
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public QueryParameters getQueryParameters() {
            return new QueryParameters() {
                @Override
                public java.util.Optional<String> get(String key) {
                    return java.util.Optional.empty();
                }

                @Override
                public List<String> getValues(String key) {
                    return Collections.emptyList();
                }

                @Override
                public Map<String, List<String>> getAll() {
                    return Collections.emptyMap();
                }
            };
        }

        @Override
        public void addResponseHeader(String key, String value) {
            responseHeaders.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(value);
        }

        @Override
        public void setResponseHeader(String key, String v1, String... vs) {
            java.util.List<String> values = new java.util.ArrayList<>();
            values.add(v1);
            values.addAll(java.util.Arrays.asList(vs));
            responseHeaders.put(key, values);
        }

        @Override
        public void setStatusCode(int code) {
            this.statusCode = code;
        }
    }
}
