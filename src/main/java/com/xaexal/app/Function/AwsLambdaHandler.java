package com.xaexal.app.Function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.xaexal.app.SaintApplication;

/**
 * AWS Lambda 진입점("Lift & Adapt" 브릿지, OciFunctionHandler의 AWS 버전).
 *
 * OCI Functions에는 Spring MVC 앱을 서버리스 위에서 그대로 구동해주는 공식 어댑터가 없어서
 * OciFunctionHandler를 MockHttpServletRequest/Response로 직접 구현해야 했지만, AWS는
 * aws-serverless-java-container-springboot3라는 공식/표준 라이브러리를 제공한다. 이 라이브러리가
 * 내부적으로 OciFunctionHandler와 동일한 원리(요청을 서블릿 요청으로 감싸 기존
 * DispatcherServlet에 위임)로 동작하므로, 여기서는 그 라이브러리를 얇게 감싸기만 한다.
 * Controller/Service 코드는 이번에도 무변경.
 *
 * OciFunctionHandler와 달리 이 라이브러리는 Spring Security의 서블릿 필터 체인까지 그대로
 * 통과시킨다(실측 확인, 2026-09-10) — OAuth2 로그인(/oauth2/authorization/{registrationId})도
 * 별도 작업 없이 정상 동작한다. 단, Kakao/Naver의 redirect-uri는 application.properties에서
 * Google처럼 `{baseUrl}/login/oauth2/code/{registrationId}` 템플릿을 써야 실제 접속 도메인
 * 으로 정확히 해석된다(하드코딩된 https://localhost:8443 값이면 그 값 그대로 나감).
 *
 * 2026-09-11: API Gateway(HTTP API) 대신 Lambda 함수 URL을 쓰도록 전환. 함수 URL이 보내는
 * 이벤트는 API Gateway HTTP API의 "페이로드 포맷 2.0"과 동일한 구조라(AWS 공식 문서 기준),
 * REST 방식(포맷 1.0)을 읽는 getAwsProxyHandler() 대신 getHttpApiV2ProxyHandler()를 써야
 * 한다. API Gateway의 30초 하드 타임아웃이 없어져서(함수 URL은 Lambda 자체 타임아웃까지
 * 그대로 기다림) 콜드스타트가 길어져도 503이 나지 않는다.
 */
public class AwsLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> HANDLER;

    static {
        System.setProperty("spring.profiles.active", "aws");
        try {
            HANDLER = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(SaintApplication.class);
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("SaintApplication을 Lambda 컨테이너 안에서 초기화하지 못했습니다.", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        HANDLER.proxyStream(inputStream, outputStream, context);
    }
}
