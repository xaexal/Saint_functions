package com.xaexal.app.Function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
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
 * 알려진 한계는 OciFunctionHandler와 동일하다: Spring Security의 서블릿 필터 체인은
 * 이 경로로 호출되지 않으므로 OAuth2 로그인 엔드포인트는 별도 후속 작업이 필요하다.
 */
public class AwsLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> HANDLER;

    static {
        System.setProperty("spring.profiles.active", "aws");
        try {
            HANDLER = SpringBootLambdaContainerHandler.getAwsProxyHandler(SaintApplication.class);
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
