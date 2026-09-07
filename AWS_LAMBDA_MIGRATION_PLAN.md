# Saint_functions → AWS Lambda 전환 계획

- 대상 저장소: `C:/Users/xaexal/git/Saint_functions` (원본 `C:/Users/xaexal/git/Saint`는 여전히 절대 수정하지 않음)
- 계획 작성 시점: 2026-09-08
- 선행 문서: `OCI_FUNCTIONS_ANALYSIS.md`(코드베이스 구조 분석 — 클라우드 무관, 그대로 유효), `OCI_FUNCTIONS_MIGRATION_PLAN.md`(OCI 전환 계획 — 실행 모델/네트워킹만 다르고 코드 구조 판단은 대부분 재사용 가능), `OCI_SUPPORT_TICKET*.md`(OCI Functions 중단 사유 전체 기록)

## 0. 전환 배경 (요약)

OCI Functions로 실제 배포까지 마쳤으나, Function의 보조 VNIC이 VCN 내부 사설 IP는 물론 **인터넷 공인 IP로도 아웃바운드 라우팅이 안 되는** 플랫폼 레벨 버그가 있는 것으로 결론지음(`NoRouteToHostException`, 사설/공인 IP 둘 다 동일하게 실패 — 자세한 재현 절차/증거는 `OCI_SUPPORT_TICKET_EN.md` 12절 참고). OCI 지원팀 문의도 반복 루프에 빠져 진전이 없어 SR을 종료하고, **서버리스 계층만 AWS Lambda로 전환**하기로 결정함(2026-09-08, 사용자 결정).

**범위**: MySQL DB와 Compute VM(`193.123.234.59` / `10.0.0.14`)은 **그대로 OCI에 유지**. DB 마이그레이션은 하지 않는다. Lambda는 인터넷을 경유해 Compute VM의 **공인 IP** `193.123.234.59:3306`으로 접속한다. 이 포트는 이미 Security List에서 `0.0.0.0/0`에 열려 있는 상태였으므로([[reference_saintfunctions_oci_infra]] 참고) 별도 방화벽 작업이 필요 없다 — 오히려 OCI Functions 때와 정반대로, **Lambda는 기본적으로 VPC에 넣지 않으면 별도 네트워크 설정 없이 바로 인터넷 아웃바운드가 가능**하므로 이번 전환에서는 이 부분이 훨씬 단순해진다.

---

## 1. 재사용 vs 신규 작성

OCI_FUNCTIONS_MIGRATION_PLAN.md의 "5. 기존 코드를 그대로 유지할 수 있는 부분"(Entity/Repository/DTO/Controller/`@LoginCheck`/`OAuth2SuccessHandler`/`Result`/`GlobalExceptionHandler`/`S3Service` 등)은 **AWS 전환에도 동일하게 그대로 적용**된다 — 이 판단은 실행 환경(OCI냐 AWS냐)과 무관하게 "Lift & Adapt로 Controller를 재작성하지 않는다"는 원칙에서 나온 것이기 때문이다.

바뀌는 것은 **어댑터 계층(진입점)과 배포/네트워킹/설정 주입 방식**뿐이다.

| 구분 | OCI Functions 버전 (보존, 삭제 안 함) | AWS Lambda 버전 (신규) |
|---|---|---|
| 진입점 어댑터 | `Function/OciFunctionHandler.java` (Fn Java FDK `HTTPGatewayContext` + 수동 `MockHttpServletRequest/Response` 브릿지) | `Function/AwsLambdaHandler.java` (신규) — 아래 2번 항목 참고 |
| 배포 프로파일 | `application-oci.properties` (`DB_DIRECT_HOST=10.0.0.14`) | `application-aws.properties` (신규, `DB_DIRECT_HOST=193.123.234.59`) |
| 컨테이너 베이스 이미지 | `fnproject/fn-java-fdk*` | `public.ecr.aws/lambda/java:17` (AWS 공식 Lambda Java 17 컨테이너 이미지) |
| 함수 메타데이터 | `func.yaml` | 불필요 (Lambda는 콘솔/CLI로 핸들러 지정) |
| 레지스트리 | OCIR | Amazon ECR |
| HTTP 진입점 | OCI API Gateway (계획만, 미구현) | API Gateway(HTTP API) + Lambda 프록시 통합 |
| 시크릿/설정 | OCI Functions Configuration | Lambda 환경변수 (동일하게 `${ENV_VAR}` 패턴 그대로 재사용) |

---

## 2. 어댑터 계층 설계 (결정)

**결정: 직접 만든 `MockHttpServletRequest`/`Response` 브릿지 대신, AWS가 공식 지원하는 `aws-serverless-java-container` 라이브러리를 사용한다.**

