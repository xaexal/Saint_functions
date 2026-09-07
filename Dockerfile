# OCI Functions용 컨테이너 이미지. func.yaml의 runtime: docker에서 이 파일을 그대로 사용한다.
#
# Spring Boot의 bootJar(중첩 fat-jar)는 Fn 런타임 클래스로더가 이해하지 못하므로,
# plain jar(우리 클래스만) + 의존성 jar들을 평평한 디렉터리(/function/app)에 모아
# Fn Java FDK가 기대하는 flat classpath 구조로 맞춘다.

# ---- Build stage ----
FROM fnproject/fn-java-fdk-build:jdk17-1.1.22 AS build-stage
WORKDIR /function

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew --no-daemon --console=plain build copyRuntimeDeps -x test

# ---- Runtime stage ----
FROM fnproject/fn-java-fdk:jre17-1.1.22
WORKDIR /function

COPY --from=build-stage /function/build/libs/*-plain.jar /function/app/
COPY --from=build-stage /function/build/function-deps/*.jar /function/app/

CMD ["com.xaexal.app.Function.OciFunctionHandler::handleRequest"]
