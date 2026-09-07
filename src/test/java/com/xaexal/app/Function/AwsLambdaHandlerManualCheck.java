package com.xaexal.app.Function;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * AwsLambdaHandler(aws-serverless-java-container 브릿지)만 단독으로 검증하기 위한 수동 테스트 도구.
 * 실제 AWS Lambda/API Gateway 없이, API Gateway REST API가 Lambda 프록시 통합으로 보내는
 * JSON 이벤트 포맷을 직접 만들어서 흘려보고 응답을 확인한다.
 *
 * 실행: ./gradlew runAwsHandlerCheck (build.gradle 참고)
 * DB는 tunnel 모드로 붙도록 APP_DATASOURCE_MODE=tunnel 등 환경변수를 넘겨서 실행해야 한다.
 */
public class AwsLambdaHandlerManualCheck {

    private static final String REQUEST_EVENT_JSON = "{"
            + "\"httpMethod\":\"GET\","
            + "\"path\":\"/\","
            + "\"resource\":\"/\","
            + "\"headers\":{\"Host\":\"localhost\"},"
            + "\"multiValueHeaders\":{\"Host\":[\"localhost\"]},"
            + "\"queryStringParameters\":null,"
            + "\"pathParameters\":null,"
            + "\"isBase64Encoded\":false,"
            + "\"requestContext\":{"
            +   "\"httpMethod\":\"GET\","
            +   "\"stage\":\"test\","
            +   "\"requestId\":\"manual-check-request-id\","
            +   "\"resourcePath\":\"/\","
            +   "\"identity\":{\"sourceIp\":\"127.0.0.1\"}"
            + "}"
            + "}";

    public static void main(String[] args) throws Exception {
        AwsLambdaHandler handler = new AwsLambdaHandler();

        ByteArrayInputStream input = new ByteArrayInputStream(
                REQUEST_EVENT_JSON.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        handler.handleRequest(input, output, new FakeLambdaContext());

        String responseJson = output.toString(StandardCharsets.UTF_8);
        System.out.println("=====================================");
        System.out.println("RAW RESPONSE: " + responseJson);
        System.out.println("=====================================");

        if (responseJson.contains("\"statusCode\":200")) {
            System.out.println("AwsLambdaHandler 수동 검증 성공");
            // Tomcat/HikariCP가 non-daemon 스레드를 띄워두므로 명시적으로 종료한다.
            System.exit(0);
        } else {
            System.out.println("AwsLambdaHandler 수동 검증 실패 (statusCode != 200)");
            System.exit(1);
        }
    }

    private static class FakeLambdaContext implements Context {
        @Override
        public String getAwsRequestId() {
            return "manual-check-request-id";
        }

        @Override
        public String getLogGroupName() {
            return "manual-check-log-group";
        }

        @Override
        public String getLogStreamName() {
            return "manual-check-log-stream";
        }

        @Override
        public String getFunctionName() {
            return "saint-functions-manual-check";
        }

        @Override
        public String getFunctionVersion() {
            return "$LATEST";
        }

        @Override
        public String getInvokedFunctionArn() {
            return "arn:aws:lambda:local:000000000000:function:saint-functions-manual-check";
        }

        @Override
        public CognitoIdentity getIdentity() {
            return null;
        }

        @Override
        public ClientContext getClientContext() {
            return null;
        }

        @Override
        public int getRemainingTimeInMillis() {
            return 300000;
        }

        @Override
        public int getMemoryLimitInMB() {
            return 1024;
        }

        @Override
        public LambdaLogger getLogger() {
            return new LambdaLogger() {
                @Override
                public void log(String message) {
                    System.out.println(message);
                }

                @Override
                public void log(byte[] message) {
                    System.out.write(message, 0, message.length);
                }
            };
        }
    }
}