- OCI에는 이런 공식 어댑터가 없어 `OciFunctionHandler`를 직접 작성해야 했지만(Fn Java FDK의 `HTTPGatewayContext`를 손으로 Mock 서블릿 요청/응답으로 변환), AWS는 Spring/Spring Boot 앱을 API Gateway + Lambda 위에서 그대로 구동하기 위한 `com.amazonaws.serverless:aws-serverless-java-container-springboot3` 라이브러리를 공식/커뮤니티 표준으로 제공한다. 이미 우리가 겪은 문제(서블릿 컨텍스트 초기화, `load-on-startup`, 요청/응답 변환)를 이 라이브러리가 내부적으로 이미 처리해준다.
- 이 라이브러리는 API Gateway의 `APIGatewayProxyRequestEvent`/`APIGatewayProxyResponseEvent`(또는 HTTP API의 `APIGatewayV2HTTPEvent`)를 받아 Spring의 `DispatcherServlet`에 위임하고 응답을 다시 변환해주는, 우리가 `OciFunctionHandler`에서 수작업으로 구현한 것과 동일한 원리의 검증된 구현체다.
- **41개 Controller/`@LoginCheck`/DTO는 이번에도 한 줄도 건드리지 않는다.**

새로 만들 파일:

| 파일 | 목적 |
|---|---|
| `src/main/java/com/xaexal/app/Function/AwsLambdaHandler.java` | `RequestStreamHandler` 구현체. `aws-serverless-java-container`의 `SpringBootLambdaContainerHandler`를 감싸서 API Gateway 이벤트를 `SaintApplication`(DispatcherServlet)에 위임 |
| `src/main/resources/application-aws.properties` | AWS Lambda 배포 전용 프로파일. `spring.profiles.active=aws`, `app.datasource.mode=direct`, `app.datasource.direct-host=${DB_DIRECT_HOST:193.123.234.59}`, `server.ssl.enabled=false`, `spring.jpa.hibernate.ddl-auto=validate` — `application-oci.properties`와 거의 동일한 값이며 host만 다름 |
| `Dockerfile.aws` (기존 `Dockerfile`은 OCI용으로 보존, 삭제하지 않음) | `public.ecr.aws/lambda/java:17` 베이스 이미지로 빌드하는 Lambda 컨테이너 이미지 정의 |

build.gradle에 추가할 의존성(기존 의존성은 삭제하지 않음):
```gradle
implementation 'com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.1.3'
implementation 'com.amazonaws:aws-lambda-java-core:1.2.3'
implementation 'com.amazonaws:aws-lambda-java-events:3.13.0'
```

---

## 3. 네트워킹 (결정 — OCI 대비 훨씬 단순함)

- **Lambda 함수를 VPC에 연결하지 않는다.** (OCI Functions처럼 VCN/서브넷을 지정할 필요 자체가 없음) VPC에 연결하지 않은 Lambda는 AWS가 관리하는 기본 네트워크에서 실행되며, 인터넷 아웃바운드가 기본으로 열려 있다.
- DB 접속 대상은 Compute VM의 **공인 IP** `193.123.234.59:3306`. 이 포트/소스(`0.0.0.0/0`)는 이미 OCI Security List에서 허용되어 있음(별도 OCI 측 변경 불필요, [[reference_saintfunctions_oci_infra]] 참고). NSG(`ig-quick-action-NSG`)에도 이미 `0.0.0.0/0` TCP 3306 규칙을 추가해둔 상태(OCI Functions 우회 테스트 때 추가함 — 그대로 유효, 삭제할 필요 없음).
- **주의**: OCI Functions에서 공인 IP로 접속을 시도했을 때도 `NoRouteToHostException`이 났던 건 OCI Functions 자체의 VNIC 버그였지, 목적지(Compute VM)나 네트워크 경로 자체의 문제가 아니었다(SSH 등 다른 경로로는 이 공인 IP가 정상 응답함을 이미 확인함). 따라서 AWS Lambda(OCI VNIC 버그와 무관한 별도 플랫폼)에서 동일한 공인 IP로 접속하면 정상 동작할 것으로 예상(추정 — 실제 배포 후 검증 필요).

---

## 4. 환경변수 / 시크릿 (결정)

OCI_FUNCTIONS_MIGRATION_PLAN.md 8번 항목과 동일한 원칙 — 기존 `${ENV_VAR}` 플레이스홀더 방식을 그대로 재사용, 코드 변경 없음. Lambda 함수의 환경변수로 아래 값을 등록(OCI Function에 등록했던 것과 동일한 11개 키 + `DB_DIRECT_HOST`):

`AWS_ACCESS_KEY_ID`, `AWS_SECRET_KEY`, `CORS_ALLOWED_ORIGIN`, `DB_PASSWORD`, `DB_DIRECT_HOST`(=`193.123.234.59`), `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `KAKAO_CLIENT_SECRET`, `MAIL_PASSWORD`, `NAVER_CLIENT_SECRET`, `TOSS_CLIENT_KEY`, `TOSS_SECRET_KEY`

**참고 (별도 트랙, 이번 계획의 필수 항목은 아님)**: 현재 `AWS_ACCESS_KEY_ID`/`AWS_SECRET_KEY`가 AWS **root 계정** 키라는 사실을 확인함([[project_saint_aws_root_key_debt]] 참고). 이번 Lambda 작업 자체는 이 키로 계속 진행하되, 추후 별도로 IAM 사용자로 교체가 필요함.

---

## 5. 로컬 테스트 방법 (결정)

`aws-serverless-java-container`는 로컬에서 실제 Lambda 없이도 `LambdaHandler`를 직접 호출하는 방식으로 테스트 가능 — OCI 때 만든 `OciFunctionHandlerManualCheck.java`와 동일한 패턴을 재사용한다.

1. 기존 `./gradlew bootRun --args='--app.datasource.mode=tunnel'`로 Controller/Service 회귀 테스트(변경 없음, 계속 유효).
2. 신규 `AwsLambdaHandlerManualCheck.java`(가칭, `OciFunctionHandlerManualCheck.java`와 동일한 구조) — `APIGatewayProxyRequestEvent`를 직접 만들어 `AwsLambdaHandler.handleRequest()`를 호출하고 `GET /` → 200 "home" 확인.
3. AWS SAM CLI(`sam local invoke`) 또는 `docker run`으로 Lambda 컨테이너 이미지를 로컬에서 직접 실행해 실제 Lambda Runtime Interface Emulator로 검증(선택, 필요시 진행).
4. 실제 AWS 배포 후 `aws lambda invoke`로 직접 호출 검증, 이어서 API Gateway 경유 `curl` 테스트.

---

## 6. AWS 배포 절차 (결정, 실행은 사용자 승인 후 단계별로)

1. **ECR 리포지토리 생성** (`aws ecr create-repository`)
2. **Docker 이미지 빌드 + ECR 푸시** (`Dockerfile.aws` 기준, `public.ecr.aws/lambda/java:17` 베이스)
3. **IAM 역할 생성** — Lambda 실행 역할(최소 권한: CloudWatch Logs 쓰기만 필요, VPC 미연결이므로 ENI 관련 권한 불필요)
4. **Lambda 함수 생성** (`aws lambda create-function`, 컨테이너 이미지 기반, 핸들러는 `AwsLambdaHandler::handleRequest`)
5. **환경변수 설정** (4번 항목의 11+1개 키)
6. **API Gateway(HTTP API) 생성 + Lambda 프록시 통합** — 모든 경로(`$default` 또는 `/{proxy+}`)를 이 Lambda로 라우팅해 기존 41개 Controller의 URL이 그대로 보이도록 함(OCI 계획의 API Gateway 원칙과 동일)
7. **invoke 테스트** — `aws lambda invoke`로 직접, 이어서 API Gateway 엔드포인트로 `curl` 테스트
8. **CORS/OAuth2 redirect-uri 갱신** — 배포 도메인 확정 후 처리(OCI 계획과 동일한 후속 작업)

---

## 확인이 필요한 사항 (실 작업 착수 전)

1. AWS 리전 선택 — 기존 S3가 `ap-northeast-2`(서울)이므로 동일 리전 사용을 기본으로 제안(확인 필요, 사용자 확정)
2. `aws-serverless-java-container-springboot3` 라이브러리와 이 프로젝트의 Spring Boot 3.4.2 버전 호환성 — 실제 빌드 시 버전 확정 필요(현재 2.1.3은 제안값, 최신 릴리스 확인 후 조정 가능)
3. API Gateway 페이로드 크기 제한(HTTP API는 기본 10MB) — 기존 `spring.servlet.multipart.max-file-size=50MB` 설정과 충돌 가능성 있음(확인 필요, 큰 파일 업로드가 실제로 50MB에 근접하는 경우가 있는지 확인 필요)
4. Lambda 함수 타임아웃/메모리 설정값 (OCI에서는 memory 1024MB/timeout 120s 사용) — 동일하게 시작해서 조정 여부 결정

---

**다음 단계**: 위 계획 승인 후, 2번 항목(어댑터 계층: `AwsLambdaHandler.java` + `application-aws.properties` + build.gradle 의존성 추가)부터 순차적으로 실제 코드 작업 진행.
